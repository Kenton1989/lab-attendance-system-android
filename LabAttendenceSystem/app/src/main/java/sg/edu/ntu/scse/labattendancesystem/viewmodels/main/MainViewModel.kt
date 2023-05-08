package sg.edu.ntu.scse.labattendancesystem.viewmodels.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sg.edu.ntu.scse.labattendancesystem.domain.models.Session
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.Attendance
import sg.edu.ntu.scse.labattendancesystem.domain.models.AttendanceState
import sg.edu.ntu.scse.labattendancesystem.domain.models.Outcome
import sg.edu.ntu.scse.labattendancesystem.repository.MainRepository
import sg.edu.ntu.scse.labattendancesystem.viewmodels.AssignableLiveData
import sg.edu.ntu.scse.labattendancesystem.viewmodels.BaseViewModel
import java.time.ZonedDateTime

class MainViewModel(private val app: LabAttendanceSystemApplication) : BaseViewModel() {
    companion object {
        val TAG: String = MainViewModel::class.java.simpleName
//        const val NO_LOCAL_ID: Int = -1
    }


    private lateinit var repo: MainRepository

    lateinit var online: LiveData<Boolean>
        private set

    lateinit var syncing: LiveData<Boolean>
        private set

    private val _lastSync: MediatorLiveData<Outcome<Unit>> = MediatorLiveData()
    val lastSync: LiveData<Outcome<Unit>> get() = _lastSync

    private var _selectedSession = AssignableLiveData<Session?>()
    val selectedSession: LiveData<Session?> get() = _selectedSession.liveData

    private var _selectedStudentAttendances = AssignableLiveData<List<Attendance>>()
    val selectedStudentAttendances: LiveData<List<Attendance>>
        get() = _selectedStudentAttendances.liveData

    private var _selectedTeacherAttendances = AssignableLiveData<List<Attendance>>()
    val selectedTeacherAttendances: LiveData<List<Attendance>>
        get() = _selectedTeacherAttendances.liveData

    private val _activeSessionList = AssignableLiveData<List<Session>>()
    val activeSessionList: LiveData<List<Session>> = _activeSessionList.liveData

    init {
        viewModelScope.launch { asyncInit() }
    }

    private suspend fun asyncInit() {
        repo = MainRepository(app, viewModelScope)
        repo.awaitForInitDone()

        _activeSessionList.setDataSource(repo.activeSessions.map { l -> l.sortedBy { it.startTime } }
            .onEach { onActiveSessionsUpdated(it) }.asLiveData())

        online = repo.online.asLiveData()
        syncing = repo.syncing.asLiveData()

        _lastSync.addSource(online) { updateSyncState(it, syncing.value) }
        _lastSync.addSource(syncing) { updateSyncState(online.value, it) }
    }

    fun refreshData() {
        repo.refreshData()
    }

    private fun updateSyncState(online: Boolean?, syncing: Boolean?) {
        Log.d(TAG, "syncing: $syncing, online: $online")
        if (syncing != false) {
            _lastSync.value = Outcome.Loading
        } else if (online == false) {
            _lastSync.value = Outcome.Failure()
        } else {
            _lastSync.value = Outcome.Success(Unit)
        }
    }

    private fun onActiveSessionsUpdated(newSessions: List<Session>) {
        // reset selected session when the section is not active
        if (newSessions.all { it.id != _selectedSession.liveData.value?.id }) {
            val newSelectedSession = if (newSessions.isNotEmpty()) newSessions.first() else null
            Log.d(TAG, "onUpdateActiveSessions: reset selected session to $newSelectedSession")
            updateSelectedSession(newSelectedSession?.id)
        } else {
            Log.d(TAG, "onUpdateActiveSessions: selected session not reset")
        }
    }

    fun updateSelectedSession(sessionId: Int?) {
        if (sessionId == selectedSession.value?.id) return

        if (sessionId != null) {
            val selectedSessionFlow = repo.getDetailSession(sessionId)
            _selectedSession.setDataSource(selectedSessionFlow.asLiveData())
            _selectedStudentAttendances.setDataSource(
                getCompleteStudentAttendanceOfSession(sessionId, selectedSessionFlow).asLiveData(),
            )
            _selectedTeacherAttendances.setDataSource(
                getCompleteTeacherAttendanceOfSession(sessionId, selectedSessionFlow).asLiveData()
            )
        } else {
            _selectedSession.setConstSource(null)
            _selectedStudentAttendances.setConstSource(listOf())
            _selectedTeacherAttendances.setConstSource(listOf())
        }
    }

    fun verifyLogoutCredentials(password: String): LiveData<Outcome<Unit>> {
        return repo.verifyLogoutCredential(password).asLiveData()
    }

    fun logout(): LiveData<Outcome<Unit>> {
        return repo.logout().asLiveData()
    }

    fun studentCheckIn(attendance: Attendance): LiveData<Outcome<Unit>> {
        val newAttendance = attendance.copy(checkInState = AttendanceState.ATTEND)
        Log.d(TAG, "new student check in $newAttendance")
        return repo.updateStudentAttendance(newAttendance).asLiveData()
    }

    fun teacherCheckIn(attendance: Attendance): LiveData<Outcome<Unit>> {
        val newAttendance = attendance.copy(checkInState = AttendanceState.ATTEND)
        Log.d(TAG, "new teacher check in $newAttendance")
        return repo.updateTeacherAttendance(newAttendance).asLiveData()
    }

    private fun getCompleteStudentAttendanceOfSession(
        sessionId: Int, sessionFlow: Flow<Session>? = null
    ): Flow<List<Attendance>> {
        val existingRecordsF = repo.getDbStudentAttendances(sessionId)
        val sessionDetailF = sessionFlow ?: repo.getDetailSession(sessionId)

        return sessionDetailF.combine(existingRecordsF) { session, records ->
            val recordOfStudent = records.associateBy { it.attender.id }
            val newRecordTime = ZonedDateTime.now()
//            Log.d(TAG, "combining student attendance")
            session.group.students!!.map {
                val res = recordOfStudent[it.student.id]?.copy(session = session, seat = it.seat)
                    ?: Attendance(
//                    localId = 0,
                        id = null,
                        session = session,
                        sessionId = session.id,
                        attender = it.student,
                        seat = it.seat,
                        checkInState = AttendanceState.ABSENT,
                        checkInDatetime = newRecordTime,
                        lastModify = newRecordTime,
                    )
                res
            }
        }
    }

    private fun getCompleteTeacherAttendanceOfSession(
        sessionId: Int, sessionFlow: Flow<Session>? = null
    ): Flow<List<Attendance>> {
        val existingRecordsF = repo.getDbTeacherAttendances(sessionId)
        val sessionDetailF = sessionFlow ?: repo.getDetailSession(sessionId)

        return sessionDetailF.combine(existingRecordsF) { session, records ->
            val recordOfStudent = records.associateBy { it.attender.id }
            val newRecordTime = ZonedDateTime.now()
//            Log.d(TAG, "combining teacher attendance")
            session.group.teachers!!.map {
                recordOfStudent[it.id]?.copy(session = session) ?: Attendance(
//                    localId = 0,
                    id = null,
                    session = session,
                    sessionId = session.id,
                    attender = it,
                    seat = null,
                    checkInState = AttendanceState.ABSENT,
                    checkInDatetime = newRecordTime,
                    lastModify = newRecordTime,
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repo.cleanUp()
    }
}
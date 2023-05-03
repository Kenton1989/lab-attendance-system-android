package sg.edu.ntu.scse.labattendancesystem.viewmodels.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sg.edu.ntu.scse.labattendancesystem.domain.models.Session
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.Attendance
import sg.edu.ntu.scse.labattendancesystem.domain.models.AttendanceState
import sg.edu.ntu.scse.labattendancesystem.repository.MainRepository
import sg.edu.ntu.scse.labattendancesystem.viewmodels.AssignableLiveData
import sg.edu.ntu.scse.labattendancesystem.viewmodels.BaseViewModel
import java.time.ZonedDateTime

class MainViewModel(private val app: LabAttendanceSystemApplication) : BaseViewModel() {
    companion object {
        val TAG: String = MainViewModel::class.java.simpleName
    }


    private lateinit var repo: MainRepository

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
    }

    private fun onActiveSessionsUpdated(newSessions: List<Session>) {
        // reset selected session when the section is not active
        if (newSessions.all { it.id != _selectedSession.liveData.value?.id }) {
            Log.d(TAG, "onUpdateActiveSessions: reset selected session")
            val newSelectedSession = if (newSessions.isNotEmpty()) newSessions.first() else null
            updateSelectedSession(newSelectedSession?.id)
        } else {
            Log.d(TAG, "onUpdateActiveSessions: selected session not reset")
        }
    }

    fun updateSelectedSession(sessionId: Int?) {
        if (sessionId == selectedSession.value?.id) return;

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

    fun studentCheckIn(attendance: Attendance) {

    }

    fun undoStudentCheckIn(attendance: Attendance) {

    }

    fun teacherCheckIn(attendance: Attendance) {

    }

    fun undoTeacherCheckIn(attendance: Attendance) {

    }

    private fun getCompleteStudentAttendanceOfSession(
        sessionId: Int, sessionFlow: Flow<Session>? = null
    ): Flow<List<Attendance>> {
        val existingRecordsF = repo.getDbStudentAttendances(sessionId)
        val sessionDetailF = sessionFlow ?: repo.getDetailSession(sessionId)

        return sessionDetailF.combine(existingRecordsF) { session, records ->
            val recordOfStudent = records.associateBy { it.attender.id }
            val newRecordTime = ZonedDateTime.now()
            Log.d(TAG, "combining student attendance")
            session.group.students!!.map {
                recordOfStudent[it.id]?.copy(seat = it.seat) ?: Attendance(
                    localId = 0,
                    id = null,
                    session = session,
                    sessionId = session.id,
                    attender = it.student,
                    seat = it.seat,
                    checkInState = AttendanceState.ABSENT,
                    checkInDatetime = newRecordTime,
                    lastModify = newRecordTime,
                )
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
            Log.d(TAG, "combining teacher attendance")
            session.group.teachers!!.map {
                recordOfStudent[it.id] ?: Attendance(
                    localId = 0,
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
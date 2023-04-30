package sg.edu.ntu.scse.labattendancesystem.viewmodels.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sg.edu.ntu.scse.labattendancesystem.domain.models.Session
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.Attendance
import sg.edu.ntu.scse.labattendancesystem.repository.MainRepository
import sg.edu.ntu.scse.labattendancesystem.domain.models.Result
import sg.edu.ntu.scse.labattendancesystem.viewmodels.BaseViewModel

class MainViewModel(app: LabAttendanceSystemApplication) : BaseViewModel() {
    companion object {
        val TAG: String = MainViewModel::class.java.simpleName
    }


    private val repo = MainRepository(app, viewModelScope)

    private var _selectedSession = MutableLiveData<Session?>()
    val selectedSession get() = _selectedSession

    private var _selectedStudentAttendances = MutableLiveData<List<Attendance>?>()
    val selectedStudentAttendances get() = _selectedStudentAttendances

    private var _activeSessionList = MutableLiveData<List<Session>>()
    val activeSessionList get() = _activeSessionList

    init {
        viewModelScope.launch { asyncInit() }
    }

    private suspend fun asyncInit() {
        repo.awaitForInitDone()
        val sessions = repo.getActiveSessions()
        sessions.collect {
            when (it) {
                is Result.Success -> updateActiveSessions(it.data)
                is Result.Failure -> Log.e(TAG, "failed to load session ${it.error}")
                else -> Log.d(TAG, "loading active session")
            }
        }
    }

    private fun updateActiveSessions(newVal: List<Session>) {
        val sortedSessions = newVal.sortedBy { it.startTime }
        _activeSessionList.value = sortedSessions

        // reset selected session when the section is not active
        if (newVal.all { it.id != _selectedSession.value?.id }) {
            val newSelectedSession =
                if (sortedSessions.isNotEmpty()) sortedSessions.first() else null
            updateSelectedSession(newSelectedSession)
        }
    }

    fun updateSelectedSession(session: Session?) {
        _selectedSession.value = session
        _selectedStudentAttendances.value = null
        if (session != null) {
            val studAtt = repo.getStudentAttendances(session)
            viewModelScope.launch {

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repo.cleanUp()
    }
}
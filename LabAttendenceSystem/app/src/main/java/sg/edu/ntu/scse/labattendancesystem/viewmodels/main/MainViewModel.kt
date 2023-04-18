package sg.edu.ntu.scse.labattendancesystem.viewmodels.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import sg.edu.ntu.scse.labattendancesystem.domain.models.Session
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.repository.MainRepository
import sg.edu.ntu.scse.labattendancesystem.repository.Result
import sg.edu.ntu.scse.labattendancesystem.viewmodels.BaseViewModel

class MainViewModel(app: LabAttendanceSystemApplication) : BaseViewModel() {
    companion object {
        val TAG: String = MainViewModel::class.java.simpleName
    }


    private val repo = MainRepository(app, viewModelScope)

    private var _selectedSession = MutableLiveData<Session?>()
    val selectedSession get() = _selectedSession

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
        _activeSessionList.value = newVal

        // clear selected session when the section is not active
        if (newVal.all { it.id != _selectedSession.value?.id }) {
            _selectedSession.value = null
        }
    }

    fun updateCurrentSession(session: Session) {
        _selectedSession.value = session
    }
}
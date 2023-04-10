package sg.edu.ntu.scse.labattendancesystem.viewmodels.main

import androidx.lifecycle.MutableLiveData
import sg.edu.ntu.scse.labattendancesystem.domain.models.Session
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.repository.MainRepository
import sg.edu.ntu.scse.labattendancesystem.viewmodels.BaseViewModel

class MainViewModel(app: LabAttendanceSystemApplication) : BaseViewModel() {
    private val repo = MainRepository(app)

    private var _selectedSession = MutableLiveData<Session?>()
    val selectedSession get() = _selectedSession

    private var _activeSessionList = MutableLiveData<List<Session>>(listOf())
    val activeSessionList get() = _activeSessionList

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
package sg.edu.ntu.scse.labattendancesystem.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.network.ApiServices
import sg.edu.ntu.scse.labattendancesystem.network.SessionManager
import sg.edu.ntu.scse.labattendancesystem.network.TokenManager
import sg.edu.ntu.scse.labattendancesystem.viewmodels.login.LoginViewModel

class ViewModelFactory(val app: Application) : ViewModelProvider.Factory {

    private val _app = app as? LabAttendanceSystemApplication
        ?: throw IllegalArgumentException("expecting an LabAttendanceSystemApplication")

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return makeLoginViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun makeLoginViewModel(): LoginViewModel {
        return LoginViewModel(_app)
    }

    fun getSM(): SessionManager {
        return SessionManager(TokenManager(_app.loginPreferenceDataStore), ApiServices.token)
    }
}
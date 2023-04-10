package sg.edu.ntu.scse.labattendancesystem.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.viewmodels.login.LoginViewModel
import sg.edu.ntu.scse.labattendancesystem.viewmodels.main.MainViewModel
import kotlin.reflect.KClass

class ViewModelFactory(val app: Application) : ViewModelProvider.Factory {

    private val _app = app as? LabAttendanceSystemApplication
        ?: throw IllegalArgumentException("expecting an LabAttendanceSystemApplication")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val modelIs = { c: KClass<*> -> modelClass.isAssignableFrom(c.java) }

        @Suppress("UNCHECKED_CAST")
        when {
            modelIs(LoginViewModel::class) -> return makeLoginViewModel() as T
            modelIs(MainViewModel::class) -> return makeMainViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun makeLoginViewModel(): LoginViewModel {
        return LoginViewModel(_app)
    }

    private fun makeMainViewModel(): MainViewModel {
        return MainViewModel(_app)
    }
}
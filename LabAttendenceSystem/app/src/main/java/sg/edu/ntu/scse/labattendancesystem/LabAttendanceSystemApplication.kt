package sg.edu.ntu.scse.labattendancesystem

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.currentCoroutineContext
import sg.edu.ntu.scse.labattendancesystem.database.LabAttendanceSystemDatabase
import sg.edu.ntu.scse.labattendancesystem.network.ApiServices
import sg.edu.ntu.scse.labattendancesystem.network.SessionManager
import sg.edu.ntu.scse.labattendancesystem.network.TokenManager
import sg.edu.ntu.scse.labattendancesystem.repository.LoginHistoryStore


class LabAttendanceSystemApplication : Application() {
    val loginPreferenceDataStore by preferencesDataStore(LOGIN_PREFERENCES_NAME)
    val tokenManager by lazy {
        val res = TokenManager(loginPreferenceDataStore)
        Log.d(TAG, "generated token manager $res")
        res
    }
    val loginHistoryStore by lazy {
        val res = LoginHistoryStore(loginPreferenceDataStore)
        Log.d(TAG, "generated login history manager $res")
        res
    }
    val apiServices by lazy { ApiServices(tokenManager) }
    val sessionManager by lazy { SessionManager(tokenManager, apiServices.token) }
    val database by lazy { LabAttendanceSystemDatabase.getDatabase(this) }

    companion object {
        private val TAG: String = LabAttendanceSystemApplication::class.java.simpleName
        const val LOGIN_PREFERENCES_NAME = "login_preference_datastore"
    }
}
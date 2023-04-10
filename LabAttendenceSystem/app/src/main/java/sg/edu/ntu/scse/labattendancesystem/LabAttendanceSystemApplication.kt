package sg.edu.ntu.scse.labattendancesystem

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.currentCoroutineContext
import sg.edu.ntu.scse.labattendancesystem.database.LabAttendanceSystemDatabase


class LabAttendanceSystemApplication: Application() {
    val loginPreferenceDataStore by preferencesDataStore(LOGIN_PREFERENCES_NAME)
    val database by lazy { LabAttendanceSystemDatabase.getDatabase(this) }

    companion object {
        const val LOGIN_PREFERENCES_NAME = "login_preference_datastore"
    }
}
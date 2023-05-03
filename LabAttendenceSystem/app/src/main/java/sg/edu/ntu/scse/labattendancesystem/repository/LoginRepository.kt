package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import retrofit2.HttpException
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.Result
import sg.edu.ntu.scse.labattendancesystem.network.UnauthenticatedError
import sg.edu.ntu.scse.labattendancesystem.network.models.LabResp
import java.net.HttpURLConnection


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(
    app: LabAttendanceSystemApplication,
    externalScope: CoroutineScope,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseRepository(
    app,
    externalScope,
    defaultDispatcher,
) {
    private val dataStore: DataStore<Preferences> = app.loginPreferenceDataStore
    private val sessionManager = app.sessionManager
    private val loginHistory = app.loginHistoryStore

    val lastLoginUsername: Flow<String?> get() = loginHistory.lastLoginUsername
    val lastLoginRoomNo: Flow<Int?> get() = loginHistory.lastLoginRoomNo

    fun labLogin(username: String, password: String, room: Int): Flow<Result<Unit>> {
        return loadFromNet {
            Log.d(TAG, "labLogin: login")
            sessionManager.login(username, password)

            lateinit var lab: LabResp
            try {
                Log.d(TAG, "labLogin: get lab")
                lab = sessionManager.getCurrentLab()
            } catch (e: HttpException) {
                checkUseIsNotLabError(e)
                throw e
            }

            Log.d(TAG, "labLogin: check lab room")
            if (room < 1 || room > lab.roomCount!!) {
                ensureLogout()
                throw InvalidLabRoomNumber(room, lab.roomCount!!)
            }
        }.onEach {
            if (it is Result.Success) {
                loginHistory.updateLastLoginUsername(username)
                loginHistory.updateLastLoginRoomNo(room)
            }
        }
    }

    fun isAlreadyLogin(): Flow<Result<Boolean>> {
        return loadFromNet {
            try {
                sessionManager.getCurrentUser()
                true
            } catch (e: UnauthenticatedError) {
                false
            }
        }
    }

    private suspend fun checkUseIsNotLabError(e: HttpException) {
        if (e.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            ensureLogout()
            throw UserIsNotLabError()
        }
    }

    private suspend fun ensureLogout() {
        try {
            sessionManager.logout()
        } catch (e: UnauthenticatedError) {
            // ignore unauthenticated error since we just want to ensure user has logout
            return
        }
    }

    private fun <T> readDataStore(key: Preferences.Key<T>) = dataStore.data.map { it[key] }

    private suspend fun <T> writeDataStore(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    companion object {
        private val TAG: String = LoginRepository::class.java.simpleName
    }
}
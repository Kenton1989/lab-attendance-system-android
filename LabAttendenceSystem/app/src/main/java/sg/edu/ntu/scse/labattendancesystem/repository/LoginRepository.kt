package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import retrofit2.HttpException
import sg.edu.ntu.scse.labattendancesystem.network.ApiServices
import sg.edu.ntu.scse.labattendancesystem.network.SessionManager
import sg.edu.ntu.scse.labattendancesystem.network.TokenManager
import sg.edu.ntu.scse.labattendancesystem.network.UnauthenticatedError
import sg.edu.ntu.scse.labattendancesystem.network.models.Lab
import java.net.HttpURLConnection

private val LAST_LOGIN_USERNAME = stringPreferencesKey("last_login_username")

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(private val dataStore: DataStore<Preferences>) : BaseRepository() {
    private val tokenManager = TokenManager(dataStore)
    private val sessionManager = SessionManager(tokenManager, ApiServices.token)

    val lastLoginUsername: Flow<String?> get() = readDataStore(LAST_LOGIN_USERNAME)

    fun labLogin(username: String, password: String, room: Int): Flow<Result<Unit>> {
        return load {
            Log.d(TAG, "labLogin: login")
            sessionManager.login(username, password)

            Log.d(TAG, "labLogin: get user")
            val user = sessionManager.getCurrentUser()
            val uid = user.id

            lateinit var lab: Lab
            try {
                Log.d(TAG, "labLogin: get lab")
                lab = sessionManager.getLab(uid)
            } catch (e: HttpException) {
                checkUseIsNotLabError(e)
                throw e
            }

            Log.d(TAG, "labLogin: check lab room")
            if (room < 1 || room > lab.roomCount) {
                ensureLogout()
                throw InvalidLabRoomNumber(room, lab.roomCount)
            }
        }.onEach {
            if (it is Result.Success) writeDataStore(LAST_LOGIN_USERNAME, username)
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
        val TAG: String = LoginRepository::class.java.simpleName
    }
}
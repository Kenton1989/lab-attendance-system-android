package sg.edu.ntu.scse.labattendancesystem.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import sg.edu.ntu.scse.labattendancesystem.network.ApiServices
import sg.edu.ntu.scse.labattendancesystem.network.SessionManager
import sg.edu.ntu.scse.labattendancesystem.network.TokenManager
import sg.edu.ntu.scse.labattendancesystem.network.models.User

private val LAST_LOGIN_USERNAME = stringPreferencesKey("last_login_username")

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(private val dataStore: DataStore<Preferences>) : BaseRepository() {
    private val tokenManager = TokenManager(dataStore)
    private val sessionManager = SessionManager(tokenManager, ApiServices.token)

    val lastLoginUsername: Flow<String?> get() = readDataStore(LAST_LOGIN_USERNAME)

    fun login(username: String, password: String): Flow<Result<Boolean>> {
        return load { sessionManager.login(username, password) }.onEach {
            if (it is Result.Success) writeDataStore(LAST_LOGIN_USERNAME, username)
        }
    }

    fun logout(): Flow<Result<Boolean>> {
        return load { sessionManager.logout() }
    }

    fun currentUser(): Flow<Result<User?>> {
        return load { sessionManager.currentUser() }
    }

    private fun <T> readDataStore(key: Preferences.Key<T>) = dataStore.data.map { it[key] }

    private suspend fun <T> writeDataStore(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }
}
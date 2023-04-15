package sg.edu.ntu.scse.labattendancesystem.network

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

class TokenManager(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        val TAG = TokenManager::class.java.simpleName
    }

    private var _cachedToken: String? = null;
    val cachedToken: String get() = _cachedToken!!;


    suspend fun setUpTokenCache(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        val flow = getToken()

        withContext(dispatcher) {
            _cachedToken = flow.first()
            Log.d(TAG, "initialized token")
        }

        scope.launch(dispatcher) {
            flow.collect {
                if (it == null) {
                    Log.w(TAG, "token become null")
                }
                _cachedToken = it
                Log.d(TAG, "updated token")
            }
            Log.d(TAG, "complete collect")
        }
    }

    fun getToken(): Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { it[AUTH_TOKEN_KEY] }

    suspend fun saveToken(newToken: String) {
        dataStore.edit { it[AUTH_TOKEN_KEY] = newToken }
    }

    suspend fun clearToken() {
        dataStore.edit { it.remove(AUTH_TOKEN_KEY) }
    }

}
package sg.edu.ntu.scse.labattendancesystem.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class TokenManager(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
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
        .map { it[AUTH_TOKEN] }

    suspend fun saveToken(newToken: String) {
        dataStore.edit { it[AUTH_TOKEN] = newToken }
    }

    suspend fun clearToken() {
        dataStore.edit { it.remove(AUTH_TOKEN) }
    }

}
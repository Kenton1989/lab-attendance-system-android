package sg.edu.ntu.scse.labattendancesystem.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoginHistoryStore(private val dataStore: DataStore<Preferences>) {
    val lastLoginUsername: Flow<String?> get() = readDataStore(LAST_LOGIN_USERNAME)
    val lastLoginRoomNo: Flow<Int?> get() = readDataStore(LAST_LOGIN_ROOM)

    suspend fun updateLastLoginUsername(newName: String?) {
        writeDataStore(LAST_LOGIN_USERNAME, newName)
    }

    suspend fun updateLastLoginRoomNo(newRoom: Int?) {
        writeDataStore(LAST_LOGIN_ROOM, newRoom)
    }

    private fun <T> readDataStore(key: Preferences.Key<T>) = dataStore.data.map { it[key] }

    private suspend fun <T> writeDataStore(key: Preferences.Key<T>, value: T?) {
        dataStore.edit {
            if (value != null) {
                it[key] = value
            } else {
                it.remove(key)
            }
        }
    }

    companion object {
        private val TAG: String = LoginRepository::class.java.simpleName
        private val LAST_LOGIN_USERNAME = stringPreferencesKey("last_login_username")
        private val LAST_LOGIN_ROOM = intPreferencesKey("last_login_room_no")
    }
}
package sg.edu.ntu.scse.labattendancesystem.network

import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import sg.edu.ntu.scse.labattendancesystem.repository.LoginRepository
import java.security.SecureRandom
import java.security.Security
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class LoginHistoryStore(private val dataStore: DataStore<Preferences>) {
    val lastLoginUsername: Flow<String?> get() = readDataStore(LAST_LOGIN_USERNAME)
    val lastLoginRoomNo: Flow<Int?> get() = readDataStore(LAST_LOGIN_ROOM_NO)

    private val lastLoginPasswordHash: Flow<String?> get() = readDataStore(LAST_LOGIN_PASSWORD_HASH)
    private val lastLoginPasswordHashSalt: Flow<String?>
        get() = readDataStore(
            LAST_LOGIN_PASSWORD_HASH_SALT
        )

    suspend fun updateLastLoginUsername(newName: String?) {
        writeDataStore(LAST_LOGIN_USERNAME, newName)
    }

    suspend fun updateLastLoginRoomNo(newRoom: Int?) {
        writeDataStore(LAST_LOGIN_ROOM_NO, newRoom)
    }

    suspend fun updateLastLoginPassword(plainPassword: String?) {
        if (plainPassword == null) {
            writeSaltAndHash(null, null)
            return
        }

        val pair = createPasswordHash(plainPassword) // pair<salt, hash>
        writeSaltAndHash(pair.first, pair.second)
    }


    suspend fun verifyLastLoginPassword(plainPassword: String): Boolean {
        val lastHash = lastLoginPasswordHash.first()
        val lastSalt = lastLoginPasswordHashSalt.first()
        if (lastHash == null || lastSalt == null) return false
        val lastSaltByte = lastSalt.toB64Bytes()
        val lastHashByte = lastHash.toB64Bytes()
        val hash = pbkdf2(plainPassword.toCharArray(), lastSaltByte)
        return hash.contentEquals(lastHashByte)
    }

    // return Pair<Salt, Hash>
    private fun createPasswordHash(plainPassword: String): Pair<String, String> {
        val salt = createSalt()
        val hash = pbkdf2(plainPassword.toCharArray(), salt)
        return Pair(salt.toB64Str(), hash.toB64Str())
    }

    private fun createSalt(): ByteArray {
        val sr = SecureRandom()
        val salt = ByteArray(32)
        sr.nextBytes(salt)
        return salt
    }

    private fun pbkdf2(
        password: CharArray,
        salt: ByteArray,
        iterationCount: Int = 4096,
        keyLength: Int = 256
    ): ByteArray {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSha256")
        val spec = PBEKeySpec(password, salt, iterationCount, keyLength)
        return factory.generateSecret(spec).encoded
    }

    private fun ByteArray.toB64Str(): String {
        return Base64.encodeToString(this, Base64.DEFAULT)
    }

    private fun String.toB64Bytes(): ByteArray {
        return Base64.decode(this, Base64.DEFAULT)
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

    private suspend fun writeSaltAndHash(salt: String?, hash: String?) {
        dataStore.edit {
            if (salt != null) {
                it[LAST_LOGIN_PASSWORD_HASH_SALT] = salt
            } else {
                it.remove(LAST_LOGIN_PASSWORD_HASH_SALT)
            }

            if (hash != null) {
                it[LAST_LOGIN_PASSWORD_HASH] = hash
            } else {
                it.remove(LAST_LOGIN_PASSWORD_HASH)
            }
        }
    }

    companion object {
        private val TAG: String = LoginRepository::class.java.simpleName
        private val LAST_LOGIN_USERNAME = stringPreferencesKey("last_login_username")
        private val LAST_LOGIN_PASSWORD_HASH = stringPreferencesKey("last_login_password_hash")
        private val LAST_LOGIN_PASSWORD_HASH_SALT =
            stringPreferencesKey("last_login_password_hash_salt")
        private val LAST_LOGIN_ROOM_NO = intPreferencesKey("last_login_room_no")
    }
}
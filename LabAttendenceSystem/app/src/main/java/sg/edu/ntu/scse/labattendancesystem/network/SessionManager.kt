package sg.edu.ntu.scse.labattendancesystem.network

import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import sg.edu.ntu.scse.labattendancesystem.network.api.AuthApi
import sg.edu.ntu.scse.labattendancesystem.network.models.User
import java.net.HttpURLConnection

class SessionManager(
    private val tokenManager: TokenManager, private val api: AuthApi
) {

    /**
     * Perform login process and store the token.
     * @return return true if login process succeed.
     *         false if username is unknown or password is wrong.
     *         other types of error will cause exception.
     */
    suspend fun login(username: String, password: String): Boolean {
        try {
            val tokenResp = api.createToken(username, password)
            tokenManager.saveToken(tokenResp.token)
        } catch (e: HttpException) {
            if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return false
            }
            throw e
        }

        return true
    }

    /**
     * Perform logout process and remove the token.
     * @return return true if logout process succeed. false if the user hasn't login.
     *         Other errors that cannot be solved by login will cause exception.
     */
    suspend fun logout(): Boolean {
        try {
            val token = tokenManager.getToken().first() ?: return false
            api.revokeToken(token)
        } catch (e: HttpException) {
            if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                tokenManager.clearToken()
                return false
            }
            throw e
        }
        return true
    }

    /**
     * Get current user.
     * @return return current user if succeed. null if the user hasn't login.
     *         Other errors that cannot be solved by login will cause exception.
     */
    suspend fun currentUser(): User? {
        try {
            val token = tokenManager.getToken().first() ?: return null
            return api.currentUser(token)
        } catch (e: HttpException) {
            if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                tokenManager.clearToken()
                return null
            }
            throw e
        }
    }


}
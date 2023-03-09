package sg.edu.ntu.scse.labattendancesystem.network

import android.util.Base64
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import sg.edu.ntu.scse.labattendancesystem.network.api.AuthApi
import sg.edu.ntu.scse.labattendancesystem.network.models.Lab
import sg.edu.ntu.scse.labattendancesystem.network.models.User
import java.net.HttpURLConnection

class SessionManager(
    private val tokenManager: TokenManager, private val api: AuthApi
) {

    /**
     * Perform login process and store the token.
     * @param enforceLogin if false (default), when currently login user is the same to
     *        the user to login, the login process will be skipped
     * @throws UnauthenticatedError if username or password is incorrect
     */
    suspend fun login(username: String, password: String, enforceLogin: Boolean = false) {
        if (!enforceLogin) {
            try {
                val user = getCurrentUser()
                if (user.username == username) return
            } catch (_: UnauthenticatedError) {
                // do nothing, continue login
            }
        }


        val authPayload = "$username:$password"
        val data = authPayload.toByteArray()
        val base64 = Base64.encodeToString(data, Base64.NO_WRAP)

        try {
            val tokenResp = api.createToken("Basic $base64")
            tokenManager.saveToken(tokenResp.token)
        } catch (e: HttpException) {
            checkUnauthorizedError(e)
            throw e
        }
    }

    /**
     * Perform logout process and remove the token.
     * @throws UnauthenticatedError if the user hasn't login.
     */
    suspend fun logout() {
        try {
            api.revokeToken(tokenHeader())
            tokenManager.clearToken()
        } catch (e: HttpException) {
            checkUnauthorizedError(e)
            throw e
        }
    }

    /**
     * Get current user.
     * @return return current login user
     * @throws UnauthenticatedError if user hasn't login
     */
    suspend fun getCurrentUser(): User {
        try {
            return api.getCurrentUser(tokenHeader())
        } catch (e: HttpException) {
            checkUnauthorizedError(e)
            throw e
        }
    }

    /**
     * Get a lab.
     * @return return the retrieved lab
     * @throws UnauthenticatedError if user hasn't login
     */
    suspend fun getLab(id: Long): Lab {
        try {
            return api.getLab(tokenHeader(), id)
        } catch (e: HttpException) {
            checkUnauthorizedError(e)
            throw e
        }
    }

    private suspend fun tokenHeader(): String {
        val token = tokenManager.getToken().first() ?: throw UnauthenticatedError()
        return "Token $token"
    }

    private suspend fun checkUnauthorizedError(e: HttpException) {
        if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            tokenManager.clearToken()
            throw UnauthenticatedError()
        }
    }
}
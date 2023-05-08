package sg.edu.ntu.scse.labattendancesystem.network

import android.util.Base64
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import sg.edu.ntu.scse.labattendancesystem.network.api.AuthApi
import sg.edu.ntu.scse.labattendancesystem.network.models.LabResp
import sg.edu.ntu.scse.labattendancesystem.network.models.UserResp
import java.net.ConnectException
import java.net.HttpURLConnection

class SessionManager(
    private val tokenManager: TokenManager,
    private val loginHistory: LoginHistoryStore,
    private val api: AuthApi
) {
    companion object {
        val TAG: String = SessionManager::class.java.simpleName
        const val DEFAULT_LOGIN_TIMEOUT = 30000L
    }

    /**
     * Perform login process and store the token and login history.
     * @param enforceLogin if false (default), when currently login user is the same to
     *        the user to login, the login process will be skipped
     * @param loginTimeout the maximal time allowed to be taken to perform online login
     * @param tryCachedPasswordIfTimeout if true, when loginTimeout is met, locally cached credential
     *      is used to verify the credentials
     * @throws UnauthenticatedError if username or password is incorrect
     */
    suspend fun login(
        username: String,
        password: String,
        enforceLogin: Boolean = false,
        loginTimeout: Long = DEFAULT_LOGIN_TIMEOUT,
        tryCachedPasswordIfTimeout: Boolean = true,
    ) {
        return loginImpl(
            username, password,
            enforceLogin = enforceLogin,
            storeToken = true,
            storeLoginHistory = true,
            tryCachedPasswordIfTimeout = tryCachedPasswordIfTimeout,
            loginTimeout = loginTimeout,
        )
    }

    /**
     * Perform login process to check if credential is valid.
     * @param loginTimeout the maximal time allowed to be taken to perform online login
     * @param tryCachedPasswordIfTimeout if true, when loginTimeout is met, locally cached credential
     *      is used to verify the credentials
     * @return true if verification succeeded
     */
    suspend fun verifyCredential(
        username: String,
        password: String,
        loginTimeout: Long = DEFAULT_LOGIN_TIMEOUT,
        tryCachedPasswordIfTimeout: Boolean = true,
    ): Boolean {
        return try {
            loginImpl(
                username, password,
                enforceLogin = true,
                storeToken = false,
                storeLoginHistory = false,
                tryCachedPasswordIfTimeout = tryCachedPasswordIfTimeout,
                loginTimeout = loginTimeout,
            )
            true
        } catch (e: UnauthenticatedError) {
            false
        }
    }

    private suspend fun loginImpl(
        username: String,
        password: String,
        enforceLogin: Boolean,
        storeToken: Boolean,
        storeLoginHistory: Boolean,
        tryCachedPasswordIfTimeout: Boolean,
        loginTimeout: Long
    ) {
        try {
            withTimeout(loginTimeout) {
                loginOnlineImpl(
                    username, password,
                    enforceLogin = enforceLogin,
                    storeToken = storeToken,
                    storeLoginHistory = storeLoginHistory
                )
            }
        } catch (e: Exception) {
            if ((e is TimeoutCancellationException || e is ConnectException) &&
                tryCachedPasswordIfTimeout
            ) {
                if (username == loginHistory.lastLoginUsername.first() &&
                    loginHistory.verifyLastLoginPassword(password)
                ) {
                    return
                } else {
                    throw UnauthenticatedError()
                }
            } else {
                throw e
            }
        }
    }

    private suspend fun loginOnlineImpl(
        username: String,
        password: String,
        enforceLogin: Boolean,
        storeToken: Boolean,
        storeLoginHistory: Boolean,
    ) {
        if (!enforceLogin) {
            try {
                val user = getCurrentUser()
                if (user.username == username) return
            } catch (_: UnauthenticatedError) {
                // do nothing, continue login
            }
        }

        val token = createToken(username, password)
        if (storeToken) {
            tokenManager.saveToken(token)
        } else {
            logout(token)
        }

        if (storeLoginHistory) {
            loginHistory.updateLastLoginUsername(username)
            loginHistory.updateLastLoginPassword(password)
        }
    }

    /**
     * Perform logout process and remove the token.
     * @param token the token used for logout. If it is null, the stored token will be used.
     * @throws UnauthenticatedError if the user hasn't login.
     */
    suspend fun logout(token: String? = null) {
        try {
            api.revokeToken(tokenHeader(token))
            if (token == null) tokenManager.clearToken()
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
    suspend fun getCurrentUser(): UserResp {
        try {
            return api.getCurrentUser(tokenHeader())
        } catch (e: HttpException) {
            checkUnauthorizedError(e)
            throw e
        }
    }

    /**
     * Get a lab.
     * @return return current login lab user
     * @throws UnauthenticatedError if user hasn't login
     * @throws UserIsNotLabError if user is not a lab user
     */
    suspend fun getCurrentLab(): LabResp {
        try {
            val user = getCurrentUser()
            return api.getLab(tokenHeader(), user.id!!)
        } catch (e: HttpException) {
            checkUnauthorizedError(e)
            checkUserIsNotLabError(e)
            throw e
        }
    }

    private suspend fun createToken(username: String, password: String): String {
        val authPayload = "$username:$password"
        val data = authPayload.toByteArray()
        val base64 = Base64.encodeToString(data, Base64.NO_WRAP)

        try {
            val tokenResp = api.createToken("Basic $base64")
            return tokenResp.token
        } catch (e: HttpException) {
            checkUnauthorizedError(e)
            throw e
        }
    }

    private suspend fun tokenHeader(tokenValue: String? = null): String {
        val token = tokenValue ?: tokenManager.getToken().first() ?: throw UnauthenticatedError()
        return "Token $token"
    }

    private suspend fun checkUnauthorizedError(e: HttpException) {
        if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            tokenManager.clearToken()
            throw UnauthenticatedError()
        }
    }

    private suspend fun checkUserIsNotLabError(e: HttpException) {
        if (e.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            tokenManager.clearToken()
            throw UserIsNotLabError()
        }
    }
}
package sg.edu.ntu.scse.labattendancesystem.network.api

import retrofit2.http.Header
import retrofit2.http.POST
import android.util.Base64
import retrofit2.http.DELETE
import retrofit2.http.GET
import sg.edu.ntu.scse.labattendancesystem.network.models.CreateTokenResponse
import sg.edu.ntu.scse.labattendancesystem.network.models.User

interface AuthApi {
    @POST("/users/me/tokens")
    suspend fun createTokenRaw(@Header("Authorization") authHeader: String): CreateTokenResponse

    @DELETE("/users/me/tokens/current")
    suspend fun revokeTokenRaw(@Header("Authorization") authHeader: String)

    @GET("/users/me")
    suspend fun currentUserRaw(@Header("Authorization") authHeader: String): User

    suspend fun createToken(username: String, password: String): CreateTokenResponse {
        val authPayload = "$username:$password"
        val data = authPayload.toByteArray()
        val base64 = Base64.encodeToString(data, Base64.NO_WRAP)

        return createTokenRaw("Basic $base64")
    }

    suspend fun revokeToken(token: String) {
        return revokeTokenRaw("Token $token")
    }

    suspend fun currentUser(token: String): User {
        return currentUserRaw("Token $token")
    }
}


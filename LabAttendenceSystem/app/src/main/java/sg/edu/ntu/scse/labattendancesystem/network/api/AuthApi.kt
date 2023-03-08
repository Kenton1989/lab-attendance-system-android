package sg.edu.ntu.scse.labattendancesystem.network.api

import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import sg.edu.ntu.scse.labattendancesystem.network.models.CreateTokenResponse
import sg.edu.ntu.scse.labattendancesystem.network.models.Lab
import sg.edu.ntu.scse.labattendancesystem.network.models.User

interface AuthApi {
    @POST("/users/me/tokens")
    suspend fun createToken(@Header("Authorization") authHeader: String): CreateTokenResponse

    @DELETE("/users/me/tokens/current")
    suspend fun revokeToken(@Header("Authorization") authHeader: String)

    @GET("/users/me")
    suspend fun currentUser(@Header("Authorization") authHeader: String): User

    @GET("/labs/{id}")
    suspend fun getLab(@Header("Authorization") authHeader: String, id: Long): Lab
}


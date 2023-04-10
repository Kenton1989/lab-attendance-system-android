package sg.edu.ntu.scse.labattendancesystem.network.api

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import sg.edu.ntu.scse.labattendancesystem.network.models.CreateTokenResp
import sg.edu.ntu.scse.labattendancesystem.network.models.LabResp
import sg.edu.ntu.scse.labattendancesystem.network.models.UserResp

interface AuthApi {
    @POST("users/me/tokens")
    suspend fun createToken(@Header("Authorization") authHeader: String): CreateTokenResp

    @DELETE("users/me/tokens/current")
    suspend fun revokeToken(@Header("Authorization") authHeader: String): Response<Unit>

    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") authHeader: String): UserResp

    @GET("labs/{id}")
    suspend fun getLab(@Header("Authorization") authHeader: String, @Path("id") id: Long): LabResp
}


package sg.edu.ntu.scse.labattendancesystem.network.models

import com.squareup.moshi.Json

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class User(
    val id: Long = 0,
    val username: String = "",
    @Json(name="display_name")
    val displayName: String = "",
    val email: String = "",
)
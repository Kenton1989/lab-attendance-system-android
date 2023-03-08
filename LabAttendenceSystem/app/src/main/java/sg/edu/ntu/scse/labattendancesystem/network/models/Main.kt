package sg.edu.ntu.scse.labattendancesystem.network.models

import com.squareup.moshi.Json

data class Lab (
    val id: Long,
    val username: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "room_count") val roomCount: Int,
)

class Session {
}
package sg.edu.ntu.scse.labattendancesystem.network.models

import com.squareup.moshi.Json

data class UserResp(
    val id: Long? = 0,
    val username: String? = "",
    @Json(name = "display_name") val displayName: String? = "",
)
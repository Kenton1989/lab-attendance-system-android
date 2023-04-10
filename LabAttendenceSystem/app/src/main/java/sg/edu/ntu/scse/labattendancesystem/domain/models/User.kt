package sg.edu.ntu.scse.labattendancesystem.domain.models


data class User(
    val id: Long,
    val username: String,
    val displayName: String,
)

data class Lab(
    val id: Long,
    val username: String,
    val displayName: String,
    val roomCount: Int,
)
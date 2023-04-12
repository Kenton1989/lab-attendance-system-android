package sg.edu.ntu.scse.labattendancesystem.domain.models


data class User(
    val id: Int,
    val username: String,
    val displayName: String,
)

data class Lab(
    val id: Int,
    val username: String,
    val displayName: String,
    val roomCount: Int,
)
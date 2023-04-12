package sg.edu.ntu.scse.labattendancesystem.network.models

import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import java.time.LocalDateTime

data class LabResp (
    val id: Int?,
    val username: String?,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "room_count") val roomCount: Int?,
)

data class CourseResp(val id: Int?, val code: String?, val title: String?)

data class GroupResp(
    val id: Int?,
    val name: String?,
    val course: CourseResp?,
    val lab: LabResp?,
    @Json(name = "room_no") val roomNo: Int?,
    val teachers: List<UserResp>?,
)

data class GroupStudentResp(
    val id: Int?,
    val group: GroupResp?,
    val student: UserResp?,
    val seat: String?,
)

data class SessionResp (
    val id: Int?,
    val group: GroupResp?,
    @Json(name = "start_datetime") val startTime: LocalDateTime?,
    @Json(name = "end_datetime") val endTime: LocalDateTime?,
)

data class MakeUpSessionResp(
    val id: Int?,
    val user: UserResp?,
    val originalSession: SessionResp?,
    val makeUpSession: SessionResp?,
)

data class AttendanceResp(
    val id: Int?,
    val session: SessionResp?,
    val attender: UserResp?,
    @Json(name = "check_in_state")val checkInState: String?,
    @Json(name = "check_in_datetime") val checkInDatetime: LocalDateTime?,
    @Json(name = "last_modify") val lastModify: LocalDateTime?,
)

data class NewAttendanceReq(
    @Json(name = "session_id") val sessionId: Int,
    @Json(name = "attender_id") val attenderId: Int,
    @Json(name = "check_in_state")val checkInState: String?,
    @Json(name = "check_in_datetime") val checkInDatetime: LocalDateTime?,
    @Json(name = "last_modify") val lastModify: LocalDateTime?,
)
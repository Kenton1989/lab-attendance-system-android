package sg.edu.ntu.scse.labattendancesystem.network.models

import com.squareup.moshi.Json
import java.time.LocalDateTime

data class LabResp (
    val id: Long?,
    val username: String?,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "room_count") val roomCount: Int?,
)

data class CourseResp(val id: Long?, val code: String?, val title: String?)

data class GroupResp(
    val id: Long?,
    val name: String?,
    val course: CourseResp?,
    val lab: LabResp?,
    @Json(name = "room_no") val roomNo: Int?,
    val teachers: List<UserResp>?,
)

data class GroupStudentResp(
    val id: Long?,
    val group: GroupResp?,
    val student: UserResp?,
    val seat: String?,
)

data class SessionResp (
    val id: Long?,
    val group: GroupResp?,
    @Json(name = "start_datetime") val startTime: LocalDateTime?,
    @Json(name = "end_datetime") val endTime: LocalDateTime?,
)

data class AttendanceResp(
    val id: Long?,
    val session: SessionResp?,
    val attender: UserResp?,
    @Json(name = "check_in_state")val checkInState: String?,
    @Json(name = "check_in_datetime") val checkInDatetime: LocalDateTime?,
    @Json(name = "last_modify") val lastModify: LocalDateTime?,
)

data class NewAttendanceReq(
    @Json(name = "session_id") val sessionId: Long,
    @Json(name = "attender_id") val attenderId: Long,
    @Json(name = "check_in_state")val checkInState: String?,
    @Json(name = "check_in_datetime") val checkInDatetime: LocalDateTime?,
    @Json(name = "last_modify") val lastModify: LocalDateTime?,
)
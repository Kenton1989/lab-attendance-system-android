package sg.edu.ntu.scse.labattendancesystem.network.models

import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import java.time.ZonedDateTime

data class PaginatedListResp<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>,
)

data class LabResp(
    val id: Int?,
    val username: String?,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "room_count") val roomCount: Int?,
) {
    fun toUserResp(): UserResp = UserResp(
        id = id,
        username = username,
        displayName = displayName,
    )
}

data class CourseResp(val id: Int?, val code: String?, val title: String?)

data class GroupResp(
    val id: Int?,
    val name: String?,
    val course: CourseResp?,
    val courseId: Int?,
    val lab: LabResp?,
    val labId: Int?,
    @Json(name = "room_no") val roomNo: Int?,
    val teachers: List<UserResp>?,
    @Json(name = "teacher_ids") val teacherIds: List<Int>?,
)

data class GroupStudentResp(
    val id: Int?,
    val group: GroupResp?,
    @Json(name = "group_id") val groupId: Int?,
    val student: UserResp?,
    @Json(name = "student_id") val studentId: Int?,
    val seat: String?,
)

data class SessionResp(
    val id: Int?,
    val group: GroupResp?,
    val groupId: Int?,
    @Json(name = "start_datetime") val startTime: ZonedDateTime?,
    @Json(name = "end_datetime") val endTime: ZonedDateTime?,
    @Json(name = "is_compulsory") val isCompulsory: Boolean?,
    @Json(name = "allow_late_check_in") val allowLateCheckIn: Boolean?,
    @Json(name = "check_in_deadline_mins") val checkInDeadlineMinutes: Int?,
)

data class MakeUpSessionResp(
    val id: Int?,
    val user: UserResp?,
    @Json(name = "original_session") val originalSession: SessionResp?,
    @Json(name = "original_session_id") val originalSessionId: Int?,
    @Json(name = "make_up_session") val makeUpSession: SessionResp?,
    @Json(name = "make_up_session_id") val makeUpSessionId: Int?,
)

data class AttendanceResp(
    val id: Int?,
    val session: SessionResp?,
    @Json(name = "session_id") val sessionId: Int?,
    val attender: UserResp?,
    @Json(name = "attender_id") val attenderId: Int?,
    @Json(name = "check_in_state") val checkInState: String?,
    @Json(name = "check_in_datetime") val checkInDatetime: ZonedDateTime?,
    @Json(name = "last_modify") val lastModify: ZonedDateTime?,
)

data class NewAttendanceReq(
    @Json(name = "session_id") val sessionId: Int,
    @Json(name = "attender_id") val attenderId: Int,
    @Json(name = "check_in_state") val checkInState: String?,
    @Json(name = "check_in_datetime") val checkInDatetime: ZonedDateTime?,
    @Json(name = "last_modify") val lastModify: ZonedDateTime?,
)
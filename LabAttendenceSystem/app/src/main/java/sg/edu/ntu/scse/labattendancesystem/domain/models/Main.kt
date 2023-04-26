package sg.edu.ntu.scse.labattendancesystem.domain.models

import java.time.OffsetDateTime

data class Course(val id: Int, val code: String, val title: String)

data class GroupStudent(
    val id: Int,
    val group: Group,
    val student: User,
    val seat: String,
)

data class Group(
    val id: Int,
    val name: String,
    val course: Course,
    val lab: Lab,
    val roomNo: Int? = null,

    val students: List<GroupStudent>? = null,
    val teachers: List<User>? = null,
)

data class Session(
    val id: Int, val group: Group, val startTime: OffsetDateTime, val endTime: OffsetDateTime
)

enum class AttendanceState(val value: String) {
    ABSENT("absent"), ATTEND("attend"), LATE("late");

    override fun toString() = value

    fun isAbsent() = this == ABSENT

    companion object {
        fun fromValue(value: String) = when (value) {
            ABSENT.value -> ABSENT;
            ATTEND.value -> ATTEND;
            LATE.value -> LATE;
            else -> throw IllegalArgumentException("unknown attendance state value")
        }
    }
}

data class Attendance(
    val id: Int,
    val session: Session,
    val attender: User,
    val seat: String?,
    val checkInState: AttendanceState,
    val checkInDatetime: OffsetDateTime,
    val lastModify: OffsetDateTime,
)

data class NewAttendance(
    val sessionId: Int,
    val attenderId: Int,
    val checkInState: String,
    val checkInDatetime: OffsetDateTime,
    val lastModify: OffsetDateTime,
)
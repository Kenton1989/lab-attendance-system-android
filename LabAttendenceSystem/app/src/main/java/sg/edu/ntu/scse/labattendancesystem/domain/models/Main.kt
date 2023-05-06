package sg.edu.ntu.scse.labattendancesystem.domain.models

import java.time.ZonedDateTime

data class Course(val id: Int, val code: String, val title: String)

data class GroupStudent(
    val id: Int,
    val group: Group?,
    val groupId: Int,
    val student: User,
    val seat: String?,
)

data class Group(
    val id: Int,
    val name: String,
    val course: Course,
    val lab: Lab?,
    val labId: Int,
    val roomNo: Int? = null,

    val students: List<GroupStudent>? = null,
    val teachers: List<User>? = null,
)

data class Session(
    val id: Int,
    val group: Group,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val isCompulsory: Boolean,
    val allowLateCheckIn: Boolean,
    val checkInDeadlineMinutes: Int,
)

enum class AttendanceState(val value: String) {
    ABSENT("absent"), ATTEND("attend"), LATE("late");

    override fun toString() = value

    fun isAbsent() = this == ABSENT

    companion object {
        fun fromValue(value: String) = when (value) {
            ABSENT.value -> ABSENT
            ATTEND.value -> ATTEND
            LATE.value -> LATE
            else -> throw IllegalArgumentException("unknown attendance state value")
        }
    }
}

data class Attendance(
//    val localId: Int,
    val id: Int?,
    val session: Session?,
    val sessionId: Int,
    val attender: User,
    val seat: String?,
    val checkInState: AttendanceState,
    val checkInDatetime: ZonedDateTime?,
    val lastModify: ZonedDateTime,
)
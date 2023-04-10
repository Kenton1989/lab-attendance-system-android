package sg.edu.ntu.scse.labattendancesystem.domain.models

import java.time.LocalDateTime

data class Course(val id: Long, val code: String, val title: String)

data class GroupStudent(
    val id: Long,
    val group: Group,
    val student: User,
    val seat: String,
)

data class Group(
    val id: Long,
    val name: String,
    val course: Course,
    val lab: Lab,
    val roomNo: Int? = null,

    val students: List<GroupStudent>? = null,
    val teachers: List<User>? = null,
)

data class Session(
    val id: Long,
    val group: Group,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

data class Attendance(
    val id: Long,
    val session: Session,
    val attender: User,
    val checkInState: String,
    val checkInDatetime: LocalDateTime,
    val lastModify: LocalDateTime,
)

data class NewAttendance(
    val sessionId: Long,
    val attenderId: Long,
    val checkInState: String,
    val checkInDatetime: LocalDateTime,
    val lastModify: LocalDateTime,
)
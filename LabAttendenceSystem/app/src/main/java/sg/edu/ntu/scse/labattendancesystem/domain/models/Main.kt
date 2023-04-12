package sg.edu.ntu.scse.labattendancesystem.domain.models

import java.time.LocalDateTime

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
    val id: Int,
    val group: Group,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

data class Attendance(
    val id: Int,
    val session: Session,
    val attender: User,
    val checkInState: String,
    val checkInDatetime: LocalDateTime,
    val lastModify: LocalDateTime,
)

data class NewAttendance(
    val sessionId: Int,
    val attenderId: Int,
    val checkInState: String,
    val checkInDatetime: LocalDateTime,
    val lastModify: LocalDateTime,
)
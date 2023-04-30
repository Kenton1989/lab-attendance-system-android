package sg.edu.ntu.scse.labattendancesystem.domain.models

import androidx.room.PrimaryKey
import sg.edu.ntu.scse.labattendancesystem.network.models.*
import sg.edu.ntu.scse.labattendancesystem.database.models.*

fun UserResp.toDatabaseModel() = DbUser(
    id = id!!,
    username = username!!,
    displayName = displayName!!,
)

fun LabResp.toDatabaseModel() = DbLab(
    id = id!!,
    roomCount = roomCount!!,
)

fun CourseResp.toDatabaseModel() = DbCourse(
    id = id!!,
    code = code!!,
    title = title!!,
)

fun GroupResp.toDatabaseModel() = DbGroup(
    id = id!!,
    name = name!!,
    courseId = course!!.id!!,
    labId = lab!!.id!!,
    roomNo = roomNo!!,
)

fun GroupResp.toDbGroupTeachers(): List<DbGroupTeacher> {
    if (teachers != null)
        return teachers.map {
            DbGroupTeacher(
                groupId = this.id!!,
                teacherId = it.id!!,
            )
        }
    else if (teacherIds != null)
        return teacherIds.map {
            DbGroupTeacher(
                groupId = this.id!!,
                teacherId = it,
            )
        }
    else
        throw NullPointerException("expecting not-null teachers or teacherIds")
}

fun GroupStudentResp.toDatabaseModel() = DbGroupStudent(
    id = id!!,
    studentId = student!!.id!!,
    groupId = group!!.id!!,
    seat = seat,
)

fun SessionResp.toDatabaseModel() = DbSession(
    id = id!!,
    groupId = group!!.id!!,
    startTime = startTime!!,
    endTime = endTime!!,
    isCompulsory = isCompulsory!!,
    allowLateCheckIn = allowLateCheckIn!!,
    checkInDeadlineMinutes = checkInDeadlineMinutes!!,
)

fun AttendanceResp.toDbStudentAttendance() = DbStudentAttendance(
    id = id!!,
    sessionId = session!!.id!!,
    attenderId = attender!!.id!!,
    checkInState = checkInState!!,
    checkInDatetime = checkInDatetime!!,
    lastModify = lastModify!!,
)

fun AttendanceResp.toDbTeacherAttendance() = DbStudentAttendance(
    id = id!!,
    sessionId = session!!.id!!,
    attenderId = attender!!.id!!,
    checkInState = checkInState!!,
    checkInDatetime = checkInDatetime!!,
    lastModify = lastModify!!,
)
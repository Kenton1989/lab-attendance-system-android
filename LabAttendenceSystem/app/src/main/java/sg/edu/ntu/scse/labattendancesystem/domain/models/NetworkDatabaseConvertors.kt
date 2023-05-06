package sg.edu.ntu.scse.labattendancesystem.domain.models

import sg.edu.ntu.scse.labattendancesystem.network.models.*
import sg.edu.ntu.scse.labattendancesystem.database.models.*
import java.time.Duration

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
    courseId = course?.id ?: courseId!!,
    labId = lab!!.id!!,
    roomNo = roomNo,
)

fun GroupStudentResp.toDatabaseModel() = DbGroupStudent(
    id = id!!,
    studentId = student?.id ?: studentId!!,
    groupId = group?.id ?: groupId!!,
    seat = seat,
)

fun SessionResp.toDatabaseModel() = DbSession(
    id = id!!,
    groupId = group?.id ?: groupId!!,
    startTime = startTime!!,
    endTime = endTime!!,
    isCompulsory = isCompulsory!!,
    allowLateCheckIn = allowLateCheckIn!!,
    checkInDeadlineMinutes = checkInDeadlineMinutes!!,
)

val LAST_MODIFY_SHIFT: Duration = Duration.ofSeconds(1)

fun AttendanceResp.toDbStudentAttendance() = DbStudentAttendance(
    id = id!!,
    sessionId = session?.id ?: sessionId!!,
    attenderId = attender?.id ?: attenderId!!,
    checkInState = checkInState!!,
    checkInDatetime = checkInDatetime,
    lastModify = lastModify!!,
)

fun DbStudentAttendance.toUpdateAttendanceReq() = UpdateAttendanceReq(
    sessionId = sessionId,
    attenderId = attenderId,
    checkInState = checkInState,
    checkInDatetime = checkInDatetime,
    lastModify = lastModify,
)

fun DbStudentAttendance.toNewAttendanceReq() = NewAttendanceReq(
    sessionId = sessionId,
    attenderId = attenderId,
    checkInState = checkInState,
    checkInDatetime = checkInDatetime,
    lastModify = lastModify,
)

fun AttendanceResp.toDbTeacherAttendance() = DbTeacherAttendance(
    id = id!!,
    sessionId = session?.id ?: sessionId!!,
    attenderId = attender?.id ?: attenderId!!,
    checkInState = checkInState!!,
    checkInDatetime = checkInDatetime,
    lastModify = lastModify!!,
)

fun DbTeacherAttendance.toUpdateAttendanceReq() = UpdateAttendanceReq(
    sessionId = sessionId,
    attenderId = attenderId,
    checkInState = checkInState,
    checkInDatetime = checkInDatetime,
    lastModify = lastModify,
)

fun DbTeacherAttendance.toNewAttendanceReq() = NewAttendanceReq(
    sessionId = sessionId,
    attenderId = attenderId,
    checkInState = checkInState,
    checkInDatetime = checkInDatetime,
    lastModify = lastModify,
)
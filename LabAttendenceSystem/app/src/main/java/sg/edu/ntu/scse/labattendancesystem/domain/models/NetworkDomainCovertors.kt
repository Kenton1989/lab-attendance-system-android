package sg.edu.ntu.scse.labattendancesystem.domain.models

import sg.edu.ntu.scse.labattendancesystem.network.models.*

fun UserResp.toDomainModel() = User(
    id = id!!,
    username = username!!,
    displayName = displayName!!,
)

fun LabResp.toDomainModel() = Lab(
    id = id!!,
    username = username!!,
    displayName = displayName!!,
    roomCount = roomCount!!,
)

fun CourseResp.toDomainModel() = Course(
    id = id!!,
    code = code!!,
    title = title!!,
)

fun GroupResp.toDomainModel() = Group(
    id = id!!,
    name = name!!,
    course = course!!.toDomainModel(),
    lab = lab!!.toDomainModel(),
    roomNo = roomNo,

    students = null,
    teachers = teachers?.map { it.toDomainModel() },
)

fun SessionResp.toDomainModel() = Session(
    id = id!!,
    group = group!!.toDomainModel(),
    startTime = startTime!!,
    endTime = endTime!!,
)

fun AttendanceResp.toDomainModel() = Attendance(
    id = id!!,
    session = session!!.toDomainModel(),
    attender = attender!!.toDomainModel(),
    checkInState = checkInState!!,
    checkInDatetime = checkInDatetime!!,
    lastModify = lastModify!!,
)

fun NewAttendance.toNetworkModel() = NewAttendanceReq(
    sessionId = sessionId,
    attenderId = attenderId,
    checkInState = checkInState,
    checkInDatetime = checkInDatetime,
    lastModify = lastModify,
)
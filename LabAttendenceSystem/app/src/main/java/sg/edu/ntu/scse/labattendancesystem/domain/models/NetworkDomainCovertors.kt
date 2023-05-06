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
    labId = lab.id!!,
    roomNo = roomNo,

    students = null,
    teachers = teachers?.map { it.toDomainModel() },
)

fun SessionResp.toDomainModel() = Session(
    id = id!!,
    group = group!!.toDomainModel(),
    startTime = startTime!!,
    endTime = endTime!!,
    isCompulsory = isCompulsory!!,
    allowLateCheckIn = allowLateCheckIn!!,
    checkInDeadlineMinutes = checkInDeadlineMinutes!!,
)

fun AttendanceResp.toDomainModel() = Attendance(
//    localId = -1,
    id = id!!,
    session = session!!.toDomainModel(),
    sessionId = session.id!!,
    attender = attender!!.toDomainModel(),
    seat = null,
    checkInState = AttendanceState.fromValue(checkInState!!),
    checkInDatetime = checkInDatetime,
    lastModify = lastModify!!,
)
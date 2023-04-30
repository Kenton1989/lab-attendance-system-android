package sg.edu.ntu.scse.labattendancesystem.domain.models

import sg.edu.ntu.scse.labattendancesystem.database.models.*

fun DbUser.toDomainModel(): User =
    User(
        id = id,
        username = username,
        displayName = displayName,
    )

fun StudentAttendanceWithAttender.toDomainModel(): Attendance =
    Attendance(
        localId = data.localId,
        id = data.id,
        session = null,
        sessionId = data.sessionId,
        attender = attender.toDomainModel(),
        seat = null,
        checkInState = AttendanceState.fromValue(data.checkInState),
        checkInDatetime = data.checkInDatetime,
        lastModify = data.lastModify,
    )

fun TeacherAttendanceWithAttender.toDomainModel(): Attendance =
    Attendance(
        localId = data.localId,
        id = data.id,
        session = null,
        sessionId = data.sessionId,
        attender = attender.toDomainModel(),
        seat = null,
        checkInState = AttendanceState.fromValue(data.checkInState),
        checkInDatetime = data.checkInDatetime,
        lastModify = data.lastModify,
    )

fun DbCourse.toDomainModel(): Course =
    Course(
        id = id,
        code = code,
        title = title,
    )

fun GroupWithCourse.toDomainModel(): Group =
    Group(
        id = data.id,
        name = data.name,
        course = course.toDomainModel(),
        lab = null,
        labId = data.labId,
        roomNo = data.roomNo,
    )

fun GroupStudentWithUser.toDomainModel(): GroupStudent =
    GroupStudent(
        id = data.id,
        group = null,
        groupId = data.groupId,
        student = student.toDomainModel(),
        seat = data.seat,
    )

fun GroupWithDetails.toDomainModel(): Group =
    Group(
        id = data.id,
        name = data.name,
        course = course.toDomainModel(),
        lab = null,
        labId = data.labId,
        roomNo = data.roomNo,
        teachers = teachers.map { it.toDomainModel() },
        students = students.map { it.toDomainModel() },
    )

fun SessionWithCourseGroup.toDomainModel() = Session(
    id = data.id,
    group = group.toDomainModel(),
    startTime = data.startTime,
    endTime = data.endTime,
    isCompulsory = data.isCompulsory,
    allowLateCheckIn = data.allowLateCheckIn,
    checkInDeadlineMinutes = data.checkInDeadlineMinutes,
)

fun SessionWithGroupDetails.toDomainModel() = Session(
    id = data.id,
    group = group.toDomainModel(),
    startTime = data.startTime,
    endTime = data.endTime,
    isCompulsory = data.isCompulsory,
    allowLateCheckIn = data.allowLateCheckIn,
    checkInDeadlineMinutes = data.checkInDeadlineMinutes,
)

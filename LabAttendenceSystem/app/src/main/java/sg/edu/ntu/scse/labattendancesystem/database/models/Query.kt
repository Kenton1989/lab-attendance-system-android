package sg.edu.ntu.scse.labattendancesystem.database.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import java.time.LocalDateTime


data class GroupWithCourse(
    @Embedded val data: DbGroup,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: DbCourse,
)

data class SessionWithCourseGroup(
    @Embedded val data: DbSession,
    @Relation(
        entity = DbGroup::class,
        parentColumn = "groupId",
        entityColumn = "id",
    )
    val group: GroupWithCourse,
)

data class GroupStudentWithUser(
    @Embedded val data: DbGroupStudent,
    @Relation(
        parentColumn = "studentId",
        entityColumn = "id",
    )
    val student: DbUser,
)

data class GroupWithDetails(
    @Embedded val data: DbGroup,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: DbCourse,

    @Relation(
        entity = DbGroupStudent::class,
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val students: List<GroupStudentWithUser>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            DbGroupTeacher::class,
            parentColumn = "groupId", entityColumn = "teacherId"
        ),
    )
    val teachers: List<DbUser>
)

data class MakeUpSessionWithOriginalSessionAndStudent(
    @Embedded val data: DbMakeUpSession,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id",
    )
    val user: DbUser,
    @Relation(
        parentColumn = "originalSessionId",
        entityColumn = "id",
    )
    val originalSessionId: DbSession,
)

data class SessionWithGroupDetailsAndMakeUps(
    @Embedded val data: DbSession,
    @Relation(
        entity = DbGroup::class,
        parentColumn = "groupId",
        entityColumn = "id",
    )
    val group: GroupWithDetails,
    @Relation(
        entity = DbMakeUpSession::class,
        parentColumn = "id",
        entityColumn = "makeUpSessionId",
    )
    val makeUpFor: MakeUpSessionWithOriginalSessionAndStudent
)

data class StudentAttendanceWithAttender(
    @Embedded val data: DbStudentAttendance,
    @Relation(
        parentColumn = "attenderId",
        entityColumn = "id",
    )
    val attender: DbUser,
)

data class TeacherAttendanceWithAttender(
    @Embedded val data: DbStudentAttendance,
    @Relation(
        parentColumn = "attenderId",
        entityColumn = "id",
    )
    val attender: DbUser,
)
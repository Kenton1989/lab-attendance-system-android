package sg.edu.ntu.scse.labattendancesystem.database.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import java.time.ZonedDateTime

data class LabWithUser(
    @Embedded val data: DbLab,
    @Relation(
        parentColumn = "l_id",
        entityColumn = "u_id"
    )
    val user: DbUser,

)

data class GroupWithCourse(
    @Embedded val data: DbGroup,
    @Relation(
        parentColumn = "g_course_id",
        entityColumn = "c_id"
    )
    val course: DbCourse,
)

data class SessionWithCourseGroup(
    @Embedded val data: DbSession,
    @Relation(
        entity = DbGroup::class,
        parentColumn = "s_group_id",
        entityColumn = "g_id",
    )
    val group: GroupWithCourse,
)

data class GroupStudentWithUser(
    @Embedded val data: DbGroupStudent,
    @Relation(
        parentColumn = "gs_student_id",
        entityColumn = "u_id",
    )
    val student: DbUser,
)

data class GroupWithDetails(
    @Embedded val data: DbGroup,
    @Relation(
        parentColumn = "g_course_id",
        entityColumn = "c_id"
    )
    val course: DbCourse,

    @Relation(
        entity = DbGroupStudent::class,
        parentColumn = "g_id",
        entityColumn = "gs_group_id"
    )
    val students: List<GroupStudentWithUser>,

    @Relation(
        parentColumn = "g_id",
        entityColumn = "u_id",
        associateBy = Junction(
            DbGroupTeacher::class,
            parentColumn = "gt_group_id", entityColumn = "gt_teacher_id"
        ),
    )
    val teachers: List<DbUser>
)

data class MakeUpSessionWithOriginalSessionAndStudent(
    @Embedded val data: DbMakeUpSession,
    @Relation(
        parentColumn = "ms_user_id",
        entityColumn = "u_id",
    )
    val user: DbUser,
    @Relation(
        parentColumn = "ms_original_session_id",
        entityColumn = "s_id",
    )
    val originalSession: DbSession,
)

data class SessionWithGroupDetails(
    @Embedded val data: DbSession,
    @Relation(
        entity = DbGroup::class,
        parentColumn = "s_group_id",
        entityColumn = "g_id",
    )
    val group: GroupWithDetails,
)

data class SessionWithGroupDetailsAndMakeUps(
    @Embedded val data: DbSession,
    @Relation(
        entity = DbGroup::class,
        parentColumn = "s_group_id",
        entityColumn = "g_id",
    )
    val group: GroupWithDetails,
    @Relation(
        entity = DbMakeUpSession::class,
        parentColumn = "s_id",
        entityColumn = "ms_make_up_session_id",
    )
    val makeUpFor: MakeUpSessionWithOriginalSessionAndStudent
)

data class StudentAttendanceWithAttender(
    @Embedded val data: DbStudentAttendance,
    @Relation(
        parentColumn = "sa_attender_id",
        entityColumn = "u_id",
    )
    val attender: DbUser,
)

data class TeacherAttendanceWithAttender(
    @Embedded val data: DbTeacherAttendance,
    @Relation(
        parentColumn = "ta_attender_id",
        entityColumn = "u_id",
    )
    val attender: DbUser,
)
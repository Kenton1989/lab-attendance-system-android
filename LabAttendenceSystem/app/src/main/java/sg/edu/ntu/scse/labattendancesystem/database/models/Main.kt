package sg.edu.ntu.scse.labattendancesystem.database.models

import androidx.room.*
import java.time.ZonedDateTime


@Entity(
    tableName = "user_tb",
    indices = [Index("u_username", unique = true)],
)
data class DbUser(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "u_id") val id: Int,

    @ColumnInfo(name = "u_username") val username: String,
    @ColumnInfo(name = "u_display_name") val displayName: String
)


@Entity(
    tableName = "lab_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["u_id"],
            childColumns = ["l_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class DbLab(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "l_id") val id: Int,

    @ColumnInfo(name = "l_room_count") val roomCount: Int
)


@Entity(
    tableName = "course_tb",
    indices = [Index("c_code", unique = true)],
)
data class DbCourse(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "c_id") val id: Int,

    @ColumnInfo(name = "c_code") val code: String,
    @ColumnInfo(name = "c_title") val title: String,
)

@Entity(
    tableName = "group_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbCourse::class,
            parentColumns = ["c_id"],
            childColumns = ["g_course_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbLab::class,
            parentColumns = ["l_id"],
            childColumns = ["g_lab_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("g_course_id", "g_name", unique = true),
        Index("g_room_no"),
        Index("g_lab_id", "g_room_no")],
)

data class DbGroup(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "g_id") val id: Int,

    @ColumnInfo(name = "g_course_id") val courseId: Int,
    @ColumnInfo(name = "g_lab_id") val labId: Int,
    @ColumnInfo(name = "g_room_no") val roomNo: Int?,
    @ColumnInfo(name = "g_name") val name: String,
)


@Entity(
    tableName = "group_student_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["u_id"],
            childColumns = ["gs_student_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["g_id"],
            childColumns = ["gs_group_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("gs_group_id", "gs_student_id", unique = true), Index("gs_student_id")],
)
data class DbGroupStudent(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "gs_id") val id: Int,
    @ColumnInfo(name = "gs_student_id") val studentId: Int,
    @ColumnInfo(name = "gs_group_id") val groupId: Int,
    @ColumnInfo(name = "gs_seat") val seat: String?,
)


@Entity(
    tableName = "group_teacher_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["u_id"],
            childColumns = ["gt_teacher_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["g_id"],
            childColumns = ["gt_group_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    primaryKeys = ["gt_group_id", "gt_teacher_id"],
    indices = [Index("gt_teacher_id")],
)
data class DbGroupTeacher(
    @ColumnInfo(name = "gt_teacher_id") val teacherId: Int,
    @ColumnInfo(name = "gt_group_id") val groupId: Int,
)


@Entity(
    tableName = "session_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["g_id"],
            childColumns = ["s_group_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("s_group_id", "s_start_datetime", unique = true)],
)
data class DbSession(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "s_id") val id: Int,

    @ColumnInfo(name = "s_group_id") val groupId: Int,
    @ColumnInfo(name = "s_start_datetime") val startTime: ZonedDateTime,
    @ColumnInfo(name = "s_end_datetime") val endTime: ZonedDateTime,
    @ColumnInfo(name = "s_is_compulsory") val isCompulsory: Boolean,
    @ColumnInfo(name = "s_allow_late_check_in") val allowLateCheckIn: Boolean,
    @ColumnInfo(name = "s_check_in_deadline_minutes") val checkInDeadlineMinutes: Int,
)


@Entity(
    tableName = "make_up_session_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbSession::class,
            parentColumns = ["s_id"],
            childColumns = ["ms_original_session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbSession::class,
            parentColumns = ["s_id"],
            childColumns = ["ms_make_up_session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["u_id"],
            childColumns = ["ms_user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("ms_original_session_id", "ms_user_id", unique = true),
        Index("ms_make_up_session_id", "ms_user_id", unique = true),
        Index("ms_user_id"),
    ],
)
data class DbMakeUpSession(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "ms_id") val id: Int,

    @ColumnInfo(name = "ms_user_id") val userId: Int,
    @ColumnInfo(name = "ms_original_session_id") val originalSessionId: Int,
    @ColumnInfo(name = "ms_make_up_session_id") val makeUpSessionId: Int,
)


@Entity(
    tableName = "student_attendance_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbSession::class,
            parentColumns = ["s_id"],
            childColumns = ["sa_session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["u_id"],
            childColumns = ["sa_attender_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("sa_session_id", "sa_attender_id", unique = true),
        Index("sa_attender_id"), Index("sa_last_modify"),
    ],
)
data class DbStudentAttendance(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sa_local_id") val localId: Int = 0,
    @ColumnInfo(name = "sa_id") val id: Int?,
    @ColumnInfo(name = "sa_session_id") val sessionId: Int,
    @ColumnInfo(name = "sa_attender_id") val attenderId: Int,
    @ColumnInfo(name = "sa_check_in_state") val checkInState: String,
    @ColumnInfo(name = "sa_check_in_datetime") val checkInDatetime: ZonedDateTime,
    @ColumnInfo(name = "sa_last_modify") val lastModify: ZonedDateTime,
)


@Entity(
    tableName = "teacher_attendance_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbSession::class,
            parentColumns = ["s_id"],
            childColumns = ["ta_session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["u_id"],
            childColumns = ["ta_attender_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("ta_session_id", "ta_attender_id", unique = true),
        Index("ta_attender_id"), Index("ta_last_modify"),
    ],
)
data class DbTeacherAttendance(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ta_local_id") val localId: Int = 0,
    @ColumnInfo(name = "ta_id") val id: Int?,
    @ColumnInfo(name = "ta_session_id") val sessionId: Int,
    @ColumnInfo(name = "ta_attender_id") val attenderId: Int,
    @ColumnInfo(name = "ta_check_in_state") val checkInState: String,
    @ColumnInfo(name = "ta_check_in_datetime") val checkInDatetime: ZonedDateTime,
    @ColumnInfo(name = "ta_last_modify") val lastModify: ZonedDateTime,
)
package sg.edu.ntu.scse.labattendancesystem.database.models

import androidx.room.*
import java.time.LocalDateTime


@Entity(
    tableName = "user_tb",
    indices = [Index("username", unique = true)],
)
data class DbUser(
    @PrimaryKey(autoGenerate = false) val id: Int,

    val username: String, val displayName: String
)


@Entity(
    tableName = "lab_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class DbLab(
    @PrimaryKey(autoGenerate = false) val id: Int, val roomCount: Int
)


@Entity(
    tableName = "course_tb",
    indices = [Index("code", unique = true)],
)
data class DbCourse(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val code: String,
    val title: String,
)

@Entity(
    tableName = "group_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbCourse::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbLab::class,
            parentColumns = ["id"],
            childColumns = ["labId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("courseId", "name", unique = true), Index("room"), Index("labId")],
)

data class DbGroup(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val courseId: Int,
    val labId: Int,
    val room: Int?,
    val name: String,
    val title: String,
)


@Entity(
    tableName = "group_student_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("groupId", "studentId", unique = true), Index("studentId")],
)
data class DbGroupStudent(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val studentId: Int,
    val groupId: Int,
    val seat: String?,
)


@Entity(
    tableName = "group_teacher_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["id"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    primaryKeys = ["groupId", "teacherId"],
    indices = [Index("teacherId")],
)
data class DbGroupTeacher(
    val teacherId: Int,
    val groupId: Int,
)


@Entity(
    tableName = "session_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("groupId", unique = true)],
)
data class DbSession(
    @PrimaryKey(autoGenerate = false) val id: Int,

    val groupId: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isCompulsory: Boolean,
    val allowLateCheckIn: Boolean,
    val checkInDeadlineMinutes: Int,
)


@Entity(
    tableName = "make_up_session_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbSession::class,
            parentColumns = ["id"],
            childColumns = ["originalSessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbSession::class,
            parentColumns = ["id"],
            childColumns = ["makeUpSessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("originalSessionId", "userId", unique = true),
        Index("makeUpSessionId", "userId", unique = true),
        Index("userId"),
    ],
)
data class DbMakeUpSession(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val userId: Int,
    val originalSessionId: Int,
    val makeUpSessionId: Int,
)


@Entity(
    tableName = "student_attendance_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["id"],
            childColumns = ["attenderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("sessionId", "attenderId", unique = true),
        Index("attenderId"), Index("lastModify"),
    ],
)
data class DbStudentAttendance(
    @PrimaryKey val localId: Int,
    val id: Int?,
    val sessionId: Int,
    val attenderId: Int,
    val checkInState: String,
    val checkInDatetime: LocalDateTime,
    val lastModify: LocalDateTime,
)


@Entity(
    tableName = "teacher_attendance_tb",
    foreignKeys = [
        ForeignKey(
            entity = DbSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbUser::class,
            parentColumns = ["id"],
            childColumns = ["attenderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("sessionId", "attenderId", unique = true),
        Index("attenderId"), Index("lastModify"),
    ],
)
data class DbTeacherAttendance(
    @PrimaryKey(autoGenerate = true) val localId: Int,
    val id: Int?,
    val sessionId: Int,
    val attenderId: Int,
    val checkInState: String,
    val checkInDatetime: LocalDateTime,
    val lastModify: LocalDateTime,
)
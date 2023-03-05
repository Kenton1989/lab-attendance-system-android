package sg.edu.ntu.scse.labattendancesystem.database.models

import androidx.room.*
import java.util.Date

@Entity
data class User(
    @PrimaryKey val id: Long,

    val username: String,
    val displayName: String,
    val email: String
)

@Entity
data class Lab(
    @PrimaryKey val id: Long,

    val username: String,
    val displayName: String,
    val roomCount: Int
)

@Entity
data class Course(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val code: String,
    val title: String,
)

@Entity
data class Group(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val courseId: Long,
    val labId: Long,
    val name: String,
    val title: String,
)

@Entity
data class GroupStudent(
    val studentId: Long,
    val groupId: Long,
    val seat: String,
)

@Entity
data class Session(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val groupId: Long,
    val startTime: Date,
    val endTime: Date,
    val isCompulsory: Boolean,
    val allowLateCheckIn: Boolean,
    val checkInDeadlineMinutes: Int,
)

@Entity
data class MakeUpSession(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val userId: Long,
    val originalSessionId: Long,
    val makeUpSessionId: Long,
)


data class GroupWithCourse(
    @Embedded val group: Group,
    @Relation(
        parentColumn = "id",
        entityColumn = "course_id"
    )
    val course: Course,
)


data class SessionWithCourseGroup(
    @Embedded val session: Session,
    @Relation(
        entity = Group::class,
        parentColumn = "id",
        entityColumn = "group_id",
    )
    val group: GroupWithCourse,
)


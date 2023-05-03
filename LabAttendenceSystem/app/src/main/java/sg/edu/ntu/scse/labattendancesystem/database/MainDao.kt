package sg.edu.ntu.scse.labattendancesystem.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import sg.edu.ntu.scse.labattendancesystem.database.models.*
import java.time.ZonedDateTime

@Dao
interface MainDao {
    @Upsert
    fun insertUsers(models: Collection<DbUser>)

    @Upsert
    fun insertLabs(models: Collection<DbLab>)

    @Query("SELECT *  FROM course_tb;")
    fun getAllCourses(): Flow<List<DbCourse>>

    @Query("SELECT * FROM course_tb WHERE id = :id;")
    fun getCourse(id: Int): Flow<DbCourse>

    @Upsert
    fun insertCourses(models: Collection<DbCourse>)

    @Transaction
    @Query("SELECT * FROM group_tb;")
    fun getAllBriefGroups(): Flow<List<GroupWithCourse>>

    @Transaction
    @Query("SELECT * FROM group_tb WHERE id = :id;")
    fun getBriefGroup(id: Int): Flow<GroupWithCourse>

    @Transaction
    @Query("SELECT * FROM group_tb WHERE id = :id;")
    fun getDetailGroup(id: Int): Flow<GroupWithDetails>

    @Upsert
    fun insertOrUpdateGroups(models: Collection<DbGroup>)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM user_tb JOIN group_teacher_tb ON id = teacherId WHERE groupId = :groupId")
    fun getTeachersOfGroup(groupId: Int): Flow<List<DbUser>>


    @Upsert
    fun insertOrUpdateGroupTeachers(models: Collection<DbGroupTeacher>)

    fun insertOrUpdateGroupTeachers(groupId: Int, teacherId: List<Int>) =
        insertOrUpdateGroupTeachers(teacherId.map {
            DbGroupTeacher(
                groupId = groupId, teacherId = it
            )
        })

    @Transaction
    @Query("SELECT * FROM group_student_tb WHERE groupId = :groupId")
    fun getStudentsOfGroup(groupId: Int): Flow<List<GroupStudentWithUser>>

    @Upsert
    fun insertOrUpdateGroupStudents(models: Collection<DbGroupStudent>)

    @Transaction
    @Query("SELECT * FROM session_tb;")
    fun getAllBriefSessions(): Flow<List<SessionWithCourseGroup>>

    @Transaction
    @Query(
        "SELECT * FROM session_tb " +
                "JOIN group_tb ON groupId = group_tb.id " +
                "WHERE startTime <= :startTimeBefore " +
                "AND :endTimeAfter < endTime " +
                "AND (:roomNo IS NULL OR :roomNo == group_tb.roomNo)" +
                "AND (:labId IS NULL OR :labId == group_tb.labId)"
    )
    fun getActiveBriefSessions(
        labId: Int? = null,
        roomNo: Int? = null,
        endTimeAfter: ZonedDateTime = ZonedDateTime.now(),
        startTimeBefore: ZonedDateTime = ZonedDateTime.now().minusMinutes(15),
    ): Flow<List<SessionWithCourseGroup>>

    @Transaction
    @Query("SELECT * FROM session_tb WHERE id = :id;")
    fun getBriefSession(id: Int): Flow<SessionWithCourseGroup>

    @Transaction
    @Query("SELECT * FROM session_tb WHERE id = :id;")
    fun getDetailSession(id: Int): Flow<SessionWithGroupDetails>

    @Upsert
    fun insertOrUpdateSessions(models: Collection<DbSession>)

    @Transaction
    @Query("SELECT * FROM make_up_session_tb WHERE makeUpSessionId = :makeUpSessionId;")
    fun getMakeUpSessions(makeUpSessionId: Int): Flow<List<MakeUpSessionWithOriginalSessionAndStudent>>

    @Upsert
    fun insertOrUpdateMakeUpSessions(models: Collection<DbMakeUpSession>)

    @Transaction
    @Query("SELECT * FROM student_attendance_tb WHERE id = :id;")
    fun getStudentAttendance(id: Int): Flow<List<StudentAttendanceWithAttender>>

    @Transaction
    @Query("SELECT * FROM student_attendance_tb WHERE sessionId = :sessionId;")
    fun getStudentAttendancesOfSession(sessionId: Int): Flow<List<StudentAttendanceWithAttender>>

    @Upsert
    fun insertOrUpdateStudentAttendances(models: Collection<DbStudentAttendance>)

    @Transaction
    @Query("SELECT * FROM teacher_attendance_tb WHERE id = :id;")
    fun getTeacherAttendance(id: Int): Flow<List<TeacherAttendanceWithAttender>>

    @Transaction
    @Query("SELECT * FROM teacher_attendance_tb WHERE sessionId = :sessionId;")
    fun getTeacherAttendancesOfSession(sessionId: Int): Flow<List<TeacherAttendanceWithAttender>>

    @Upsert
    fun insertOrUpdateTeacherAttendances(models: Collection<DbTeacherAttendance>)
}
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

    @Query("SELECT * FROM course_tb WHERE c_id = :id;")
    fun getCourse(id: Int): Flow<DbCourse?>

    @Upsert
    fun insertCourses(models: Collection<DbCourse>)

    @Transaction
    @Query("SELECT * FROM group_tb;")
    fun getAllBriefGroups(): Flow<List<GroupWithCourse>>

    @Transaction
    @Query("SELECT * FROM group_tb WHERE g_id = :id;")
    fun getBriefGroup(id: Int): Flow<GroupWithCourse?>

    @Transaction
    @Query("SELECT * FROM group_tb WHERE g_id = :id;")
    fun getDetailGroup(id: Int): Flow<GroupWithDetails?>

    @Upsert
    fun insertOrUpdateGroups(models: Collection<DbGroup>)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM user_tb JOIN group_teacher_tb ON u_id = gt_teacher_id WHERE gt_group_id = :groupId")
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
    @Query("SELECT * FROM group_student_tb WHERE gs_group_id = :groupId")
    fun getStudentsOfGroup(groupId: Int): Flow<List<GroupStudentWithUser>>

    @Upsert
    fun insertOrUpdateGroupStudents(models: Collection<DbGroupStudent>)

    @Transaction
    @Query("SELECT * FROM session_tb;")
    fun getAllBriefSessions(): Flow<List<SessionWithCourseGroup>>

    @Transaction
    @Query(
        "SELECT session_tb.* FROM session_tb  JOIN group_tb ON s_group_id = g_id  WHERE s_start_datetime <= :startTimeBefore  AND :endTimeAfter < s_end_datetime  AND (:roomNo IS NULL OR :roomNo == g_room_no) AND (:labId IS NULL OR :labId == g_lab_id)"
    )
    fun getActiveBriefSessions(
        labId: Int? = null,
        roomNo: Int? = null,
        endTimeAfter: ZonedDateTime = ZonedDateTime.now(),
        startTimeBefore: ZonedDateTime = ZonedDateTime.now().minusMinutes(15),
    ): Flow<List<SessionWithCourseGroup>>

    @Query("SELECT EXISTS(SELECT * FROM session_tb WHERE s_id = :id);")
    fun hasSession(id: Int): Flow<Boolean>

    @Transaction
    @Query("SELECT * FROM session_tb WHERE s_id = :id;")
    fun getBriefSession(id: Int): Flow<SessionWithCourseGroup?>

    @Transaction
    @Query("SELECT * FROM session_tb WHERE s_id = :id;")
    fun getDetailSession(id: Int): Flow<SessionWithGroupDetails?>

    @Upsert
    fun insertOrUpdateSessions(models: Collection<DbSession>)

    @Query("DELETE FROM session_tb WHERE s_start_datetime < :minStartTimeInCache")
    fun deleteExpiredSession(minStartTimeInCache: ZonedDateTime)

    @Transaction
    @Query("SELECT * FROM make_up_session_tb WHERE ms_make_up_session_id = :makeUpSessionId;")
    fun getMakeUpSessions(makeUpSessionId: Int): Flow<List<MakeUpSessionWithOriginalSessionAndStudent>>

    @Upsert
    fun insertOrUpdateMakeUpSessions(models: Collection<DbMakeUpSession>)

    @Transaction
    @Query("SELECT * FROM student_attendance_tb WHERE sa_id = :id;")
    fun getStudentAttendance(id: Int): Flow<List<StudentAttendanceWithAttender>>

    @Transaction
    @Query("SELECT * FROM student_attendance_tb WHERE sa_session_id = :sessionId;")
    fun getStudentAttendancesOfSession(sessionId: Int): Flow<List<StudentAttendanceWithAttender>>


    @Transaction
    @Query(
        "SELECT * FROM student_attendance_tb WHERE " +
                "(:id IS NULL OR sa_id = :id) AND" +
                "(:attenderId IS NULL OR sa_attender_id = :attenderId) AND" +
                "(:sessionId IS NULL OR sa_session_id = :sessionId) AND" +
                "(:lastModifyAfter IS NULL OR sa_last_modify > :lastModifyAfter) AND" +
                "(:lastModifyBefore IS NULL OR sa_last_modify < :lastModifyBefore) "
    )
    fun getRawStudentAttendances(
        id: Int? = null,
        attenderId: Int? = null,
        sessionId: Int? = null,
        lastModifyAfter: ZonedDateTime? = null,
        lastModifyBefore: ZonedDateTime? = null,
    ): Flow<List<DbStudentAttendance>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudentAttendances(models: Collection<DbStudentAttendance>): List<Long>

    @Update
    suspend fun updateStudentAttendances(models: Collection<DbStudentAttendance>): Int

    @Upsert
    suspend fun insertOrUpdateStudentAttendances(models: Collection<DbStudentAttendance>)

    @Delete
    suspend fun deleteStudentAttendances(models: Collection<DbStudentAttendance>): Int

    @Transaction
    @Query("SELECT * FROM teacher_attendance_tb WHERE ta_id = :id;")
    fun getTeacherAttendance(id: Int): Flow<List<TeacherAttendanceWithAttender>>

    @Transaction
    @Query("SELECT * FROM teacher_attendance_tb WHERE ta_session_id = :sessionId;")
    fun getTeacherAttendancesOfSession(sessionId: Int): Flow<List<TeacherAttendanceWithAttender>>

    @Transaction
    @Query(
        "SELECT * FROM teacher_attendance_tb WHERE " +
                "(:id IS NULL OR ta_id = :id) AND" +
                "(:attenderId IS NULL OR ta_attender_id = :attenderId) AND" +
                "(:sessionId IS NULL OR ta_session_id = :sessionId) AND" +
                "(:lastModifyAfter IS NULL OR ta_last_modify > :lastModifyAfter) AND" +
                "(:lastModifyBefore IS NULL OR ta_last_modify < :lastModifyBefore) "
    )
    fun getRawTeacherAttendances(
        id: Int? = null,
        attenderId: Int? = null,
        sessionId: Int? = null,
        lastModifyAfter: ZonedDateTime? = null,
        lastModifyBefore: ZonedDateTime? = null,
    ): Flow<List<DbTeacherAttendance>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeacherAttendances(models: Collection<DbTeacherAttendance>): List<Long>

    @Update
    suspend fun updateTeacherAttendances(models: Collection<DbTeacherAttendance>): Int

    @Upsert
    suspend fun insertOrUpdateTeacherAttendances(models: Collection<DbTeacherAttendance>)

    @Delete
    suspend fun deleteTeacherAttendances(models: Collection<DbTeacherAttendance>): Int
}
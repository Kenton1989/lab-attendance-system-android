package sg.edu.ntu.scse.labattendancesystem.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import sg.edu.ntu.scse.labattendancesystem.database.models.*

@Dao
interface MainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg models: DbUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLabs(vararg models: DbLab)

    @Query("SELECT *  FROM course_tb;")
    fun getAllCourses(): Flow<List<DbCourse>>

    @Query("SELECT * FROM course_tb WHERE id = :id;")
    fun getCourse(id: Int): Flow<DbCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourses(vararg models: DbCourse)

    @Transaction
    @Query("SELECT * FROM group_tb;")
    fun getAllBriefGroups(): Flow<List<GroupWithCourse>>

    @Transaction
    @Query("SELECT * FROM group_tb WHERE id = :id;")
    fun getBriefGroup(id: Int): Flow<GroupWithCourse>

    @Transaction
    @Query("SELECT * FROM group_tb WHERE id = :id;")
    fun getDetailGroup(id: Int): Flow<GroupWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroups(vararg models: DbGroup)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM user_tb JOIN group_teacher_tb ON id = teacherId WHERE groupId = :groupId")
    fun getTeachersOfGroup(groupId: Int): Flow<List<DbUser>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupTeachers(vararg models: DbGroupTeacher)

    fun insertGroupTeachers(groupId: Int, vararg teacherId: Int) =
        insertGroupTeachers(*teacherId.map {
            DbGroupTeacher(
                groupId = groupId, teacherId = it
            )
        }.toTypedArray())

    @Transaction
    @Query("SELECT * FROM group_student_tb WHERE groupId = :groupId")
    fun getStudentsOfGroup(groupId: Int): Flow<List<GroupStudentWithUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupStudents(vararg models: DbGroupStudent)

    @Transaction
    @Query("SELECT * FROM session_tb;")
    fun getAllBriefSessions(): Flow<List<SessionWithCourseGroup>>

    @Transaction
    @Query("SELECT * FROM session_tb WHERE id = :id;")
    fun getBriefSessions(id: Int): Flow<List<SessionWithCourseGroup>>

    @Transaction
    @Query("SELECT * FROM session_tb WHERE id = :id;")
    fun getDetailSessions(id: Int): Flow<List<SessionWithGroupDetailsAndMakeUps>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSessions(vararg models: DbSession)

    @Transaction
    @Query("SELECT * FROM make_up_session_tb WHERE makeUpSessionId = :makeUpSessionId;")
    fun getMakeUpSessions(makeUpSessionId: Int): Flow<List<MakeUpSessionWithOriginalSessionAndStudent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMakeUpSessions(vararg models: DbMakeUpSession)

    @Transaction
    @Query("SELECT * FROM student_attendance_tb WHERE id = :id;")
    fun getStudentAttendance(id: Int): Flow<List<StudentAttendanceWithAttender>>

    @Transaction
    @Query("SELECT * FROM student_attendance_tb WHERE sessionId = :sessionId;")
    fun getStudentAttendancesOfSession(sessionId: Int): Flow<List<StudentAttendanceWithAttender>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inseStudentAttendances(vararg models: DbStudentAttendance)

    @Transaction
    @Query("SELECT * FROM teacher_attendance_tb WHERE id = :id;")
    fun getTeacherAttendance(id: Int): Flow<List<TeacherAttendanceWithAttender>>

    @Transaction
    @Query("SELECT * FROM teacher_attendance_tb WHERE sessionId = :sessionId;")
    fun getTeacherAttendancesOfSession(sessionId: Int): Flow<List<TeacherAttendanceWithAttender>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inseTeacherAttendances(vararg models: DbTeacherAttendance)
}
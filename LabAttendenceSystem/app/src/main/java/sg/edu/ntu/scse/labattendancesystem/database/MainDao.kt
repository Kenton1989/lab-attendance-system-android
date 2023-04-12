package sg.edu.ntu.scse.labattendancesystem.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import sg.edu.ntu.scse.labattendancesystem.database.models.*

@Dao
interface MainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg users: DbUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLabs(vararg users: DbLab)

    @Query("SELECT *  FROM course_tb;")
    fun getAllCourses(): Flow<List<DbCourse>>

    @Query("SELECT * FROM course_tb WHERE id = :id;")
    fun getCourse(id: Int): Flow<DbCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourses(vararg courses: DbCourse)

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
    fun insertGroups(vararg groups: DbGroup)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM user_tb JOIN group_teacher_tb ON id = teacherId WHERE groupId = :groupId")
    fun getGroupTeachers(groupId: Int): Flow<List<DbUser>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupTeachers(vararg groupTeachers: DbGroupTeacher)

    fun insertGroupTeachers(groupId: Int, vararg teacherId: Int) =
        insertGroupTeachers(*teacherId.map {
            DbGroupTeacher(
                groupId = groupId,
                teacherId = it
            )
        }.toTypedArray())

    @Transaction
    @Query("SELECT * FROM group_student_tb WHERE groupId = :groupId")
    fun getGroupStudents(groupId: Int): Flow<List<GroupStudentWithUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupStudents(vararg groupStudents: DbGroupStudent)

    @Transaction
    @Query("SELECT * FROM session_tb;")
    fun getAllBriefSessions(): Flow<List<SessionWithCourseGroup>>

    @Transaction
    @Query("SELECT * FROM session_tb WHERE id = :id;")
    fun getBriefSessions(id: Int): Flow<List<SessionWithCourseGroup>>

    @Transaction
    @Query("SELECT * FROM session_tb WHERE id = :id;")
    fun getDetailSessions(id: Int): Flow<List<SessionWithGroupDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSessions(vararg sessions: DbSession)
}
package sg.edu.ntu.scse.labattendancesystem.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sg.edu.ntu.scse.labattendancesystem.database.models.*

@Database(
    version = 2,
    exportSchema = false,
    entities = [
        DbUser::class, DbLab::class, DbCourse::class, DbGroup::class, DbSession::class,
        DbGroupStudent::class, DbGroupTeacher::class, DbMakeUpSession::class,
        DbStudentAttendance::class, DbTeacherAttendance::class,
    ]
)
@TypeConverters(Converters::class)
abstract class LabAttendanceSystemDatabase : RoomDatabase() {
    abstract fun mainDao(): MainDao

    companion object {
        @Volatile
        private var INSTANCE: LabAttendanceSystemDatabase? = null

        fun getDatabase(context: Context): LabAttendanceSystemDatabase {
            return synchronized(this) {
                val instance = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LabAttendanceSystemDatabase::class.java,
                    "lab_attendance_system_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
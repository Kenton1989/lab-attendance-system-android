package sg.edu.ntu.scse.labattendancesystem.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sg.edu.ntu.scse.labattendancesystem.database.models.Course

@Database(entities = [Course::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LabAttendanceSystemDatabase : RoomDatabase() {
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
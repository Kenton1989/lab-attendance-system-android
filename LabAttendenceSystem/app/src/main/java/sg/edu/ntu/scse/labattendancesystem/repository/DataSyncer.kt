package sg.edu.ntu.scse.labattendancesystem.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withTimeout
import sg.edu.ntu.scse.labattendancesystem.database.MainDao
import sg.edu.ntu.scse.labattendancesystem.database.models.DbGroup
import sg.edu.ntu.scse.labattendancesystem.database.models.DbGroupStudent
import sg.edu.ntu.scse.labattendancesystem.database.models.DbGroupTeacher
import sg.edu.ntu.scse.labattendancesystem.database.models.DbSession
import sg.edu.ntu.scse.labattendancesystem.domain.models.toDatabaseModel
import sg.edu.ntu.scse.labattendancesystem.network.api.MainApi
import sg.edu.ntu.scse.labattendancesystem.network.models.*
import java.time.*
import java.time.temporal.ChronoUnit

class DataSyncer(
    private val api: MainApi,
    private val db: MainDao,
    private val workManager: WorkManager,
    initLabId: Int? = null,
    private val netRequestTimeoutMillis: Long = 10000,
    private val cacheInAdvanceTimeRange: Duration = Duration.ofDays(3),
    private val syncInterval: Duration = Duration.ofMinutes(15),
    onlineInitVal: Boolean = true,
) {
    companion object {
        val TAG: String = DataSyncer::class.java.simpleName;
        const val PERIODIC_SYNC_WORK_NAME = "syncing_session_every_several_minutes";
        const val SYNC_ONCE_WORK_NAME = "syncing_session_once";
        private const val SYNCING_LAB = "syncing_lab";

        private val labSyncer = mutableMapOf<Int, DataSyncer>()
        fun getSyncerOfLab(labId: Int): DataSyncer {
            return labSyncer[labId]
                ?: throw IllegalArgumentException("syncer with lab id not exist")
        }
    }

    private val _online = MutableStateFlow(onlineInitVal)
    val online: StateFlow<Boolean> get() = _online;

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> get() = _syncing;

    var labId: Int? = null
        set(value) {
            if (value == field) return
            Log.d(TAG, "setting labId $field -> $value")

            field?.let { labSyncer.remove(it) }
            value?.let { labSyncer[it] = this }
            field = value
        }

    private var lastDownloadedSessions: Collection<DbSession>? = null
    private var lastDownloadedGroups: Collection<DbGroup>? = null
    private var lastDownloadedCourses: Collection<CourseResp>? = null
    private var lastDownloadedLabs: Collection<LabResp>? = null
    private var lastDownloadedUsers: Collection<UserResp>? = null

    init {
        labId = initLabId
    }

    class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
        CoroutineWorker(appContext, workerParams) {

        private val syncer by lazy {
            Log.d(TAG, "creating syncer in worker")
            getSyncerOfLab(
                workerParams.inputData.getInt(SYNCING_LAB, 0)
            )
        }

        override suspend fun doWork(): Result {
            syncer.doSyncJob()
            return Result.success()
        }
    }

    suspend fun doSyncJob() {
        _syncing.value = true
        downloadSessions()
        _syncing.value = false
    }

    fun syncOnce() {
        if (labId == null) throw NullPointerException("you must set labId before sync")

        val data = Data.Builder()
            .putInt(SYNCING_LAB, labId!!)
            .build()

        val work = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(data)
            .build()

        _syncing.value = true
        workManager
            .beginUniqueWork(SYNC_ONCE_WORK_NAME, ExistingWorkPolicy.KEEP, work)
            .enqueue()
    }

    fun startSync() {
        if (labId == null) throw NullPointerException("you must set labId before startSync")

        val data = Data.Builder()
            .putInt(SYNCING_LAB, labId!!)
            .build()

        val work = PeriodicWorkRequest.Builder(SyncWorker::class.java, syncInterval)
            .setInputData(data)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )
        Log.d(TAG, "started syncing")
    }


    fun stopSync() {
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }


    private suspend fun downloadSessions(): Boolean {
        Log.d(TAG, "started downloading sessions")
        val sessions = fetchSessions(labId!!) ?: return false
        if (!downloadGroupsOfSessions(sessions)) return false
        val dbSessions = sessions.map { it.toDatabaseModel() }
        if (lastDownloadedSessions != dbSessions) {
            db.insertSessions(dbSessions)
            lastDownloadedSessions = dbSessions
        }
        Log.d(TAG, "finished downloading sessions")
        return true
    }

    private suspend fun downloadGroupsOfSessions(sessions: List<SessionResp>): Boolean {
        Log.d(TAG, "started downloading groups")
        val groups = sessions.map { it.group!! }.distinctBy { it.id!! }.sortedBy { it.id }
        if (!downloadLabsOfGroups(groups)) return false
        if (!downloadCoursesOfGroups(groups)) return false

        val allTeachers = mutableSetOf<DbGroupTeacher>()
        val allStudents = mutableSetOf<DbGroupStudent>()
        val allUsers = mutableSetOf<UserResp>()

        for (g in groups) {
            val gid = g.id!!
            val teachers = fetchGroupTeachers(gid) ?: return false
            val students = fetchGroupStudents(gid) ?: return false
            allUsers.addAll(teachers)
            allUsers.addAll(students.map { it.student!! })
            allStudents.addAll(students.map { it.toDatabaseModel() })
            allTeachers.addAll(teachers.map { DbGroupTeacher(teacherId = it.id!!, groupId = gid) })
        }

        val dbGroups = groups.map { it.toDatabaseModel() }
        if (dbGroups != lastDownloadedGroups) {
            db.insertGroups(dbGroups)
            lastDownloadedGroups = dbGroups
        }
        db.insertGroupTeachers(allTeachers)
        db.insertGroupStudents(allStudents)
        if (lastDownloadedUsers != allUsers) {
            db.insertUsers(allUsers.map { it.toDatabaseModel() })
            lastDownloadedUsers = allUsers
        }
        Log.d(TAG, "finished downloading groups")
        return true
    }

    private suspend fun downloadCoursesOfGroups(groups: List<GroupResp>): Boolean {
        Log.d(TAG, "started downloading courses")
        val courses = groups.map { it.course!! }.distinctBy { it.id!! }.sortedBy { it.id }
        if (courses != lastDownloadedCourses) {
            db.insertCourses(courses.map { it.toDatabaseModel() })
            lastDownloadedCourses = courses
        }
        Log.d(TAG, "finished downloading courses")
        return true
    }

    private suspend fun downloadLabsOfGroups(groups: List<GroupResp>): Boolean {
        Log.d(TAG, "started downloading labs")
        val labs = groups.map { it.lab!! }.distinctBy { it.id!! }.sortedBy { it.id }
        if (labs != lastDownloadedLabs) {
            db.insertUsers(labs.map { it.toUserResp().toDatabaseModel() })
            db.insertLabs(labs.map { it.toDatabaseModel() })
            lastDownloadedLabs = labs
        }
        Log.d(TAG, "finished downloading labs")
        return true
    }

    private suspend fun fetchGroupTeachers(groupId: Int): List<UserResp>? {
        return netFetch {
            api.getGroupTeachers(groupId = groupId).teachers
        }
    }

    private suspend fun fetchGroupStudents(groupId: Int): List<GroupStudentResp>? {
        return netFetch {
            api.getStudentsOfGroup(groupId = groupId)
        }
    }

    private suspend fun fetchSessions(labId: Int): List<SessionResp>? {
        val rangeBeg = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val rangeEnd = rangeBeg + cacheInAdvanceTimeRange
        return netFetch {
            api.getSessions(
                labId = labId,
                startDateTimeAfter = rangeBeg,
                startDateTimeBefore = rangeEnd
            ).results
        }
    }

    private suspend fun <T> netFetch(f: suspend () -> T): T? {
        return try {
            withTimeout(netRequestTimeoutMillis) {
                val result = f()
                _online.value = true
                result
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(TAG, "network timeout, maybe offline")
            _online.value = false
            null
        }
    }
}
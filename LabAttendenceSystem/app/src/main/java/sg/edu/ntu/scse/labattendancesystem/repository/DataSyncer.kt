package sg.edu.ntu.scse.labattendancesystem.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withTimeout
import sg.edu.ntu.scse.labattendancesystem.database.MainDao
import sg.edu.ntu.scse.labattendancesystem.database.models.DbGroupStudent
import sg.edu.ntu.scse.labattendancesystem.database.models.DbGroupTeacher
import sg.edu.ntu.scse.labattendancesystem.domain.models.toDatabaseModel
import sg.edu.ntu.scse.labattendancesystem.domain.models.toDbStudentAttendance
import sg.edu.ntu.scse.labattendancesystem.domain.models.toDbTeacherAttendance
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
    private val cacheExpireAfterTimeRange: Duration = Duration.ofDays(7),
    private val cacheInAdvanceTimeRange: Duration = Duration.ofDays(7),
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

    private val _lastNetworkRequestSucceeded = MutableStateFlow(onlineInitVal)
    val lastNetworkRequestSucceeded: StateFlow<Boolean> get() = _lastNetworkRequestSucceeded;

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
            syncer.syncAll()
            return Result.success()
        }
    }

    suspend fun syncAll() {
        withSyncFlag {
            saveAllSessionsOfCurrentLab()
        }
    }

    fun syncAllOnce() {
        if (labId == null) throw NullPointerException("you must set labId before sync")

        val data = Data.Builder().putInt(SYNCING_LAB, labId!!).build()

        val work = OneTimeWorkRequestBuilder<SyncWorker>().setInputData(data).build()

        _syncing.value = true
        workManager.beginUniqueWork(SYNC_ONCE_WORK_NAME, ExistingWorkPolicy.KEEP, work).enqueue()
    }

    fun startPeriodicSyncAll() {
        if (labId == null) throw NullPointerException("you must set labId before startSync")

        val data = Data.Builder().putInt(SYNCING_LAB, labId!!).build()

        val work =
            PeriodicWorkRequest.Builder(SyncWorker::class.java, syncInterval).setInputData(data)
                .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, work
        )
        Log.d(TAG, "started syncing")
    }


    fun stopPeriodicSyncAll() {
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }

    suspend fun syncSession(sessionId: Int) {
        sync(
            download = { api.getSession(sessionId) },
            save = { saveSessions(listOf(it)) }
        )
    }

    suspend fun syncGroup(groupId: Int) {
        sync(
            download = { api.getGroup(groupId) },
            save = { saveGroups(listOf(it)) }
        )
    }

    suspend fun syncStudentAttendanceOfSession(sessionId: Int) {
        sync(
            download = { api.getStudentAttendances(sessionId = sessionId).results },
            save = { l -> db.insertOrUpdateStudentAttendances(l.map { it.toDbStudentAttendance() }) }
        )
    }

    suspend fun syncTeacherAttendanceOfSession(sessionId: Int) {
        sync(
            download = { api.getTeacherAttendances(sessionId = sessionId).results },
            save = { l -> db.insertOrUpdateStudentAttendances(l.map { it.toDbTeacherAttendance() }) }
        )
    }

    private suspend fun <T> withSyncFlag(f: suspend () -> T): T {
        _syncing.value = true;
        return try {
            f();
        } finally {
            _syncing.value = false;
        }
    }

    private suspend fun <T : Any> sync(
        download: suspend () -> T?,
        save: suspend (T) -> Any?,
        onError: (e: Exception) -> Any? = { Log.e(TAG, it.toString()); throw it; }
    ) {
        return withSyncFlag {
            try {
                val s = safeNetReq { download() }
                if (s != null) {
                    save(s)
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private suspend fun saveAllSessionsOfCurrentLab(): Boolean {
        if (labId == null) throw NullPointerException("you must set labId before syncAllSessions")

        val sessions = fetchSessionsOfLab(labId!!) ?: return false
        Log.d(TAG, "saving ${sessions.size} sessions for current lab")
        saveSessions(sessions)

        return true
    }

    private suspend fun saveSessions(sessions: List<SessionResp>): Boolean {
        Log.d(TAG, "started downloading sessions")

        val groups = sessions.map { it.group!! }.distinctBy { it.id!! }.sortedBy { it.id }
        if (!saveGroups(groups)) return false
        val dbSessions = sessions.map { it.toDatabaseModel() }
        db.insertOrUpdateSessions(dbSessions)

        Log.d(TAG, "finished downloading sessions")
        return true
    }

    private suspend fun saveGroups(groups: List<GroupResp>): Boolean {
        Log.d(TAG, "started downloading groups")

        val labs = groups.map { it.lab!! }.distinctBy { it.id!! }.sortedBy { it.id }
        if (!saveLabs(labs)) return false

        val courses = groups.map { it.course!! }.distinctBy { it.id!! }.sortedBy { it.id }
        if (!saveCourses(courses)) return false

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
        db.insertOrUpdateGroups(dbGroups)
        db.insertUsers(allUsers.map { it.toDatabaseModel() })
        db.insertOrUpdateGroupTeachers(allTeachers)
        db.insertOrUpdateGroupStudents(allStudents)

        Log.d(TAG, "finished downloading groups")
        return true
    }

    private suspend fun saveCourses(courses: List<CourseResp>): Boolean {
        Log.d(TAG, "started downloading courses")
        db.insertCourses(courses.map { it.toDatabaseModel() })
        Log.d(TAG, "finished downloading courses")
        return true
    }

    private suspend fun saveLabs(labs: List<LabResp>): Boolean {
        Log.d(TAG, "started downloading labs")
        db.insertUsers(labs.map { it.toUserResp().toDatabaseModel() })
        db.insertLabs(labs.map { it.toDatabaseModel() })
        Log.d(TAG, "finished downloading labs")
        return true
    }

    private suspend fun fetchGroupTeachers(groupId: Int): List<UserResp>? {
        Log.d(TAG, "fetchGroupTeachers")
        return safeNetReq {
            api.getGroupTeachers(groupId = groupId).teachers
        }
    }

    private suspend fun fetchGroupStudents(groupId: Int): List<GroupStudentResp>? {
        Log.d(TAG, "fetchGroupStudents")
        return safeNetReq {
            api.getStudentsOfGroup(groupId = groupId).results
        }
    }

    private suspend fun fetchSessionsOfLab(labId: Int): List<SessionResp>? {
        val now = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val rangeBeg = now - cacheExpireAfterTimeRange
        val rangeEnd = now + cacheInAdvanceTimeRange
        return safeNetReq {
            api.getSessions(
                labId = labId, startDateTimeAfter = rangeBeg, startDateTimeBefore = rangeEnd
            ).results
        }
    }

    private suspend fun <T> safeNetReq(f: suspend () -> T): T? {
        return try {
            withTimeout(netRequestTimeoutMillis) {
                val result = f()
                _lastNetworkRequestSucceeded.value = true
                result
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(TAG, "network timeout, maybe offline")
            _lastNetworkRequestSucceeded.value = false
            null
        }
    }
}
package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.*
import sg.edu.ntu.scse.labattendancesystem.network.UnauthenticatedError
import java.time.ZonedDateTime

class MainRepository(
    app: LabAttendanceSystemApplication,
    externalScope: CoroutineScope,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseRepository(
    app,
    externalScope,
    defaultDispatcher,
) {
    companion object {
        val TAG: String = MainRepository::class.java.simpleName
    }

    private val workManager = WorkManager.getInstance(app)
    private val tokenManager get() = app.tokenManager
    private val sessionManager get() = app.sessionManager
    private val mainDb = app.database.mainDao()
    private val loginHistory get() = app.loginHistoryStore
    private lateinit var syncer: DataSyncer
    private lateinit var labCache: Lab
    private var roomCache: Int? = null

    val online: Flow<Boolean> get() = syncer.lastNetworkRequestSucceeded
    val syncing: Flow<Boolean> get() = syncer.syncing

    val activeSessions: Flow<List<Session>>
        get() = mainDb.getActiveBriefSessions(
            labId = labCache.id,
            roomNo = roomCache,
//            startTimeBefore = ZonedDateTime.now().plusYears(1),
//            endTimeAfter = ZonedDateTime.now().minusYears(1),
        ).map { l ->
            Log.d(TAG, "get ${l.size} sessions")
            l.map { it.toDomainModel() }
        }

    override suspend fun asyncInit() {
        super.asyncInit()
        tokenManager.setUpTokenCache(externalScope, defaultDispatcher)
        Log.d(TAG, "token cache init done")

        labCache = sessionManager.getCurrentLab().toDomainModel()
        roomCache = loginHistory.lastLoginRoomNo.first()
        Log.d(TAG, "lab cache init done $labCache")

        syncer = DataSyncer(
            app,
            workManager,
            initLabId = labCache.id
        )
        syncer.syncAllOnce()
        syncer.startPeriodicSyncAll()
        Log.d(TAG, "main repo init done")
    }

    override fun cleanUp() {
        super.cleanUp()
        syncer.stopPeriodicSyncAll()
    }

    fun refreshData() {
        syncer.syncAllOnce()
    }

    fun verifyLogoutCredential(password: String): Flow<Outcome<Unit>> {
        return asyncLoad {
            val username = loginHistory.lastLoginUsername.first()!!
            if (!sessionManager.verifyCredential(username, password)) {
                throw UnauthenticatedError()
            }
        }
    }

    fun logout(): Flow<Outcome<Unit>> {
        return asyncLoad {
            sessionManager.logout()
        }
    }

    fun getDetailSession(sessionId: Int): Flow<Session> {
        return mainDb.getDetailSession(sessionId)
            .onEach { Log.d(TAG, "getDetailSession(id=$sessionId): got $it") }
            .filterNotNull().map {
                it.toDomainModel()
            }
    }

    fun getDbStudentAttendances(sessionId: Int): Flow<List<Attendance>> {
        return mainDb.getStudentAttendancesOfSession(sessionId = sessionId).filterNotNull()
            .map { l ->
                l.map { it.toDomainModel() }
            }
    }

    fun getDbTeacherAttendances(sessionId: Int): Flow<List<Attendance>> {
        return mainDb.getTeacherAttendancesOfSession(sessionId = sessionId).filterNotNull()
            .map { l ->
                l.map { it.toDomainModel() }
            }
    }

    fun updateStudentAttendance(attendance: Attendance): Flow<Outcome<Unit>> {
        return asyncLoad {
            val newAttendance =
                attendance.copy(lastModify = ZonedDateTime.now() - LAST_MODIFY_SHIFT)
            Log.d(TAG, "new student check in $newAttendance")
            mainDb.insertOrUpdateStudentAttendances(listOf(newAttendance.toDbStudentAttendance()))
            Log.d(TAG, "start syncing student attendance after update")
            syncer.syncStudentAttendance()
            Log.d(TAG, "student att syncing done after update")
        }
    }

    fun updateTeacherAttendance(attendance: Attendance): Flow<Outcome<Unit>> {
        return asyncLoad {
            val newAttendance =
                attendance.copy(lastModify = ZonedDateTime.now() - LAST_MODIFY_SHIFT)
            Log.d(TAG, "new teacher check in $newAttendance")
            mainDb.insertOrUpdateTeacherAttendances(listOf(newAttendance.toDbTeacherAttendance()))
            Log.d(TAG, "start syncing teacher attendance after update")
            syncer.syncTeacherAttendance()
            Log.d(TAG, "teacher att syncing done after update")
        }
    }
}
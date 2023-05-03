package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.*
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
        fun getTokenManager() {

        }
    }

    private val workManager = WorkManager.getInstance(app)
    private val tokenManager get() = app.tokenManager
    private val sessionManager get() = app.sessionManager
    private val mainDb = app.database.mainDao()
    private val loginHistory get() = app.loginHistoryStore
    private val syncer = DataSyncer(
        app.apiServices.main,
        mainDb,
        workManager,
    )
    private lateinit var labCache: Lab
    private var roomCache: Int? = null

    val online: Flow<Boolean> = syncer.lastNetworkRequestSucceeded
    val syncing: Flow<Boolean> = syncer.syncing

    val activeSessions: Flow<List<Session>>
        get() = mainDb.getActiveBriefSessions(
            labId = labCache.id,
            roomNo = roomCache,
            startTimeBefore = ZonedDateTime.now().plusYears(1),
            endTimeAfter = ZonedDateTime.now().minusYears(1),
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
        Log.d(TAG, "lab cache init done")

        syncer.labId = labCache.id
        syncer.syncAllOnce()
        syncer.startPeriodicSyncAll()
        Log.d(TAG, "main repo init done")
    }

    fun refreshData() {
        syncer.syncAllOnce()
    }

    fun getBriefSession(sessionId: Int): Flow<Session> {
        return mainDb.getBriefSession(sessionId).map {
            it.toDomainModel()
        }
    }

    fun getDetailSession(sessionId: Int): Flow<Session> {
        return mainDb.getDetailSession(sessionId).map {
            it.toDomainModel()
        }
    }

    fun getDetailGroup(groupId: Int): Flow<Group> {
        return mainDb.getDetailGroup(groupId).map {
            it.toDomainModel()
        }
    }

    fun getDbStudentAttendances(sessionId: Int): Flow<List<Attendance>> {
        return mainDb.getStudentAttendancesOfSession(sessionId = sessionId).map { l ->
            l.map { it.toDomainModel() }
        }
    }

    fun getDbTeacherAttendances(sessionId: Int): Flow<List<Attendance>> {
        return mainDb.getTeacherAttendancesOfSession(sessionId = sessionId).map { l ->
            l.map { it.toDomainModel() }
        }
    }
}
package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.*
import sg.edu.ntu.scse.labattendancesystem.network.ApiServices
import sg.edu.ntu.scse.labattendancesystem.network.SessionManager
import sg.edu.ntu.scse.labattendancesystem.network.TokenManager
import java.time.OffsetDateTime

class MainRepository(
    private val app: LabAttendanceSystemApplication,
    private val externalScope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseRepository() {
    companion object {
        val TAG: String = MainRepository::class.java.simpleName
    }

    private val workManager = WorkManager.getInstance(app)
    private val tokenManager = TokenManager(app.loginPreferenceDataStore)
    private val sessionManager = SessionManager(tokenManager, ApiServices.token)
    private val apiServices = ApiServices(tokenManager)
    private val deferredInit: Deferred<Any>
    private lateinit var labCache: Lab

    init {
        Log.d(TAG, "init started")
        deferredInit = externalScope.async(defaultDispatcher) { asyncInit() }
    }

    suspend fun awaitForInitDone() {
        withContext(defaultDispatcher) {
            deferredInit.await()
        }
    }

    private suspend fun asyncInit() {
        tokenManager.setUpTokenCache(externalScope, defaultDispatcher)
        Log.d(TAG, "token cache init done")
        initLabCache()
        Log.d(TAG, "lab cache init done")
        Log.d(TAG, "main repo init done")
    }

    private suspend fun initLabCache() {
        labCache = sessionManager.getCurrentLab().toDomainModel()
    }

    private fun <T> safeLoad(call: suspend () -> T) = load {
        deferredInit.await()
        call()
    }

    fun getRecentSessions(): Flow<Result<List<Session>>> {
        return safeLoad {
            getRecentSessionsImpl()
        }
    }

    fun getActiveSessions(): Flow<Result<List<Session>>> {
        return safeLoad {
            getActiveSessionsImpl()
        }
    }

    fun getStudentAttendances(session: Session): Flow<Result<List<Attendance>>> {
        return safeLoad {
            val resp = apiServices.main.getStudentAttendances(pageLimit = 50)
            resp.results.map { it.toDomainModel() }
        }
    }

    fun getTeacherAttendances(session: Session): Flow<Result<List<Attendance>>> {
        return safeLoad {
            val resp = apiServices.main.getStudentAttendances(pageLimit = 3)
            resp.results.map { it.toDomainModel() }
        }
    }

    private suspend fun getActiveSessionsImpl(): List<Session> {
        val labId = labCache.id
        val now = OffsetDateTime.now()
        val resp = apiServices.main.getSessions(
            labId = labId,
//            startDateTimeAfter = now,
            pageLimit = 10,
        )
        return resp.results.map { it.toDomainModel() }
    }


    private suspend fun getRecentSessionsImpl(): List<Session> {
        val labId = labCache.id
        val resp = apiServices.main.getSessions(
            labId = labId,
            pageLimit = 10,
        )
        return resp.results.map { it.toDomainModel() }
    }
}
package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.*
import sg.edu.ntu.scse.labattendancesystem.network.ApiServices
import sg.edu.ntu.scse.labattendancesystem.network.SessionManager
import sg.edu.ntu.scse.labattendancesystem.network.TokenManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

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
    private val sessionManager =
        SessionManager(tokenManager, ApiServices.token)
    private val apiServices = ApiServices(tokenManager)
    private val initJob: Deferred<Any>
    private lateinit var labCache: Lab

    init {
        Log.d(TAG, "init started")
        initJob = externalScope.async(defaultDispatcher) { asyncInit() }
    }

    suspend fun awaitForInitDone() {
        withContext(defaultDispatcher) {
            initJob.await()
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

    fun getAllSessions(): Flow<Result<List<Session>>> {
        return load {
            getAllSessionsImpl()
        }
    }

    private suspend fun getActiveSessionsImpl(): List<Session> {
        val allSession = getAllSessionsImpl()
        val now = LocalDateTime.now()
        return allSession.filter { it.startTime <= now && it.endTime > now }
    }


    private suspend fun getAllSessionsImpl(): List<Session> {
        // TODO: change dummy data to actual data
        val labId = labCache.id
        val res = mutableListOf<Session>()
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val firstSlotBeg = LocalDateTime.of(today, LocalTime.of(8, 30))
        val lastSlotBeg = LocalDateTime.of(today, LocalTime.of(18, 30))
        var startTime = firstSlotBeg
        var i = 1
        while (startTime <= lastSlotBeg) {
            val endTime = startTime + Duration.ofHours(2)
            val randId: Int = i.toInt()
            res.add(
                Session(
                    id = startTime.toEpochSecond(ZoneOffset.UTC).toInt(),
                    group = Group(
                        id = randId,
                        name = "GRP$i",
                        roomNo = if (i % 4 == 0) null else i % 4,
                        course = Course(
                            id = randId,
                            code = "SC000$i",
                            title = "Computer Course $i"
                        ),
                        lab = Lab(
                            id = labId,
                            username = "LAB$i",
                            displayName = "CS LAB $i",
                            roomCount = 4
                        )
                    ),
                    startTime = startTime,
                    endTime = endTime,
                )
            )
            startTime = endTime
            i++
        }
        delay(500)
        return res
    }
}
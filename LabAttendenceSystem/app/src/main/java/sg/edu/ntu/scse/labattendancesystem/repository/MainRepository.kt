package sg.edu.ntu.scse.labattendancesystem.repository

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

class MainRepository(app: LabAttendanceSystemApplication) : BaseRepository() {

    private val workManager = WorkManager.getInstance(app)
    private val sessionManager =
        SessionManager(TokenManager(app.loginPreferenceDataStore), ApiServices.token)

    private val _activeSessions = MutableStateFlow<Result<List<Session>>>(Result.Loading)

    private suspend fun updateActiveSessions() {
        _activeSessions.value = Result.Success(getActiveSessionsImpl())
    }

    fun getActiveSessions(): Flow<Result<List<Session>>> {
        return _activeSessions
    }

    fun getAllSessions(): Flow<Result<List<Session>>> {
        return load {
            getAllSessionsImpl()
        }
    }

    private lateinit var labCache: Deferred<Lab>

    private suspend fun getLab(): Lab {
        if (!this::labCache.isInitialized) initLabCache()
        return labCache.await()
    }

    private suspend fun initLabCache() {
        return coroutineScope() {
            labCache = async {
                sessionManager.getCurrentLab().toDomainModel()
            }
        }
    }

    private suspend fun getActiveSessionsImpl(): List<Session> {
        val allSession = getAllSessionsImpl()
        val now = LocalDateTime.now()
        return allSession.filter { it.startTime <= now && it.endTime > now }
    }


    private suspend fun getAllSessionsImpl(): List<Session> {
        // TODO: change dummy data to actual data
        val labId = getLab().id
        val res = mutableListOf<Session>()
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val firstSlotBeg = LocalDateTime.of(today, LocalTime.of(8, 30))
        val lastSlotBeg = LocalDateTime.of(today, LocalTime.of(18, 30))
        var startTime = firstSlotBeg
        var i = 1
        while (startTime <= lastSlotBeg) {
            val endTime = startTime + Duration.ofHours(2)
            val randId: Long = i.toLong()
            res.add(
                Session(
                    id = startTime.toEpochSecond(ZoneOffset.UTC),
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
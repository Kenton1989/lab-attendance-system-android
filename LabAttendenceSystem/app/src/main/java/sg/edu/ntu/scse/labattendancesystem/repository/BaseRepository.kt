package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import retrofit2.Response
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.Outcome
import java.net.HttpURLConnection

abstract class BaseRepository(
    protected val app: LabAttendanceSystemApplication,
    protected val externalScope: CoroutineScope,
    protected val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    companion object {
        private val TAG = BaseRepository::class.java.simpleName
        private const val REQUEST_TIMEOUT_MS = 30000L
    }

    private val deferredInit: Deferred<Any>

    init {
        Log.d(TAG, "init started")
        deferredInit = externalScope.async(defaultDispatcher) { asyncInit() }
    }

    open fun cleanUp() {}

    suspend fun awaitForInitDone() {
        withContext(defaultDispatcher) {
            deferredInit.await()
        }
    }

    protected open suspend fun asyncInit() {}

    /**
     * Helper function for tracing the loading status of suspended function
     */
    fun <T> asyncLoad(
        timeout: Long = REQUEST_TIMEOUT_MS,
        call: suspend () -> T
    ): Flow<Outcome<T>> = flow {
        emit(Outcome.Loading)
        deferredInit.await()
        withTimeout(timeout) {
            val result = call()

            if (result is Response<*> && result.code() >= 400) {
                throw HttpException(result)
            }

            emit(Outcome.Success(result))
        }
    }.catch { e ->
        when (e) {
            is TimeoutCancellationException -> {
                Log.e(TAG, "operation timeout: $e")
                emit(
                    Outcome.Failure(
                        OperationTimeoutError(),
                        "operation timeout",
                        HttpURLConnection.HTTP_CLIENT_TIMEOUT
                    )
                )
            }
            is HttpException -> {
                Log.e(TAG, "Wrapped unhandled HTTP exception $e")
                val response = e.response()
                response?.errorBody()?.let { error ->
                    error.close()
                    val parsedError: String = error.charStream().readText()
                    emit(Outcome.Failure(e, parsedError, e.code()))
                }
            }
            else -> {
                Log.e(TAG, "Wrapped unhandled exception $e")
                e.printStackTrace()
                emit(
                    Outcome.Failure(
                        e, e.message ?: e.toString(), HttpURLConnection.HTTP_INTERNAL_ERROR
                    )
                )
            }
        }
    }.onEach {
        if (it is Outcome.Success) Log.d(TAG, "result: $it")
        else Log.d(TAG, "result: $it")
    }.flowOn(Dispatchers.IO)
}
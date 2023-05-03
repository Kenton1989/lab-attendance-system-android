package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import retrofit2.Response
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.domain.models.Result
import java.net.HttpURLConnection

abstract class BaseRepository(
    protected val app: LabAttendanceSystemApplication,
    protected val externalScope: CoroutineScope,
    protected val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    companion object {
        private val TAG = BaseRepository::class.java.simpleName
        private const val REQUEST_TIMEOUT_MS = 10000L
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
    fun <T> loadFromNet(call: suspend () -> T): Flow<Result<T>> = flow {
        emit(Result.Loading)
        deferredInit.await()
        withTimeout(REQUEST_TIMEOUT_MS) {
            try {
                val result = call()

                if (result is Response<*> && result.code() >= 400) {
                    throw HttpException(result)
                }

                emit(Result.Success(result))
            } catch (e: HttpException) {
                Log.e(TAG, "Wrapped unhandled HTTP exception $e")
                val response = e.response()
                response?.errorBody()?.let { error ->
                    error.close()
                    val parsedError: String = error.charStream().readText()
                    emit(Result.Failure(e, parsedError, e.code()))
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Wrapped unhandled exception $e")
                e.printStackTrace()
                emit(
                    Result.Failure(
                        e, e.message ?: e.toString(), HttpURLConnection.HTTP_INTERNAL_ERROR
                    )
                )
            }
        }
    }.catch {
        if (it is TimeoutCancellationException) {
            Log.e(TAG, "operation timeout: $it")
            emit(
                Result.Failure(
                    OperationTimeoutError(),
                    "operation timeout",
                    HttpURLConnection.HTTP_CLIENT_TIMEOUT
                )
            )
        } else {
            throw it
        }
    }.onEach {
        if (it is Result.Success) Log.d(TAG, "result: $it")
        else Log.d(TAG, "result: $it")
    }.flowOn(Dispatchers.IO)
}
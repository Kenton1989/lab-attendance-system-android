package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection

abstract class BaseRepository {
    companion object {
        const val HTTP_REQUEST_TIMEOUT_MS = 10000L
        private val TAG = BaseRepository::class.java.simpleName
    }

    /**
     * Helper function for tracing the loading status of suspended function
     */
    fun <T> load(call: suspend () -> T): Flow<Result<T>> = flow {
        emit(Result.Loading)

        withTimeoutOrNull(HTTP_REQUEST_TIMEOUT_MS) {
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
            } catch (e: Exception) {
                Log.e(TAG, "Wrapped unhandled exception")
                emit(
                    Result.Failure(
                        e,
                        e.message ?: e.toString(),
                        HttpURLConnection.HTTP_INTERNAL_ERROR
                    )
                )
            }
        } ?: emit(
            Result.Failure(
                OperationTimeoutError(),
                "operation timeout",
                HttpURLConnection.HTTP_CLIENT_TIMEOUT
            )
        )

    }.flowOn(Dispatchers.IO)
}
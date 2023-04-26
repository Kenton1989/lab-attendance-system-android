package sg.edu.ntu.scse.labattendancesystem.domain.models

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T>() {
    object Loading: Result<Nothing>()

    data class Success<out T>(
        val data: T
    ): Result<T>()

    data class Failure(
        val error: Throwable? = null,
        val errorMessage: String,
        val errorCode: Int,
    ): Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Failure -> "Failure[code=$errorCode]"
            Loading -> "Loading[]"
            is Success -> "Success[data=$data]"
        }
    }
}
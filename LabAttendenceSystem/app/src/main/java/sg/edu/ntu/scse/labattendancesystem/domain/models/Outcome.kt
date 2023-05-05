package sg.edu.ntu.scse.labattendancesystem.domain.models

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Outcome<out T>() {
    object Loading: Outcome<Nothing>()

    data class Success<out T>(
        val data: T
    ): Outcome<T>()

    data class Failure(
        val error: Throwable? = null,
        val errorMessage: String = "",
        val errorCode: Int = 0,
    ): Outcome<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Failure -> "Failure[code=$errorCode]"
            Loading -> "Loading[]"
            is Success -> "Success[data=$data]"
        }
    }
}
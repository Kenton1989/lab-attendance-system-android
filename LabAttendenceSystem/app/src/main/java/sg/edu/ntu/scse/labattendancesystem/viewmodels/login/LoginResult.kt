package sg.edu.ntu.scse.labattendancesystem.viewmodels.login

import androidx.annotation.StringRes

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    @StringRes val errorMsg: Int? = null,
)
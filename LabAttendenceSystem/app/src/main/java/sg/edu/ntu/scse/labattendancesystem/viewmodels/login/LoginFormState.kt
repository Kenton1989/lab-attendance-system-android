package sg.edu.ntu.scse.labattendancesystem.viewmodels.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val roomNoError: Int? = null,
    val isDataValid: Boolean = false
)
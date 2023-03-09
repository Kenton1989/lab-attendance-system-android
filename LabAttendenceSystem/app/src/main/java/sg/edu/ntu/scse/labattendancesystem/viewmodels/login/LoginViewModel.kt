package sg.edu.ntu.scse.labattendancesystem.viewmodels.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.repository.Result

import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.network.UnauthenticatedError
import sg.edu.ntu.scse.labattendancesystem.repository.InvalidLabRoomNumber
import sg.edu.ntu.scse.labattendancesystem.repository.LoginRepository
import sg.edu.ntu.scse.labattendancesystem.repository.UserIsNotLabError
import sg.edu.ntu.scse.labattendancesystem.viewmodels.BaseViewModel


class LoginViewModel(app: LabAttendanceSystemApplication) : BaseViewModel() {
    private val loginRepo = LoginRepository(app.loginPreferenceDataStore)

    private val _loginFormState = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    val allowLogin: LiveData<Boolean> = combine(loginFormState, loginResult) { form, result ->
        form?.isDataValid ?: false && !(result?.isLoading ?: false)
    }

    val lastLoginUsername: LiveData<String?> = loginRepo.lastLoginUsername.asLiveData()

    val defaultUsernameList: LiveData<List<String>> = MutableLiveData(DEFAULT_LAB_USERNAMES)

    fun labLogin(username: String, password: String, roomNo: Int) {
        _loginResult.load {
            loginRepo.labLogin(username, password, roomNo).map { result ->
                Log.i(TAG, result.toString())
                when (result) {
                    Result.Loading -> LoginResult(isLoading = true)
                    is Result.Success -> LoginResult(success = true)
                    is Result.Failure -> when (result.error) {
                        is UnauthenticatedError -> LoginResult(errorMsg = R.string.incorrect_username_or_password)
                        is UserIsNotLabError -> LoginResult(errorMsg = R.string.user_is_not_lab_error)
                        is InvalidLabRoomNumber -> LoginResult(errorMsg = R.string.invalid_room_number_error)
                        else -> LoginResult(errorMsg = R.string.unknown_login_error)
                    }
                }
            }
        }
    }

    fun updateLoginForm(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginFormState.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginFormState.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.isNotBlank()
    }

    companion object {
        private val DEFAULT_LAB_USERNAMES: List<String> = listOf(
            "SWLAB1",
            "SWLAB2",
            "SWLAB3",
            "HWLAB1",
            "HWLAB2",
            "HWLAB3",
            "SPL",
            "HPL",
        )

        private val TAG = LoginViewModel::class.java.simpleName
    }
}
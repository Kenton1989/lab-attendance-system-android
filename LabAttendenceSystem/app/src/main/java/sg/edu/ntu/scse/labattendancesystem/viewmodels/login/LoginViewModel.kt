package sg.edu.ntu.scse.labattendancesystem.viewmodels.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication

import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.domain.models.Outcome
import sg.edu.ntu.scse.labattendancesystem.network.UnauthenticatedError
import sg.edu.ntu.scse.labattendancesystem.repository.*
import sg.edu.ntu.scse.labattendancesystem.viewmodels.BaseViewModel


class LoginViewModel(private val app: LabAttendanceSystemApplication) : BaseViewModel() {
    private val loginRepo = LoginRepository(app, viewModelScope)

    private val _loginFormState = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    val allowLogin: LiveData<Boolean> = combine(loginFormState, loginResult) { form, result ->
        form?.isDataValid ?: false && !(result?.isLoading ?: false)
    }

    val lastLoginUsername: LiveData<String?> = loginRepo.lastLoginUsername.asLiveData()
    val lastLoginRoomNumber: LiveData<Int?> = loginRepo.lastLoginRoomNo.asLiveData()
    val isAlreadyLogin: LiveData<Boolean?> = loginRepo.isAlreadyLogin().map { alreadyLogin ->
        when (alreadyLogin) {
            Outcome.Loading -> null
            is Outcome.Success -> alreadyLogin.data
            is Outcome.Failure -> false
        }
    }.asLiveData()

    val defaultUsernameList: LiveData<List<String>> = MutableLiveData(DEFAULT_LAB_USERNAMES)

    fun labLogin(username: String, password: String, roomNo: Int) {
        _loginResult.load {
            loginRepo.labLogin(username, password, roomNo).map { result ->
                Log.i(TAG, result.toString())
                val onlineRes = when (result) {
                    Outcome.Loading -> LoginResult(isLoading = true)
                    is Outcome.Success -> LoginResult(success = true)
                    is Outcome.Failure -> formatLoginErrorResult(result.error)
                }

                var res = onlineRes
                if (result is Outcome.Failure &&
                    result.error is OperationTimeoutError &&
                    username == lastLoginUsername.value &&
                    app.loginHistoryStore.verifyLastLoginPassword(password)
                ) {
                    res = LoginResult(success = true)
                }

                res
            }
        }
    }

    private fun formatLoginErrorResult(e: Throwable?) =
        when (e) {
            is UnauthenticatedError -> LoginResult(errorMsg = R.string.incorrect_username_or_password)
            is UserIsNotLabError -> LoginResult(errorMsg = R.string.user_is_not_lab_error)
            is InvalidLabRoomNumber -> LoginResult(errorMsg = R.string.invalid_lab_room_number_error)
            is OperationTimeoutError -> LoginResult(errorMsg = R.string.network_request_timeout_error)
            else -> LoginResult(errorMsg = R.string.unknown_login_error)
        }


    fun updateLoginForm(username: String, password: String, roomNo: String) {
        if (!isUserNameValid(username)) {
            _loginFormState.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else if (!isRoomNoValid(roomNo)) {
            _loginFormState.value = LoginFormState(roomNoError = R.string.invalid_room_number)
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

    // A placeholder room number validation check
    private fun isRoomNoValid(roomNo: String): Boolean {
        if (roomNo.isBlank()) return false
        try {
            return roomNo.toInt() >= 1
        } catch (e: NumberFormatException) {
            return false
        }
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
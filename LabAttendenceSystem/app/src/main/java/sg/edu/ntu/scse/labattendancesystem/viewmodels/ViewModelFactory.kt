package sg.edu.ntu.scse.labattendancesystem.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sg.edu.ntu.scse.labattendancesystem.network.LoginDataSource
import sg.edu.ntu.scse.labattendancesystem.repository.LoginRepository
import sg.edu.ntu.scse.labattendancesystem.viewmodels.login.LoginViewModel

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return makeLoginViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun makeLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            loginRepository = LoginRepository(
                dataSource = LoginDataSource()
            )
        )
    }
}
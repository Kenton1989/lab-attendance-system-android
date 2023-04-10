package sg.edu.ntu.scse.labattendancesystem.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import sg.edu.ntu.scse.labattendancesystem.ui.main.MainActivity
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.ActivityLoginBinding

import sg.edu.ntu.scse.labattendancesystem.viewmodels.ViewModelFactory
import sg.edu.ntu.scse.labattendancesystem.viewmodels.login.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: ActivityLoginBinding? = null
    private val binding: ActivityLoginBinding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        loginViewModel =
            ViewModelProvider(this, ViewModelFactory(application))[LoginViewModel::class.java]

        binding.viewModel = loginViewModel

        val username = binding.username
        val password = binding.password
        val roomNo = binding.roomNo
        val login = binding.login
        val loading = binding.loading

        Log.d(TAG, "setup allowLogin observer")
        loginViewModel.allowLogin.observe(this) {
            login.isEnabled = it
        }

        loginViewModel.lastLoginUsername.observe(this) {
            it?.apply {
                username.setText(it)
            }
        }

        loginViewModel.lastLoginRoomNumber.observe(this) {
            it?.apply {
                roomNo.setText(it.toString())
            }
        }

        loginViewModel.loginFormState.observe(this) {
            it?.apply {
                if (usernameError != null) {
                    username.error = getString(usernameError)
                }
                if (passwordError != null) {
                    password.error = getString(passwordError)
                }
                if (roomNoError != null) {
                    roomNo.error = getString(roomNoError)
                }
            }
        }

        loginViewModel.loginResult.observe(this) {
            Log.d(TAG, it.toString())
            it?.apply {
                if (!isLoading)
                    hideLoadingSpinner()

                if (errorMsg != null)
                    showLoginFailed(errorMsg)

                if (success)
                    goToMainApp()
            }
        }

        loginViewModel.defaultUsernameList.observe(this) {
            val nameList = it ?: listOf()
            val adapter = ArrayAdapter(
                this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line,
                nameList
            )
            username.setAdapter(adapter)
        }

        loginViewModel.isAlreadyLogin.observe(this) { isLogin->
            if (isLogin == null) {
                showLoadingSpinner()
            } else if (isLogin) {
                goToMainApp()
            } else {
                hideLoadingSpinner()
            }
        }

        username.afterTextChanged { validateForm() }

        password.afterTextChanged { validateForm() }

        roomNo.afterTextChanged { validateForm() }

        login.setOnClickListener { performLogin() }
    }

    private fun hideLoadingSpinner() {
        binding.loading.visibility = View.GONE
    }

    private fun showLoadingSpinner() {
        binding.loading.visibility = View.VISIBLE
    }

    private fun performLogin() {
        showLoadingSpinner()
        loginViewModel.labLogin(
            binding.username.text.toString(),
            binding.password.text.toString(),
            binding.roomNo.text.toString().toInt()
        )
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_LONG).show()
    }

    private fun validateForm() {
        loginViewModel.updateLoginForm(
            binding.username.text.toString(),
            binding.password.text.toString(),
            binding.roomNo.text.toString(),
        )
    }

    private fun goToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        val TAG: String = LoginActivity::class.java.simpleName
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
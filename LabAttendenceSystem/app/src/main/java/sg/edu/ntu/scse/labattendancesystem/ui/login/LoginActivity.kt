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
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import sg.edu.ntu.scse.labattendancesystem.ItemDetailHostActivity
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.ActivityLoginBinding

import sg.edu.ntu.scse.labattendancesystem.viewmodels.ViewModelFactory
import sg.edu.ntu.scse.labattendancesystem.viewmodels.login.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        loginViewModel =
            ViewModelProvider(this, ViewModelFactory(application))[LoginViewModel::class.java]

        binding.viewModel = loginViewModel

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        Log.d(TAG, "setup allowLogin observer")
        loginViewModel.allowLogin.observe(this) {
            Log.d(TAG, "enable edit: $it")
            login.isEnabled = it
        }

        loginViewModel.lastLoginUsername.observe(this) {
            it?.apply {
                username.setText(it)
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
            }
        }

        loginViewModel.loginResult.observe(this) {
            Log.d(TAG, it.toString())
            it?.apply {
                if (!isLoading)
                    loading.visibility = View.GONE

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

        username.afterTextChanged {
            loginViewModel.updateLoginForm(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.afterTextChanged {
            loginViewModel.updateLoginForm(
                username.text.toString(),
                password.text.toString()
            )
        }

        login.setOnClickListener { performLogin() }
    }

    private fun performLogin() {
        binding.loading.visibility = View.VISIBLE
        loginViewModel.login(
            binding.username.text.toString(),
            binding.password.text.toString()
        )
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun goToMainApp() {
        val intent = Intent(this, ItemDetailHostActivity::class.java)
        startActivity(intent)
        setResult(Activity.RESULT_OK)
        finish()
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
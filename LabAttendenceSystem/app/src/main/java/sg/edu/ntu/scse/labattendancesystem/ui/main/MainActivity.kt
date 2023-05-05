package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.ActivityMainBinding
import sg.edu.ntu.scse.labattendancesystem.domain.models.Outcome
import sg.edu.ntu.scse.labattendancesystem.viewmodels.ViewModelFactory
import sg.edu.ntu.scse.labattendancesystem.viewmodels.main.MainViewModel


class MainActivity : AppCompatActivity() {
    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory(application)
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var menu: Menu
    private val syncAction: MenuItem get() = menu.findItem(R.id.action_sync)
    private val syncActionView: ActionMenuItemView get() = findViewById(R.id.action_sync)

    private lateinit var logoutDialog: AlertDialog
    private lateinit var logoutPassword: EditText

    private val spinAnimation: RotateAnimation = RotateAnimation(
        360F,
        0F,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
    ).apply {
        duration = 1000
        repeatCount = Animation.INFINITE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        buildLogoutDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu!!

        Handler(Looper.getMainLooper()).post {
            viewModel.lastSync.observe(this) {
                Log.d(TAG, "sync result: $it")
                when (it) {
                    Outcome.Loading -> displaySyncingIcon()
                    is Outcome.Success -> displaySyncIdleIcon()
                    is Outcome.Failure -> displaySyncErrorIcon()
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                handleSyncActionClicked()
                true
            }
            R.id.action_logout -> {
                handleLogoutActionClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_nav_host)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun buildLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_logout, null)
        logoutDialog = builder
            .setTitle("Logout")
            .setPositiveButton("LOGOUT") { _, _ -> handleConfirmLogout() }
            .setNegativeButton("CANCEL") { _, _ -> }
            .setView(view)
            .create()
        logoutPassword = view.findViewById(R.id.logout_password)
    }

    private fun handleSyncActionClicked() {
        Log.d(TAG, "action: Sync")
        if (viewModel.lastSync.value == Outcome.Loading) {
            Log.d(TAG, "already syncing")
            return
        }
        viewModel.refreshData()
    }


    private fun handleLogoutActionClicked() {
        Log.d(TAG, "action: Logout");
        logoutPassword.text.clear()
        logoutDialog.show()

    }

    private fun handleConfirmLogout() {
        Log.d(TAG, logoutPassword.text.toString())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Logout...")
            .setMessage("Please wait for logout...")
            .setCancelable(false)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        viewModel.verifyLogoutCredentials(logoutPassword.text.toString()).observeOutcome(
            onSuccess = {
                viewModel.logout().observeOutcome(
                    onSuccess = {
                        dialog.dismiss()
                        showLogoutSuccessBeforeExit()
                    },
                    onError = {
                        dialog.dismiss()
                        showLogoutFailure()
                    }
                )
            },
            onError = {
                dialog.dismiss()
                showLogoutFailure()
            },
            onLoading = { Log.d(TAG, "doing logout...") }
        )
    }

    private fun showLogoutFailure() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Logout Failed")
            .setMessage("Please check correctness of password and network availability.")
            .setPositiveButton("OK") { _, _ -> }
            .create()
        dialog.show()
    }

    private fun showLogoutSuccessBeforeExit() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Logout Success")
            .setMessage("The app will be closed.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .create()
        dialog.show()
    }


    private fun <T> LiveData<Outcome<T>>.observeOutcome(
        onSuccess: (res: Outcome.Success<T>) -> Any?,
        onError: (res: Outcome.Failure) -> Any?,
        onLoading: (res: Outcome.Loading) -> Any? = { Log.d(TAG, "loading outcome...") }
    ) {
        this.observe(this@MainActivity) {
            when (it) {
                is Outcome.Success -> onSuccess(it)
                is Outcome.Failure -> onError(it)
                is Outcome.Loading -> onLoading(it)
            }
        }
    }


    private fun displaySyncingIcon() {
        Log.d(TAG, "syncing")
        syncAction.setIcon(R.drawable.ic_sync_white)
        syncActionView.startAnimation(spinAnimation)
    }

    private fun displaySyncIdleIcon() {
        Log.d(TAG, "synced")
        syncAction.setIcon(R.drawable.ic_sync_white)
        syncActionView.clearAnimation()
    }

    private fun displaySyncErrorIcon() {
        Log.d(TAG, "sync error")
        syncAction.setIcon(R.drawable.ic_sync_error_white)
        syncActionView.clearAnimation()
    }
}
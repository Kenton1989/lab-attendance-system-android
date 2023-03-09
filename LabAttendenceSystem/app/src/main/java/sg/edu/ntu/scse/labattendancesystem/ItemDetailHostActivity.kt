package sg.edu.ntu.scse.labattendancesystem

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sg.edu.ntu.scse.labattendancesystem.databinding.ActivityItemDetailBinding
import sg.edu.ntu.scse.labattendancesystem.viewmodels.ViewModelFactory

class ItemDetailHostActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_item_detail) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_item_detail)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onStop() {
        val sm = ViewModelFactory(application).getSM()
        lifecycleScope.launch{
            Log.d(TAG, "start logout")
            withContext(Dispatchers.IO) {
                sm.logout()
            }
            Log.d(TAG, "logout")
        }
        super.onStop()
    }

    companion object {
        val TAG: String = ItemDetailHostActivity::class.java.simpleName
    }
}
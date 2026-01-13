package page.ooooo.geoshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.billing.BillingImpl
import page.ooooo.geoshare.ui.MainNavigation
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ConversionActivity : ComponentActivity() {
    private lateinit var billing: BillingImpl
    private val viewModel: ConversionViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ConversionViewModel(
                    userPreferencesRepository,
                    billing,
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateInput(AndroidTools.getIntentUriString(intent) ?: "")
        viewModel.start()
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainNavigation(viewModel, introEnabled = false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        billing.startConnection()
    }

    override fun onPause() {
        super.onPause()
        billing.endConnection()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("ConversionActivity", "New intent: ${intent.data}")
        viewModel.updateInput(AndroidTools.getIntentUriString(intent) ?: "")
        viewModel.start()
    }
}

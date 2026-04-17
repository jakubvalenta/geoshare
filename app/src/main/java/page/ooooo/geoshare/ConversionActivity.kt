package page.ooooo.geoshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.ui.BillingViewModel
import page.ooooo.geoshare.ui.ConversionViewModel
import page.ooooo.geoshare.ui.MainNavigation
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ConversionActivity : ComponentActivity() {
    private val billingViewModel: BillingViewModel by viewModels()
    private val conversionViewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "Create: ${intent.data}")
        conversionViewModel.onCreateOrNewIntent(intent)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainNavigation(billingViewModel, conversionViewModel, introEnabled = false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        billingViewModel.onResume(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "New intent: ${intent.data}")
        conversionViewModel.onCreateOrNewIntent(intent)
    }

    companion object {
        const val TAG = "ConversionActivity"
    }
}

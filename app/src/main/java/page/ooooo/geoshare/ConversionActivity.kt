package page.ooooo.geoshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.ui.MainNavigation
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ConversionActivity : ComponentActivity() {
    private val viewModel: ConversionViewModel by viewModels()

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
        viewModel.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("ConversionActivity", "New intent: ${intent.data}")
        viewModel.updateInput(AndroidTools.getIntentUriString(intent) ?: "")
        viewModel.start()
    }
}

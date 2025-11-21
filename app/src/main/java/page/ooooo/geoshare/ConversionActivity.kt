package page.ooooo.geoshare

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.ui.ConversionNavigation
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
                ConversionNavigation(viewModel, onFinish = { finish() })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.updateInput(AndroidTools.getIntentUriString(intent) ?: "")
        viewModel.start()
    }
}

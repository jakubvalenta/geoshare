package page.ooooo.geoshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.ConversionRunContext
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ConversionActivity : ComponentActivity() {
    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val context = LocalContext.current
                val clipboard = LocalClipboard.current
                val saveGpxLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        viewModel.saveGpx(context, it)
                    }
                LaunchedEffect(intent) {
                    viewModel.start(ConversionRunContext(context, clipboard, saveGpxLauncher), intent)
                }
                ConversionNavigation(viewModel, onFinish = { finish() })
            }
        }
    }
}

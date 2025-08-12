package page.ooooo.geoshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.components.ConversionDialog
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(intent) {
                viewModel.start(intent)
            }
            AppTheme {
                ConversionDialog(onFinish = { finish() }, viewModel = viewModel)
            }
        }
    }
}

package page.ooooo.geoshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainNavigation(intent, viewModel)
            }
        }
    }
}

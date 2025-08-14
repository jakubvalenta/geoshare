package page.ooooo.geoshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.components.ConversionDialog
import page.ooooo.geoshare.lib.ManagedActivityResultLauncherWrapper
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val clipboard = LocalClipboard.current
            val context = LocalContext.current
            val currentState by viewModel.currentState.collectAsStateWithLifecycle()

            LaunchedEffect(intent) {
                viewModel.start(intent)
            }

            AppTheme {
                ConversionDialog(
                    currentState,
                    viewModel.loadingIndicatorTitleResId,
                    queryGeoUriApps = { context -> viewModel.queryGeoUriApps(context) },
                    onGrant = { doNotAsk -> viewModel.grant(doNotAsk) },
                    onDeny = { doNotAsk -> viewModel.grant(doNotAsk) },
                    onCopy = { viewModel.copy(context, clipboard) },
                    onShare = { settingsLauncher ->
                        viewModel.share(context, ManagedActivityResultLauncherWrapper(settingsLauncher))
                    },
                    onSkip = { settingsLauncher ->
                        viewModel.skip(context, ManagedActivityResultLauncherWrapper(settingsLauncher))
                    },
                    onCancel = { viewModel.cancel() },
                    onFinish = { finish() }
                )
            }
        }
    }
}

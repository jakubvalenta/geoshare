package page.ooooo.geoshare

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.ConversionDialog
import page.ooooo.geoshare.components.MainForm
import page.ooooo.geoshare.lib.Initial
import page.ooooo.geoshare.lib.ManagedActivityResultLauncherWrapper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    onNavigateToAboutScreen: () -> Unit = {},
    onNavigateToFaqScreen: () -> Unit = {},
    onNavigateToIntroScreen: () -> Unit = {},
    onNavigateToUserPreferencesScreen: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()

    when (currentState) {
        is Initial -> {
            MainForm(
                inputUriString = viewModel.inputUriString,
                onUpdateInput = { viewModel.updateInput(it) },
                onStart = { viewModel.start() },
                onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                onNavigateToFaqScreen = onNavigateToFaqScreen,
                onNavigateToIntroScreen = onNavigateToIntroScreen,
                onNavigateToAboutScreen = onNavigateToAboutScreen,
            )
        }

        else -> {
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
            )
        }
    }
}

package page.ooooo.geoshare

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import page.ooooo.geoshare.components.ConversionDialog
import page.ooooo.geoshare.components.MainForm

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    onNavigateToAboutScreen: () -> Unit = {},
    onNavigateToFaqScreen: () -> Unit = {},
    onNavigateToIntroScreen: () -> Unit = {},
    onNavigateToUserPreferencesScreen: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    MainForm(
        inputUriString = viewModel.inputUriString,
        onUpdateInput = { viewModel.updateInput(it) },
        onStart = { viewModel.start() },
        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
        onNavigateToFaqScreen = onNavigateToFaqScreen,
        onNavigateToIntroScreen = onNavigateToIntroScreen,
        onNavigateToAboutScreen = onNavigateToAboutScreen,
    )
    ConversionDialog(viewModel = viewModel)
}

package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import page.ooooo.geoshare.ui.UrlConvertersScreen
import page.ooooo.geoshare.ui.UserPreferencesDetailDeveloperOptionsScreen
import page.ooooo.geoshare.ui.UserPreferencesListScreen
import page.ooooo.geoshare.ui.UserPreferencesDetailAutomaticActionScreen
import page.ooooo.geoshare.ui.UserPreferencesDetailConnectionPermissionScreen

@Composable
fun ConversionNavigation(viewModel: ConversionViewModel, onFinish: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "conversion") {
        composable("about") {
            AboutScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("conversion") },
            )
        }
        composable("conversion") {
            ConversionScreen(
                onBack = onFinish,
                onCancel = onFinish,
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToUrlConvertersScreen = { navController.navigate("url_converters") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("faq") {
            FaqScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("conversion") },
            )
        }
        composable("intro") {
            IntroScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("conversion") },
                viewModel = viewModel,
            )
        }
        composable("url_converters") {
            UrlConvertersScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("conversion") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences") {
            UserPreferencesListScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("conversion") },
                onNavigateToUserPreferencesDetailAutomaticActionScreen = { navController.navigate("user_preferences/automatic_action") },
                onNavigateToUserPreferencesDetailConnectionPermissionScreen = { navController.navigate("user_preferences/connection_permission") },
                onNavigateToUserPreferencesDetailDeveloperOptionsScreen = { navController.navigate("user_preferences/developer_options") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences/automatic_action") {
            UserPreferencesDetailAutomaticActionScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences/connection_permission") {
            UserPreferencesDetailConnectionPermissionScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences/developer_options") {
            UserPreferencesDetailDeveloperOptionsScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
    }
}

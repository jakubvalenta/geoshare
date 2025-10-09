package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import page.ooooo.geoshare.components.MainScreen
import page.ooooo.geoshare.ui.UrlConvertersScreen
import page.ooooo.geoshare.ui.UserPreferencesDetailDeveloperOptionsScreen
import page.ooooo.geoshare.ui.UserPreferencesListScreen
import page.ooooo.geoshare.ui.UserPreferencesDetailAutomaticActionScreen
import page.ooooo.geoshare.ui.UserPreferencesDetailConnectionPermissionScreen

@Composable
fun MainNavigation(viewModel: ConversionViewModel) {
    val navController = rememberNavController()
    val introShown by viewModel.introShown.collectAsState()

    LaunchedEffect(introShown) {
        if (!introShown) {
            navController.navigate("intro") {
                popUpTo("main") { inclusive = false }
            }
        }
    }

    NavHost(navController = navController, startDestination = "main") {
        composable("about") {
            AboutScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("main") },
            )
        }
        composable("conversion") {
            ConversionScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("main") },
                onCancel = {},
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
                onBack = { if (!navController.popBackStack()) navController.navigate("main") },
            )
        }
        composable("intro") {
            IntroScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("conversion") },
                viewModel = viewModel,
            )
        }
        composable("main") {
            MainScreen(
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToConversionScreen = { navController.navigate("conversion") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToUrlConvertersScreen = { navController.navigate("url_converters") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("url_converters") {
            UrlConvertersScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("main") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences") {
            UserPreferencesListScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("main") },
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

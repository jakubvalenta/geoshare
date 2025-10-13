package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import page.ooooo.geoshare.ui.MainScreen
import page.ooooo.geoshare.lib.ConversionRunContext
import page.ooooo.geoshare.ui.*

@Composable
fun MainNavigation(runContext: ConversionRunContext, viewModel: ConversionViewModel) {
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
                runContext = runContext,
                onBack = { if (!navController.popBackStack()) navController.navigate("main") },
                onFinish = {},
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToUrlConvertersScreen = { navController.navigate("url_converters") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                onNavigateToUserPreferencesAutomationScreen = { navController.navigate("user_preferences/automation") },
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
                runContext = runContext,
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
                onNavigateToUserPreferencesAutomationScreen = { navController.navigate("user_preferences/automation") },
                onNavigateToUserPreferencesConnectionPermissionScreen = { navController.navigate("user_preferences/connection_permission") },
                onNavigateToUserPreferencesDeveloperScreen = { navController.navigate("user_preferences/developer") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences/automation") {
            UserPreferencesDetailAutomationScreen(
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
        composable("user_preferences/developer") {
            UserPreferencesDetailDeveloperScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
    }
}

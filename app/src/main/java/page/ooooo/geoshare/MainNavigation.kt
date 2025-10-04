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

@Composable
fun MainNavigation(viewModel: ConversionViewModel) {
    val navController = rememberNavController()
    val changelogShown by viewModel.changelogShown.collectAsState()
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
                onBack = { navController.navigate("main") },
            )
        }
        composable("faq") {
            FaqScreen(
                onBack = { navController.navigate("main") },
            )
        }
        composable("intro") {
            IntroScreen(
                onCloseIntro = {
                    viewModel.setIntroShown()
                    navController.navigate("main")
                },
                viewModel = viewModel,
            )
        }
        composable("main") {
            MainScreen(
                changelogShown = changelogShown,
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToConversionScreen = { navController.navigate("conversion") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToUrlConvertersScreen = { navController.navigate("url_converters") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("conversion") {
            ConversionScreen(
                changelogShown = changelogShown,
                onBack = { navController.navigate("main") },
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToUrlConvertersScreen = { navController.navigate("url_converters") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("url_converters") {
            // TODO Set changelog shown
            UrlConvertersScreen(
                onBack = { navController.navigate("main") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences") {
            UserPreferencesScreen(
                onBack = { navController.navigate("main") },
                viewModel = viewModel,
            )
        }
    }
}

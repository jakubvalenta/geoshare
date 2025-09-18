package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import page.ooooo.geoshare.components.MainScreen

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
                onNavigateToMainScreen = { navController.navigate("main") },
            )
        }
        composable("faq") {
            FaqScreen(
                onNavigateToMainScreen = { navController.navigate("main") },
            )
        }
        composable("intro") {
            IntroScreen(
                onCloseIntro = {
                    viewModel.setIntroShown()
                    navController.navigate("main")
                },
            )
        }
        composable("main") {
            MainScreen(
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToConversionScreen = { navController.navigate("conversion") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("conversion") {
            ConversionScreen(
                onBack = { navController.navigate("main") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences") {
            UserPreferencesScreen(
                onNavigateToMainScreen = { navController.navigate("main") },
                viewModel = viewModel,
            )
        }
    }
}

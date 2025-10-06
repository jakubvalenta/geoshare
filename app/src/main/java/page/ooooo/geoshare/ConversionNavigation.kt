package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import page.ooooo.geoshare.ui.UrlConvertersScreen

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
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToUrlConvertersScreen = { navController.navigate("url_converters") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                onFinish = onFinish,
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
            UserPreferencesScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate("conversion") },
                viewModel = viewModel,
            )
        }
    }
}

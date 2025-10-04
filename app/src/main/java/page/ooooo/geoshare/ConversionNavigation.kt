package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import page.ooooo.geoshare.ui.UrlConvertersScreen

@Composable
fun ConversionNavigation(viewModel: ConversionViewModel, onFinish: () -> Unit) {
    val navController = rememberNavController()
    val changelogShown by viewModel.changelogShown.collectAsState()

    NavHost(navController = navController, startDestination = "conversion") {
        composable("about") {
            AboutScreen(
                onNavigateToMainScreen = { navController.navigate("conversion") },
            )
        }
        composable("faq/{itemId}") { backStackEntry ->
            val initialExpandedItemId = backStackEntry.arguments?.getString("itemId")?.let {
                try {
                    FaqItemId.valueOf(it)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
            FaqScreen(
                initialExpandedItemId = initialExpandedItemId,
                onBack = { navController.navigate("conversion") },
            )
        }
        composable("intro") {
            IntroScreen(
                onCloseIntro = {
                    viewModel.setIntroShown()
                    navController.navigate("conversion")
                },
                viewModel = viewModel,
            )
        }
        composable("conversion") {
            ConversionScreen(
                changelogShown = changelogShown,
                onBack = onFinish,
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToFaqScreen = { itemId -> navController.navigate("faq/$itemId") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToUrlConvertersScreen = { navController.navigate("url_converters") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                onFinish = onFinish,
                viewModel = viewModel,
            )
        }
        composable("url_converters") {
            // TODO Set changelog shown
            UrlConvertersScreen(
                onNavigateToMainScreen = { navController.navigate("conversion") },
                viewModel = viewModel,
            )
        }
        composable("user_preferences") {
            UserPreferencesScreen(
                onNavigateToMainScreen = { navController.navigate("conversion") },
                viewModel = viewModel,
            )
        }
    }
}

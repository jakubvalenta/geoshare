package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ConversionNavigation(viewModel: ConversionViewModel, onFinish: () -> Unit) {
    val navController = rememberNavController()
    val changelogShown by viewModel.changelogShown.collectAsState()

    NavHost(navController = navController, startDestination = "conversion") {
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
        composable("conversion") {
            ConversionScreen(
                changelogShown = changelogShown,
                onBack = onFinish,
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToChangelogScreen = { navController.navigate("changelog") },
                onNavigateToFaqScreen = { itemId -> navController.navigate("faq/$itemId") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToSupportedUrisScreen = { navController.navigate("supported_uris") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                onFinish = onFinish,
                viewModel = viewModel,
            )
        }
    }
}

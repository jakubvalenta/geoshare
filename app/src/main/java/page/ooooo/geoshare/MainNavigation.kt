package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import page.ooooo.geoshare.components.MainScreen
import page.ooooo.geoshare.ui.ChangelogScreen
import page.ooooo.geoshare.ui.MapServicesScreen

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
                onNavigateToMainScreen = { navController.navigate("main") },
            )
        }
        composable("changelog") {
            viewModel.setChangelogShown()
            ChangelogScreen(
                onNavigateToMainScreen = { navController.navigate("main") },
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
                onNavigateToChangelogScreen = { navController.navigate("changelog") },
                onNavigateToConversionScreen = { navController.navigate("conversion") },
                onNavigateToFaqScreen = { itemId -> navController.navigate("faq/$itemId") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToSupportedUrisScreen = { navController.navigate("supported_uris") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("conversion") {
            ConversionScreen(
                changelogShown = changelogShown,
                onBack = { navController.navigate("main") },
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToChangelogScreen = { navController.navigate("changelog") },
                onNavigateToFaqScreen = { itemId -> navController.navigate("faq/$itemId") },
                onNavigateToIntroScreen = { navController.navigate("intro") },
                onNavigateToSupportedUrisScreen = { navController.navigate("supported_uris") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") },
                viewModel = viewModel,
            )
        }
        composable("supported_uris") {
            MapServicesScreen(
                onNavigateToMainScreen = { navController.navigate("main") },
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

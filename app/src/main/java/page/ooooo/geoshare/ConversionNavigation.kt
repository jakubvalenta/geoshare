package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ConversionNavigation(viewModel: ConversionViewModel, onFinish: () -> Unit) {
    val navController = rememberNavController()

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
                onBack = onFinish,
                onNavigateToFaqScreen = { itemId -> navController.navigate("faq/$itemId") },
                onFinish = onFinish,
                viewModel = viewModel,
            )
        }
    }
}

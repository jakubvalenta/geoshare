package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ConversionNavigation(viewModel: ConversionViewModel, onFinish: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "conversion") {
        composable("faq") {
            FaqScreen(
                onNavigateToMainScreen = onFinish,
            )
        }
        composable("conversion") {
            ConversionScreen(
                onBack = onFinish,
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onFinish = onFinish,
                viewModel = viewModel,
            )
        }
    }
}

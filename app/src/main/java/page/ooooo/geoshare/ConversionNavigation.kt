package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import page.ooooo.geoshare.lib.ConversionRunContext
import page.ooooo.geoshare.ui.*

@Composable
fun ConversionNavigation(runContext: ConversionRunContext, viewModel: ConversionViewModel, onFinish: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ConversionRoute) {
        composable<AboutRoute> {
            AboutScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(ConversionRoute) },
            )
        }
        composable<ConversionRoute> {
            ConversionScreen(
                runContext = runContext,
                onBack = onFinish,
                onFinish = onFinish,
                onNavigateToAboutScreen = { navController.navigate(AboutRoute) },
                onNavigateToFaqScreen = { navController.navigate(FaqRoute) },
                onNavigateToInputsScreen = { navController.navigate(InputsRoute) },
                onNavigateToIntroScreen = { navController.navigate(IntroRoute) },
                onNavigateToUserPreferencesScreen = { navController.navigate(UserPreferencesRoute()) },
                onNavigateToUserPreferencesAutomationScreen = {
                    navController.navigate(UserPreferencesRoute(UserPreferencesGroupId.AUTOMATION))
                },
                viewModel = viewModel,
            )
        }
        composable<FaqRoute> {
            FaqScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(ConversionRoute) },
            )
        }
        composable<InputsRoute> {
            InputsScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(ConversionRoute) },
                viewModel = viewModel,
            )
        }
        composable<IntroRoute> {
            IntroScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(ConversionRoute) },
                viewModel = viewModel,
            )
        }
        composable<UserPreferencesRoute> { backStackEntry ->
            val route: UserPreferencesRoute = backStackEntry.toRoute()
            UserPreferencesScreen(
                initialGroupId = route.id,
                onBack = { if (!navController.popBackStack()) navController.navigate(ConversionRoute) },
                viewModel = viewModel,
            )
        }
    }
}

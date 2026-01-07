package page.ooooo.geoshare.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.lib.inputs.InputDocumentationId

@Serializable
object AboutRoute

@Serializable
object FaqRoute

@Serializable
data class InputsRoute(val id: InputDocumentationId? = null)

@Serializable
object IntroRoute

@Serializable
object MainRoute

@Serializable
data class UserPreferencesRoute(val id: UserPreferencesGroupId? = null)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainNavigation(viewModel: ConversionViewModel, introEnabled: Boolean) {
    val navController = rememberNavController()
    val introShown by viewModel.introShown.collectAsState()

    LaunchedEffect(introEnabled, introShown) {
        if (introEnabled && !introShown) {
            navController.navigate(IntroRoute) {
                popUpTo(MainRoute) { inclusive = false }
            }
        }
    }

    NavHost(navController = navController, startDestination = MainRoute) {
        composable<AboutRoute> {
            AboutScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
            )
        }
        composable<FaqRoute> {
            FaqScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
            )
        }
        composable<IntroRoute> {
            IntroScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                viewModel = viewModel,
            )
        }
        composable<MainRoute> {
            MainScreen(
                onNavigateToAboutScreen = { navController.navigate(AboutRoute) },
                onNavigateToFaqScreen = { navController.navigate(FaqRoute) },
                onNavigateToIntroScreen = { navController.navigate(IntroRoute) },
                onNavigateToInputsScreen = { navController.navigate(InputsRoute()) },
                onNavigateToSubscriptionScreen = {
                    // TODO Navigate to subscription screen
                },
                onNavigateToUserPreferencesScreen = { navController.navigate(UserPreferencesRoute()) },
                onNavigateToUserPreferencesAutomationScreen = {
                    navController.navigate(UserPreferencesRoute(UserPreferencesGroupId.AUTOMATION))
                },
                viewModel = viewModel,
            )
        }
        composable<InputsRoute> { backStackEntry ->
            val route: InputsRoute = backStackEntry.toRoute()
            InputsScreen(
                initialDocumentationId = route.id,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                viewModel = viewModel,
            )
        }
        composable<UserPreferencesRoute> { backStackEntry ->
            val route: UserPreferencesRoute = backStackEntry.toRoute()
            UserPreferencesScreen(
                initialGroupId = route.id,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                onNavigateToSubscriptionScreen = {
                    // TODO Navigate to subscription screen
                },
                viewModel = viewModel,
            )
        }
    }
}

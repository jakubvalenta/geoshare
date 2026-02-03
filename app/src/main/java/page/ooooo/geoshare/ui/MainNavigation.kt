package page.ooooo.geoshare.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
object BillingRoute

@Serializable
data class UserPreferencesRoute(val id: UserPreferencesGroupId? = null)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainNavigation(viewModel: ConversionViewModel, introEnabled: Boolean) {
    val navController = rememberNavController()
    val introShown by viewModel.introShown.collectAsStateWithLifecycle()

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
                viewModel = viewModel,
            )
        }
        composable<FaqRoute> {
            FaqScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                onNavigateToUserPreferencesConnectionPermissionScreen = {
                    navController.navigate(UserPreferencesRoute(UserPreferencesGroupId.CONNECTION_PERMISSION))
                },
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
                onNavigateToBillingScreen = { navController.navigate(BillingRoute) },
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
        composable<BillingRoute> {
            BillingScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                viewModel = viewModel,
            )
        }
        composable<UserPreferencesRoute> { backStackEntry ->
            val route: UserPreferencesRoute = backStackEntry.toRoute()
            UserPreferencesScreen(
                initialGroupId = route.id,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                onNavigateToBillingScreen = { navController.navigate(BillingRoute) },
                viewModel = viewModel,
            )
        }
    }
}

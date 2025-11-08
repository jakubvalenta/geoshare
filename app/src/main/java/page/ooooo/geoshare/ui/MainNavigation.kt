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
import page.ooooo.geoshare.lib.ConversionRunContext

@Serializable
object AboutRoute

@Serializable
object ConversionRoute

@Serializable
object FaqRoute

@Serializable
object InputsRoute

@Serializable
object IntroRoute

@Serializable
object MainRoute

@Serializable
data class UserPreferencesRoute(val id: UserPreferencesGroupId? = null)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainNavigation(runContext: ConversionRunContext, viewModel: ConversionViewModel) {
    val navController = rememberNavController()
    val introShown by viewModel.introShown.collectAsState()

    LaunchedEffect(introShown) {
        if (!introShown) {
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
        composable<ConversionRoute> {
            ConversionScreen(
                runContext = runContext,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                onFinish = {},
                onNavigateToAboutScreen = { navController.navigate(AboutRoute) },
                onNavigateToFaqScreen = { navController.navigate(FaqRoute) },
                onNavigateToIntroScreen = { navController.navigate(IntroRoute) },
                onNavigateToInputsScreen = { navController.navigate(InputsRoute) },
                onNavigateToUserPreferencesScreen = { navController.navigate(UserPreferencesRoute()) },
                onNavigateToUserPreferencesAutomationScreen = {
                    navController.navigate(UserPreferencesRoute(UserPreferencesGroupId.AUTOMATION))
                },
                viewModel = viewModel,
            )
        }
        composable<FaqRoute> {
            FaqScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
            )
        }
        composable<IntroRoute> {
            IntroScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(ConversionRoute) },
                viewModel = viewModel,
            )
        }
        composable<MainRoute> {
            MainScreen(
                runContext = runContext,
                onNavigateToAboutScreen = { navController.navigate(AboutRoute) },
                onNavigateToConversionScreen = { navController.navigate(ConversionRoute) },
                onNavigateToFaqScreen = { navController.navigate(FaqRoute) },
                onNavigateToInputsScreen = { navController.navigate(InputsRoute) },
                onNavigateToIntroScreen = { navController.navigate(IntroRoute) },
                onNavigateToUserPreferencesScreen = { navController.navigate(UserPreferencesRoute()) },
                viewModel = viewModel,
            )
        }
        composable<InputsRoute> {
            InputsScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                viewModel = viewModel,
            )
        }
        composable<UserPreferencesRoute> { backStackEntry ->
            val route: UserPreferencesRoute = backStackEntry.toRoute()
            UserPreferencesScreen(
                initialGroupId = route.id,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                viewModel = viewModel,
            )
        }
    }
}

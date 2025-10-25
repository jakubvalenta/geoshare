package page.ooooo.geoshare

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
import page.ooooo.geoshare.ui.MainScreen
import page.ooooo.geoshare.lib.ConversionRunContext
import page.ooooo.geoshare.ui.*

@Serializable
object AboutRoute

@Serializable
object ConversionRoute

@Serializable
object FaqRoute

@Serializable
object IntroRoute

@Serializable
object MainRoute

@Serializable
object UrlConvertersRoute

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
                onNavigateToUrlConvertersScreen = { navController.navigate(UrlConvertersRoute) },
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
                onNavigateToIntroScreen = { navController.navigate(IntroRoute) },
                onNavigateToUrlConvertersScreen = { navController.navigate(UrlConvertersRoute) },
                onNavigateToUserPreferencesScreen = { navController.navigate(UserPreferencesRoute()) },
                viewModel = viewModel,
            )
        }
        composable<UrlConvertersRoute> {
            UrlConvertersScreen(
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

package page.ooooo.geoshare.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.data.IntroViewModel
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
object LinksRoute

@Serializable
object MainRoute

@Serializable
object BillingRoute

@Serializable
data class UserPreferencesRoute(val id: UserPreferencesGroupId? = null)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainNavigation(
    billingViewModel: BillingViewModel,
    conversionViewModel: ConversionViewModel = hiltViewModel(),
    introEnabled: Boolean = true,
    introViewModel: IntroViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val introShown by introViewModel.shown.collectAsStateWithLifecycle()

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
                billingViewModel = billingViewModel,
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
                onClose = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                viewModel = introViewModel,
            )
        }
        composable<MainRoute> {
            MainScreen(
                onNavigateToAboutScreen = { navController.navigate(AboutRoute) },
                onNavigateToBillingScreen = { navController.navigate(BillingRoute) },
                onNavigateToFaqScreen = { navController.navigate(FaqRoute) },
                onNavigateToInputsScreen = { navController.navigate(InputsRoute()) },
                onNavigateToIntroScreen = { navController.navigate(IntroRoute) },
                onNavigateToLinksScreen = { navController.navigate(LinksRoute) },
                onNavigateToUserPreferencesAutomationScreen = {
                    navController.navigate(UserPreferencesRoute(UserPreferencesGroupId.AUTOMATION))
                },
                onNavigateToUserPreferencesScreen = { navController.navigate(UserPreferencesRoute()) },
                billingViewModel = billingViewModel,
                conversionViewModel = conversionViewModel,
            )
        }
        composable<InputsRoute> { backStackEntry ->
            val route: InputsRoute = backStackEntry.toRoute()
            InputsScreen(
                initialDocumentationId = route.id,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
            )
        }
        composable<LinksRoute> { backStackEntry ->
            LinksScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
            )
        }
        composable<BillingRoute> {
            BillingScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                billingViewModel = billingViewModel,
            )
        }
        composable<UserPreferencesRoute> { backStackEntry ->
            val route: UserPreferencesRoute = backStackEntry.toRoute()
            UserPreferencesScreen(
                initialGroupId = route.id,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                onNavigateToBillingScreen = { navController.navigate(BillingRoute) },
                onNavigateToLinksScreen = { navController.navigate(LinksRoute) },
                billingViewModel = billingViewModel,
            )
        }
    }
}

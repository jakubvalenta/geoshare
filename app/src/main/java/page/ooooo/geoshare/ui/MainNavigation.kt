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
import page.ooooo.geoshare.lib.inputs.InputDocumentationGroup

@Serializable
object AboutRoute

@Serializable
object BillingRoute

@Serializable
object FaqRoute

@Serializable
data class InputsRoute(val group: InputDocumentationGroup? = null)

@Serializable
object IntroRoute

@Serializable
object LicensesRoute

@Serializable
object LinkRoute

@Serializable
object MainRoute

@Serializable
object ServerRoute

@Serializable
data class UserPreferencesRoute(val id: UserPreferenceGroupId? = null)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainNavigation(
    billingViewModel: BillingViewModel,
    conversionViewModel: ConversionViewModel = hiltViewModel(),
    introEnabled: Boolean = true,
    onFinish: () -> Unit = {},
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
                onNavigateToLicensesScreen = { navController.navigate(LicensesRoute) },
                billingViewModel = billingViewModel,
            )
        }
        composable<FaqRoute> {
            FaqScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                onNavigateToUserPreferencesConnectionPermissionScreen = {
                    navController.navigate(UserPreferencesRoute(UserPreferenceGroupId.CONNECTION_PERMISSION))
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
                onFinish = onFinish,
                onNavigateToAboutScreen = { navController.navigate(AboutRoute) },
                onNavigateToBillingScreen = { navController.navigate(BillingRoute) },
                onNavigateToFaqScreen = { navController.navigate(FaqRoute) },
                onNavigateToInputsScreen = { navController.navigate(InputsRoute()) },
                onNavigateToIntroScreen = { navController.navigate(IntroRoute) },
                onNavigateToLinkScreen = { navController.navigate(LinkRoute) },
                onNavigateToUserPreferencesAutomationScreen = {
                    navController.navigate(UserPreferencesRoute(UserPreferenceGroupId.AUTOMATION))
                },
                onNavigateToUserPreferencesScreen = { navController.navigate(UserPreferencesRoute()) },
                billingViewModel = billingViewModel,
                conversionViewModel = conversionViewModel,
            )
        }
        composable<InputsRoute> { backStackEntry ->
            val route: InputsRoute = backStackEntry.toRoute()
            InputsScreen(
                initialDocumentationGroup = route.group,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
            )
        }
        composable<LicensesRoute> {
            LicensesScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
            )
        }
        composable<LinkRoute> {
            LinkScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                onNavigateToBillingScreen = { navController.navigate(BillingRoute) },
            )
        }
        composable<BillingRoute> {
            BillingScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                billingViewModel = billingViewModel,
            )
        }
        composable<ServerRoute> {
            ServerScreen(
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
            )
        }
        composable<UserPreferencesRoute> { backStackEntry ->
            val route: UserPreferencesRoute = backStackEntry.toRoute()
            UserPreferenceScreen(
                initialGroupId = route.id,
                onBack = { if (!navController.popBackStack()) navController.navigate(MainRoute) },
                onNavigateToBillingScreen = { navController.navigate(BillingRoute) },
                onNavigateToLinkScreen = { navController.navigate(LinkRoute) },
                onNavigateToServerScreen = { navController.navigate(ServerRoute) },
                billingViewModel = billingViewModel,
            )
        }
    }
}

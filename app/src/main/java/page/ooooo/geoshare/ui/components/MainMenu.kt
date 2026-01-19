package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainMenu(
    billingAppNameResId: Int,
    billingStatus: BillingStatus,
    changelogShown: Boolean = true,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var menuExpanded by retain { mutableStateOf(false) }

    Box {
        IconButton(
            { menuExpanded = true },
            Modifier
                .padding(end = spacing.windowPadding - spacing.builtInTopBarPaddingEnd)
                .testTag("geoShareMainMenuButton"),
        ) {
            BadgedBox(
                badge = {
                    if (!changelogShown) {
                        Badge(Modifier.testTag("geoShareMainMenuBadge"))
                    }
                },
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = stringResource(R.string.nav_menu_content_description),
                )
            }
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.semantics { testTagsAsResourceId = true },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.user_preferences_title)) },
                modifier = Modifier.testTag("geoShareMainMenuUserPreferences"),
                onClick = {
                    menuExpanded = false
                    onNavigateToUserPreferencesScreen()
                },
                leadingIcon = {
                    Icon(Icons.Default.Settings, null)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.faq_title)) },
                onClick = {
                    menuExpanded = false
                    onNavigateToFaqScreen()
                },
                leadingIcon = {},
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.intro_title)) },
                onClick = {
                    menuExpanded = false
                    onNavigateToIntroScreen()
                },
                leadingIcon = {
                    Icon(painterResource(R.drawable.rocket_launch_24px), contentDescription = null)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.inputs_title)) },
                modifier = Modifier.testTag("geoShareMainMenuInputs"),
                onClick = {
                    menuExpanded = false
                    onNavigateToInputsScreen()
                },
                leadingIcon = {
                    BadgedBox(
                        badge = {
                            if (!changelogShown) {
                                Badge()
                            }
                        },
                    ) {
                        Icon(Icons.Outlined.Info, null)
                    }
                },
            )
            if (
                billingStatus is BillingStatus.Purchased && billingStatus.product.type != BillingProduct.Type.DONATION ||
                billingStatus is BillingStatus.NotPurchased
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(billingAppNameResId)) },
                    onClick = {
                        menuExpanded = false
                        onNavigateToBillingScreen()
                    },
                    leadingIcon = {},
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.about_title)) },
                onClick = {
                    menuExpanded = false
                    onNavigateToAboutScreen()
                },
                leadingIcon = {},
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        MainMenu(
                            billingAppNameResId = R.string.app_name_pro,
                            billingStatus = BillingStatus.Purchased(
                                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                                refundable = true,
                            ),
                            changelogShown = false,
                            onNavigateToAboutScreen = {},
                            onNavigateToBillingScreen = {},
                            onNavigateToFaqScreen = {},
                            onNavigateToInputsScreen = {},
                            onNavigateToIntroScreen = {},
                            onNavigateToUserPreferencesScreen = {},
                        )
                    }
                )
            },
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {}
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkPreview() {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        MainMenu(
                            billingAppNameResId = R.string.app_name_pro,
                            billingStatus = BillingStatus.Purchased(
                                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                                refundable = true,
                            ),
                            changelogShown = false,
                            onNavigateToAboutScreen = {},
                            onNavigateToBillingScreen = {},
                            onNavigateToFaqScreen = {},
                            onNavigateToInputsScreen = {},
                            onNavigateToIntroScreen = {},
                            onNavigateToUserPreferencesScreen = {},
                        )
                    }
                )
            },
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {}
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonationPreview() {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        MainMenu(
                            billingAppNameResId = R.string.app_name_pro,
                            billingStatus = BillingStatus.Purchased(
                                product = BillingProduct("test", BillingProduct.Type.DONATION),
                                refundable = true,
                            ),
                            changelogShown = false,
                            onNavigateToAboutScreen = {},
                            onNavigateToBillingScreen = {},
                            onNavigateToFaqScreen = {},
                            onNavigateToInputsScreen = {},
                            onNavigateToIntroScreen = {},
                            onNavigateToUserPreferencesScreen = {},
                        )
                    }
                )
            },
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {}
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkDonationPreview() {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        MainMenu(
                            billingAppNameResId = R.string.app_name_pro,
                            billingStatus = BillingStatus.Purchased(
                                product = BillingProduct("test", BillingProduct.Type.DONATION),
                                refundable = true,
                            ),
                            changelogShown = false,
                            onNavigateToAboutScreen = {},
                            onNavigateToBillingScreen = {},
                            onNavigateToFaqScreen = {},
                            onNavigateToInputsScreen = {},
                            onNavigateToIntroScreen = {},
                            onNavigateToUserPreferencesScreen = {},
                        )
                    }
                )
            },
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {}
        }
    }
}

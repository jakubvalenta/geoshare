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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
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
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.State
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainMenu(
    currentState: State,
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
    var expanded by retain { mutableStateOf(false) }

    if (currentState is Initial && billingStatus !is BillingStatus.Loading && billingStatus !is BillingStatus.Purchased) {
        FeatureBadgeSmall(
            onClick = onNavigateToBillingScreen,
            modifier = Modifier.testTag("geoShareMainBillingIcon"),
        )
    }
    Box(Modifier.padding(end = spacing.windowPadding - spacing.builtInTopBarPaddingEnd)) {
        IconButton(
            { expanded = true },
            Modifier.testTag("geoShareMainMenuButton"),
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
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            shape = ShapeDefaults.Large,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.user_preferences_title)) },
                modifier = Modifier.testTag("geoShareMainMenuUserPreferences"),
                onClick = {
                    expanded = false
                    onNavigateToUserPreferencesScreen()
                },
                leadingIcon = {
                    Icon(Icons.Default.Settings, null)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.faq_title)) },
                onClick = {
                    expanded = false
                    onNavigateToFaqScreen()
                },
                leadingIcon = {},
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.intro_title)) },
                onClick = {
                    expanded = false
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
                    expanded = false
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
                billingStatus is BillingStatus.Pending ||
                billingStatus is BillingStatus.NotPurchased ||
                billingStatus is BillingStatus.Purchased && billingStatus.product.type != BillingProduct.Type.DONATION
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(billingAppNameResId)) },
                    modifier = Modifier.testTag("geoShareMainMenuBilling"),
                    onClick = {
                        expanded = false
                        onNavigateToBillingScreen()
                    },
                    leadingIcon = {},
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.about_title)) },
                onClick = {
                    expanded = false
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
                            currentState = Initial(),
                            billingAppNameResId = R.string.app_name_pro,
                            billingStatus = BillingStatus.NotPurchased(),
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
                            currentState = Initial(),
                            billingAppNameResId = R.string.app_name_pro,
                            billingStatus = BillingStatus.NotPurchased(),
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
                            currentState = Initial(),
                            billingAppNameResId = R.string.app_name_pro,
                            billingStatus = BillingStatus.Purchased(
                                product = BillingProduct("test", BillingProduct.Type.DONATION),
                                expired = false,
                                refundable = true,
                                token = "test_purchased",
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
                            currentState = Initial(),
                            billingAppNameResId = R.string.app_name_pro,
                            billingStatus = BillingStatus.Purchased(
                                product = BillingProduct("test", BillingProduct.Type.DONATION),
                                expired = false,
                                refundable = true,
                                token = "test_purchased",
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

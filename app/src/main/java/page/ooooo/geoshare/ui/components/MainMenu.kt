package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainMenu(
    changelogShown: Boolean = true,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var menuExpanded by remember { mutableStateOf(false) }

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
                text = { Text(stringResource(R.string.url_converters_title)) },
                modifier = Modifier.testTag("geoShareMainMenuUrlConverters"),
                onClick = {
                    menuExpanded = false
                    onNavigateToUrlConvertersScreen()
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
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface(Modifier.size(200.dp, 400.dp)) {
            MainMenu(
                changelogShown = false,
                onNavigateToAboutScreen = {},
                onNavigateToFaqScreen = {},
                onNavigateToIntroScreen = {},
                onNavigateToUrlConvertersScreen = {},
                onNavigateToUserPreferencesScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface(Modifier.size(200.dp, 400.dp)) {
            MainMenu(
                changelogShown = false,
                onNavigateToAboutScreen = {},
                onNavigateToFaqScreen = {},
                onNavigateToIntroScreen = {},
                onNavigateToUrlConvertersScreen = {},
                onNavigateToUserPreferencesScreen = {},
            )
        }
    }
}

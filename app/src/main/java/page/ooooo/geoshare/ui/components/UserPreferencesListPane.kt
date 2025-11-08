package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.UserPreferencesGroup
import page.ooooo.geoshare.ui.UserPreferencesGroupId
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesListPane(
    currentGroup: UserPreferencesGroup?,
    groups: List<UserPreferencesGroup>,
    expanded: Boolean,
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onNavigateToGroup: (id: UserPreferencesGroupId) -> Unit,
) {
    val containerColor = if (expanded) {
        Color.Unspecified
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val selectedContainerColor = if (expanded) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    TopAppBar(
        title = {},
        navigationIcon = {
            FilledIconButton(
                onBack,
                Modifier.testTag("geoShareUserPreferencesBack"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back_content_description)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
        ),
    )
    Column(
        Modifier
            .background(containerColor)
            .fillMaxHeight()
    ) {
        Headline(stringResource(R.string.user_preferences_title))
        for (group in groups) {
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(group.titleResId),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier
                    .clickable(onClick = { onNavigateToGroup(group.id) })
                    .testTag("geoShareUserPreferencesGroup_${group.id}"),
                supportingContent = group.userPreferences.takeIf { it.size == 1 }?.firstOrNull()
                    ?.let { userPreference ->
                        {
                            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                                userPreference.ValueLabel(userPreferencesValues)
                            }
                        }
                    },
                colors = ListItemDefaults.colors(
                    containerColor = if (currentGroup == group) selectedContainerColor else containerColor
                ),
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesListPane(
                    currentGroup = UserPreferencesGroup.connectionPermission,
                    groups = UserPreferencesGroup.all,
                    expanded = false,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToGroup = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesListPane(
                    currentGroup = UserPreferencesGroup.connectionPermission,
                    groups = UserPreferencesGroup.all,
                    expanded = false,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToGroup = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandedPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesListPane(
                    currentGroup = UserPreferencesGroup.connectionPermission,
                    groups = UserPreferencesGroup.all,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToGroup = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkExpandedPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesListPane(
                    currentGroup = UserPreferencesGroup.connectionPermission,
                    groups = UserPreferencesGroup.all,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToGroup = {},
                )
            }
        }
    }
}

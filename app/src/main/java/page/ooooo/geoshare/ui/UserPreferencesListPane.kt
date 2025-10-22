package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.components.Headline
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun UserPreferencesListPane(
    currentGroup: UserPreferencesGroup?,
    groups: List<UserPreferencesGroup>,
    expanded: Boolean,
    userPreferencesValues: UserPreferencesValues,
    onNavigateToGroup: (id: UserPreferencesGroupId) -> Unit,
) {
    val spacing = LocalSpacing.current
    if (expanded) {
        Headline(stringResource(R.string.user_preferences_title))
    } else {
        Spacer(Modifier.height(spacing.large))
    }
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
            supportingContent = group.userPreferences.takeIf { it.size == 1 }?.firstOrNull()?.let { userPreference ->
                {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                        userPreference.ValueLabel(userPreferencesValues)
                    }
                }
            },
            tonalElevation = if (currentGroup == group) 2.dp else ListItemDefaults.Elevation,
        )
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
                    onNavigateToGroup = {},
                )
            }
        }
    }
}

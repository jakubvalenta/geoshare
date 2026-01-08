package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.billing.FeatureStatus
import page.ooooo.geoshare.ui.AutomationDelayUserPreferencesGroup
import page.ooooo.geoshare.ui.AutomationUserPreferencesGroup
import page.ooooo.geoshare.ui.ConnectionPermissionUserPreferencesGroup
import page.ooooo.geoshare.ui.DeveloperOptionsUserPreferencesGroup
import page.ooooo.geoshare.ui.UserPreferencesGroup
import page.ooooo.geoshare.ui.UserPreferencesGroupId
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

    ScrollablePane(
        titleResId = R.string.user_preferences_title,
        containerColor = containerColor,
        onBack = onBack,
    ) {
        groups.filter { it.visible }.forEach { group ->
            val enabled = group.enabled(userPreferencesValues)
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(group.titleResId),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier
                    .alpha(if (enabled) 1f else 0.7f)
                    .clickable(enabled = enabled, onClick = { onNavigateToGroup(group.id) })
                    .testTag("geoShareUserPreferencesGroup_${group.id}"),
                supportingContent = group.userPreferences.takeIf { it.size == 1 }?.firstOrNull()
                    ?.let { userPreference ->
                        {
                            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                                userPreference.ValueLabel(userPreferencesValues)
                            }
                        }
                    },
                trailingContent = if (group.featureStatus == FeatureStatus.NOT_AVAILABLE) {
                    {
                        FeatureBadgeSmall(onClick = { onNavigateToGroup(group.id) })
                    }
                } else {
                    null
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
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(featureStatus = FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(featureStatus = FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
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
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(featureStatus = FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(featureStatus = FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
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
private fun FeatureEnabledPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesListPane(
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(featureStatus = FeatureStatus.AVAILABLE),
                        AutomationDelayUserPreferencesGroup(featureStatus = FeatureStatus.AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
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
private fun DarkFeatureEnabledPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesListPane(
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(featureStatus = FeatureStatus.AVAILABLE),
                        AutomationDelayUserPreferencesGroup(featureStatus = FeatureStatus.AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
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
private fun FeatureNotEnabledPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesListPane(
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(featureStatus = FeatureStatus.NOT_AVAILABLE),
                        AutomationDelayUserPreferencesGroup(featureStatus = FeatureStatus.NOT_AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
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
private fun DarkFeatureNotEnabledPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesListPane(
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(featureStatus = FeatureStatus.NOT_AVAILABLE),
                        AutomationDelayUserPreferencesGroup(featureStatus = FeatureStatus.NOT_AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
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
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(featureStatus = FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(featureStatus = FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
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
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(featureStatus = FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(featureStatus = FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToGroup = {},
                )
            }
        }
    }
}

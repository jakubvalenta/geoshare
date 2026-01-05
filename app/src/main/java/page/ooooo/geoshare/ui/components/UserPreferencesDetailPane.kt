package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.outputs.GpxOutput
import page.ooooo.geoshare.ui.AutomationDelayUserPreferencesGroup
import page.ooooo.geoshare.ui.AutomationUserPreferencesGroup
import page.ooooo.geoshare.ui.ConnectionPermissionUserPreferencesGroup
import page.ooooo.geoshare.ui.DeveloperOptionsUserPreferencesGroup
import page.ooooo.geoshare.ui.UserPreferencesGroup
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun UserPreferencesDetailPane(
    currentGroup: UserPreferencesGroup,
    expanded: Boolean,
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
) {
    val spacing = LocalSpacing.current

    ScrollablePane(
        titleResId = currentGroup.titleResId,
        onBack = onBack.takeIf { expanded },
    ) {
        Column(
            Modifier.padding(horizontal = spacing.windowPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            for (userPreference in currentGroup.userPreferences) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                    if (currentGroup.userPreferences.size > 1) {
                        ParagraphHtml(userPreference.title())
                    }
                    userPreference.description()?.let { description ->
                        ParagraphHtml(description)
                    }
                    userPreference.Component(userPreferencesValues) { transform ->
                        onValueChange(transform)
                    }
                }
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun ConnectionPermissionPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkConnectionPermissionPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = ConnectionPermissionUserPreferencesGroup,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = AutomationUserPreferencesGroup,
                    expanded = true,
                    userPreferencesValues = UserPreferencesValues(
                        automationValue = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = AutomationUserPreferencesGroup,
                    expanded = true,
                    userPreferencesValues = UserPreferencesValues(
                        automationValue = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationDelayPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = AutomationDelayUserPreferencesGroup,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationDelayPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = AutomationDelayUserPreferencesGroup,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeveloperOptionsPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = DeveloperOptionsUserPreferencesGroup,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDeveloperOptionsPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = DeveloperOptionsUserPreferencesGroup,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

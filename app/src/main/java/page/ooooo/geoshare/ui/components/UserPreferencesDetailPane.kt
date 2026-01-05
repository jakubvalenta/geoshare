package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.UserPreferencesGroup
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Suppress("SameParameterValue")
private fun createLinearFunc(x1: Float, y1: Float, x2: Float, y2: Float): (x: Int) -> Float {
    val a = (y2 - y1) / (x2 - x1)
    val b = y1 - a * x1
    return fun(x: Int) = a * x + b
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesDetailPane(
    currentGroup: UserPreferencesGroup,
    expanded: Boolean,
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
) {
    val density = LocalDensity.current
    val spacing = LocalSpacing.current

    val scrollState = rememberScrollState()
    val headlineHeightPx = with(density) { MaterialTheme.typography.headlineMedium.fontSize.toPx() }
    val headlinePaddingTopPx = with(density) { spacing.large.toPx() }
    val headlinePaddingBottomPx = with(density) { spacing.medium.toPx() }
    val calcHeadlineAlpha = createLinearFunc(
        headlinePaddingTopPx, 1f,
        headlinePaddingTopPx + headlineHeightPx, 0f,
    )
    val calcTitleAlpha = createLinearFunc(
        headlinePaddingTopPx + headlineHeightPx, 0f,
        headlinePaddingTopPx + headlineHeightPx + headlinePaddingBottomPx, 1f,
    )
    val headlineAlpha by remember {
        derivedStateOf {
            calcHeadlineAlpha(scrollState.value).coerceIn(0f, 1f)
        }
    }
    val titleAlpha by remember {
        derivedStateOf {
            calcTitleAlpha(scrollState.value).coerceIn(0f, 1f)
        }
    }

    TopAppBar(
        title = {
            Text(
                stringResource(currentGroup.titleResId),
                Modifier.graphicsLayer { alpha = titleAlpha },
            )
        },
        navigationIcon = {
            if (expanded) {
                IconButton(onBack, Modifier.testTag("geoShareUserPreferencesBack")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back_content_description)
                    )
                }
            }
        }
    )
    Column(Modifier.verticalScroll(scrollState)) {
        Headline(
            stringResource(currentGroup.titleResId),
            Modifier.graphicsLayer { alpha = headlineAlpha },
        )
        Column(
            Modifier.padding(horizontal = spacing.windowPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            for (userPreference in currentGroup.userPreferences) {
                if (currentGroup.userPreferences.size > 1) {
                    ParagraphHtml(
                        userPreference.title(),
                        Modifier.padding(bottom = spacing.tiny)
                    )
                }
                userPreference.description()?.let { description ->
                    ParagraphHtml(
                        description,
                        Modifier.padding(bottom = spacing.tiny)
                    )
                }
                userPreference.Component(userPreferencesValues) { transform ->
                    onValueChange(transform)
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
                    currentGroup = UserPreferencesGroup.connectionPermission,
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
                    currentGroup = UserPreferencesGroup.connectionPermission,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=1600px,dpi=440")
@Composable
private fun AutomationPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = UserPreferencesGroup.automation,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=1600px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DarkAutomationPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesDetailPane(
                    currentGroup = UserPreferencesGroup.automation,
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
                    currentGroup = UserPreferencesGroup.developerOptions,
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
                    currentGroup = UserPreferencesGroup.developerOptions,
                    expanded = true,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

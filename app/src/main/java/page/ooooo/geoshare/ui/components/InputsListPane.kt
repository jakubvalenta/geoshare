package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.inputs.InputDocumentation
import page.ooooo.geoshare.lib.inputs.InputDocumentationId
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun InputsListPane(
    currentDocumentation: InputDocumentation?,
    documentations: List<InputDocumentation>,
    expanded: Boolean,
    changelogShownForVersionCode: Int?,
    onBack: () -> Unit,
    onNavigateToDocumentation: (id: InputDocumentationId) -> Unit,
) {
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Do nothing
    }

    InputsListPane(
        currentDocumentation = currentDocumentation,
        documentations = documentations,
        expanded = expanded,
        changelogShownForVersionCode = changelogShownForVersionCode,
        onBack = onBack,
        onNavigateToDocumentation = onNavigateToDocumentation,
        onShowOpenByDefaultSettings = { AndroidTools.showOpenByDefaultSettings(context, settingsLauncher) },
    )
}

@Composable
private fun InputsListPane(
    currentDocumentation: InputDocumentation?,
    documentations: List<InputDocumentation>,
    expanded: Boolean,
    changelogShownForVersionCode: Int?,
    onBack: () -> Unit,
    onNavigateToDocumentation: (id: InputDocumentationId) -> Unit,
    onShowOpenByDefaultSettings: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)

    val containerColor = if (expanded) {
        Color.Unspecified
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val recentDocumentations = changelogShownForVersionCode?.let { changelogShownForVersionCode ->
        documentations.filter { documentation ->
            documentation.items.any { it.addedInVersionCode > changelogShownForVersionCode }
        }.takeIf { it.isNotEmpty() }
    }

    ScrollablePane(
        titleResId = R.string.inputs_title,
        containerColor = containerColor,
        onBack = onBack,
    ) {
        if (expanded) {
            Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                ParagraphText(
                    stringResource(R.string.inputs_list_text, appName),
                    Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.mediumAdaptive),
                )
                InputsSettingsButton(onShowOpenByDefaultSettings)
            }
        }
        if (recentDocumentations != null) {
            LabelLarge(
                stringResource(R.string.inputs_recent),
                color = MaterialTheme.colorScheme.error,
            )
            InputsListDocumentations(
                currentDocumentation = currentDocumentation,
                documentations = recentDocumentations,
                containerColor = containerColor,
                onNavigateToDocumentation = onNavigateToDocumentation,
                testTagPrefix = "geoShareInputsDocumentationRecent_",
            )
            LabelLarge(stringResource(R.string.inputs_all))
        } else {
            Spacer(Modifier.height(spacing.mediumAdaptive))
        }
        InputsListDocumentations(
            currentDocumentation = currentDocumentation,
            documentations = documentations,
            containerColor = containerColor,
            onNavigateToDocumentation = onNavigateToDocumentation,
            testTagPrefix = "geoShareInputsDocumentationAll_",
        )
    }
}

@Composable
private fun InputsListDocumentations(
    currentDocumentation: InputDocumentation?,
    documentations: List<InputDocumentation>,
    containerColor: Color,
    onNavigateToDocumentation: (id: InputDocumentationId) -> Unit,
    testTagPrefix: String,
) {
    val spacing = LocalSpacing.current
    ElevatedCard(Modifier.padding(horizontal = spacing.windowPadding)) {
        documentations
            .map { documentation -> Pair(documentation, stringResource(documentation.nameResId)) }
            .sortedBy { (_, name) -> name }
            .forEachIndexed { i, (documentation, name) ->
                ListItem(
                    headlineContent = {
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                    },
                    modifier = Modifier
                        .clickable(onClick = { onNavigateToDocumentation(documentation.id) }, role = Role.Button)
                        .testTag("${testTagPrefix}${documentation.id}"),
                    colors = ListItemDefaults.colors(
                        containerColor = if (currentDocumentation == documentation) {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                    ),
                )
                if (i != documentations.size - 1) {
                    HorizontalDivider(color = containerColor)
                }
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
                InputsListPane(
                    currentDocumentation = null,
                    documentations = allInputs.map { it.documentation },
                    expanded = true,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                    onNavigateToDocumentation = {},
                    onShowOpenByDefaultSettings = {},
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
                InputsListPane(
                    currentDocumentation = null,
                    documentations = allInputs.map { it.documentation },
                    expanded = true,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                    onNavigateToDocumentation = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoRecentPreview() {
    AppTheme {
        Surface {
            Column {
                InputsListPane(
                    currentDocumentation = null,
                    documentations = allInputs.map { it.documentation },
                    expanded = true,
                    changelogShownForVersionCode = 999,
                    onBack = {},
                    onNavigateToDocumentation = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoRecentPreview() {
    AppTheme {
        Surface {
            Column {
                InputsListPane(
                    currentDocumentation = null,
                    documentations = allInputs.map { it.documentation },
                    expanded = true,
                    changelogShownForVersionCode = 999,
                    onBack = {},
                    onNavigateToDocumentation = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotExpandedPreview() {
    AppTheme {
        Surface {
            Column {
                InputsListPane(
                    currentDocumentation = null,
                    documentations = allInputs.map { it.documentation },
                    expanded = false,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                    onNavigateToDocumentation = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNotExpandedPreview() {
    AppTheme {
        Surface {
            Column {
                InputsListPane(
                    currentDocumentation = null,
                    documentations = allInputs.map { it.documentation },
                    expanded = false,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                    onNavigateToDocumentation = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotExpandedNoRecentPreview() {
    AppTheme {
        Surface {
            Column {
                InputsListPane(
                    currentDocumentation = null,
                    documentations = allInputs.map { it.documentation },
                    expanded = false,
                    changelogShownForVersionCode = 999,
                    onBack = {},
                    onNavigateToDocumentation = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNotExpandedNoRecentPreview() {
    AppTheme {
        Surface {
            Column {
                InputsListPane(
                    currentDocumentation = null,
                    documentations = allInputs.map { it.documentation },
                    expanded = false,
                    changelogShownForVersionCode = 999,
                    onBack = {},
                    onNavigateToDocumentation = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

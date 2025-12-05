package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputsListPane(
    currentDocumentation: Input.Documentation?,
    documentations: List<Input.Documentation>,
    expanded: Boolean,
    changelogShownForVersionCode: Int?,
    onBack: () -> Unit,
    onNavigateToDocumentation: (id: Input.DocumentationId) -> Unit,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputsListPane(
    currentDocumentation: Input.Documentation?,
    documentations: List<Input.Documentation>,
    expanded: Boolean,
    changelogShownForVersionCode: Int?,
    onBack: () -> Unit,
    onNavigateToDocumentation: (id: Input.DocumentationId) -> Unit,
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
            documentation.inputs.any { it.addedInVersionCode > changelogShownForVersionCode }
        }.takeIf { it.isNotEmpty() }
    }

    TopAppBar(
        title = {},
        navigationIcon = {
            FilledIconButton(
                onBack,
                Modifier.testTag("geoShareInputsBack"),
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
            .verticalScroll(rememberScrollState()),
    ) {
        Headline(stringResource(R.string.url_converters_title))
        Column(
            Modifier.padding(horizontal = spacing.windowPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                stringResource(R.string.url_converters_text, appName),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineBreak = LineBreak.Paragraph,
                ),
            )
            if (expanded) {
                Button({ onShowOpenByDefaultSettings() }) {
                    Text(stringResource(R.string.url_converters_settings_button))
                }
            }
        }
        if (recentDocumentations != null) {
            LabelLarge(stringResource(R.string.url_converters_filter_recent))
            InputsListDocumentations(
                currentDocumentation = currentDocumentation,
                documentations = recentDocumentations,
                containerColor = containerColor,
                onNavigateToDocumentation = onNavigateToDocumentation,
            )
        }
        LabelLarge(stringResource(R.string.url_converters_filter_all))
        InputsListDocumentations(
            currentDocumentation = currentDocumentation,
            documentations = documentations,
            containerColor = containerColor,
            onNavigateToDocumentation = onNavigateToDocumentation,
        )
    }
}

@Composable
private fun InputsListDocumentations(
    currentDocumentation: Input.Documentation?,
    documentations: List<Input.Documentation>,
    containerColor: Color,
    onNavigateToDocumentation: (id: Input.DocumentationId) -> Unit,
) {
    val spacing = LocalSpacing.current
    ElevatedCard(Modifier.padding(horizontal = spacing.windowPadding)) {
        documentations.forEachIndexed { i, documentation ->
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(documentation.nameResId),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier
                    .clickable(onClick = { onNavigateToDocumentation(documentation.id) })
                    .testTag("geoShareInputsDocumentation_${documentation.id}"),
                colors = ListItemDefaults.colors(
                    containerColor = if (currentDocumentation == documentation) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHigh
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
private fun DarkPreview() {
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
private fun ExpandedPreview() {
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
private fun DarkExpandedPreview() {
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
private fun DarkNoRecentPreview() {
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

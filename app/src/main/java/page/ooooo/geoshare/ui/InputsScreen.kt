package page.ooooo.geoshare.ui

import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.inputs.InputDocumentation
import page.ooooo.geoshare.lib.inputs.InputDocumentationId
import page.ooooo.geoshare.lib.inputs.InputDocumentationItem
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.ui.components.BasicListDetailScaffold
import page.ooooo.geoshare.ui.components.InputsSettingsButton
import page.ooooo.geoshare.ui.components.LabelLarge
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputsScreen(
    initialDocumentationId: InputDocumentationId?,
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val changelogShownForVersionCode by viewModel.changelogShownForVersionCode.collectAsStateWithLifecycle()

    InputsScreen(
        changelogShownForVersionCode = changelogShownForVersionCode,
        initialDocumentationId = initialDocumentationId,
        onBack = {
            viewModel.setChangelogShown()
            onBack()
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InputsScreen(
    changelogShownForVersionCode: Int?,
    initialDocumentationId: InputDocumentationId?,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = listOf(
            if (initialDocumentationId == null) {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List)
            } else {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, initialDocumentationId)
            },
        ),
    )
    val documentations = remember {
        allInputs.map { input -> input.documentation }
    }
    val currentDocumentation = remember(navigator.currentDestination, documentations) {
        navigator.currentDestination?.contentKey?.let { id -> documentations.find { it.id == id } }
    }

    BackHandler {
        onBack()
    }

    BasicListDetailScaffold(
        navigator = navigator,
        listPane = { wide, containerColor ->
            InputsListPane(
                currentDocumentation = currentDocumentation,
                documentations = documentations,
                changelogShownForVersionCode = changelogShownForVersionCode,
                containerColor = containerColor,
                wide = wide,
                onBack = {
                    coroutineScope.launch {
                        if (navigator.canNavigateBack()) {
                            navigator.navigateBack()
                        } else {
                            onBack()
                        }
                    }
                },
                onNavigateToDocumentation = { id ->
                    coroutineScope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                },
            )
        },
        detailPane = { wide ->
            if (currentDocumentation != null) {
                InputsDetailPane(
                    currentDocumentation = currentDocumentation,
                    wide = wide,
                    onBack = {
                        coroutineScope.launch {
                            if (navigator.canNavigateBack()) {
                                navigator.navigateBack()
                            } else {
                                onBack()
                            }
                        }
                    },
                )
            }
        },
        listContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@Composable
private fun InputsListPane(
    currentDocumentation: InputDocumentation?,
    documentations: List<InputDocumentation>,
    changelogShownForVersionCode: Int?,
    containerColor: Color,
    wide: Boolean,
    onBack: () -> Unit,
    onNavigateToDocumentation: (id: InputDocumentationId) -> Unit,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)

    val recentDocumentations = remember(changelogShownForVersionCode, documentations) {
        changelogShownForVersionCode?.let { changelogShownForVersionCode ->
            documentations.filter { documentation ->
                documentation.items.any { it.addedInVersionCode > changelogShownForVersionCode }
            }.takeIf { it.isNotEmpty() }
        }
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Do nothing
    }

    ScrollablePane(
        titleResId = R.string.inputs_title,
        onBack = onBack,
    ) {
        if (!wide) {
            Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                ParagraphText(
                    stringResource(R.string.inputs_list_text, appName),
                    Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.mediumAdaptive),
                )
                InputsSettingsButton {
                    AndroidTools.showOpenByDefaultSettings(context, settingsLauncher)
                }
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

private data class DocumentationInputDetails(
    val documentationInput: InputDocumentationItem,
    val defaultHandlerEnabled: Boolean?,
)

private fun getDocumentationInputDetails(
    documentation: InputDocumentation,
    packageManager: PackageManager,
): List<DocumentationInputDetails> =
    documentation.items.map { documentationInput ->
        DocumentationInputDetails(
            documentationInput,
            if (documentationInput is InputDocumentationItem.Url) {
                AndroidTools.isDefaultHandlerEnabled(packageManager, documentationInput.urlString)
            } else {
                null
            },
        )
    }

@Composable
private fun InputsDetailPane(
    currentDocumentation: InputDocumentation,
    wide: Boolean,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)

    var documentationInputDetailsList by remember(currentDocumentation) {
        mutableStateOf(getDocumentationInputDetails(currentDocumentation, context.packageManager))
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        documentationInputDetailsList = getDocumentationInputDetails(currentDocumentation, context.packageManager)
    }

    ScrollablePane(
        titleResId = currentDocumentation.nameResId,
        onBack = onBack.takeUnless { wide },
    ) {
        Column(
            Modifier
                .widthIn(max = 600.dp)
                .padding(horizontal = spacing.windowPadding),
        ) {
            ParagraphText(
                stringResource(R.string.inputs_detail_text, appName),
                Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.mediumAdaptive),
            )
            InputsSettingsButton {
                AndroidTools.showOpenByDefaultSettings(context, settingsLauncher)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.largeAdaptive, bottom = spacing.smallAdaptive),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.inputs_link),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    stringResource(R.string.inputs_default_handler, appName),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            HorizontalDivider()
            documentationInputDetailsList.forEach { documentationInputDetails ->
                Row(
                    Modifier.padding(vertical = spacing.smallAdaptive),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SelectionContainer(Modifier.weight(1f)) {
                        Text(
                            when (documentationInputDetails.documentationInput) {
                                is InputDocumentationItem.Text ->
                                    documentationInputDetails.documentationInput.text()

                                is InputDocumentationItem.Url ->
                                    documentationInputDetails.documentationInput.urlString
                                        .removePrefix("https://")
                                        .trimEnd('/')
                            },
                            Modifier.padding(end = spacing.tiny),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        stringResource(
                            when (documentationInputDetails.defaultHandlerEnabled) {
                                true -> R.string.yes
                                false -> R.string.no
                                null -> R.string.not_available
                            },
                            appName,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                HorizontalDivider()
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
                InputsScreen(
                    initialDocumentationId = null,
                    changelogShownForVersionCode = 25,
                    onBack = {},
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
                InputsScreen(
                    initialDocumentationId = null,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = null,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OpenStreetMapPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.OPEN_STREET_MAP,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkOpenStreetMapPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.OPEN_STREET_MAP,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletOpenStreetMapPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.OPEN_STREET_MAP,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GeoUriPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.GEO_URI,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkGeoUriPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.GEO_URI,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletGeoUriPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.GEO_URI,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

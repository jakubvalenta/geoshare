package page.ooooo.geoshare.ui

import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.inputs.InputDocumentation
import page.ooooo.geoshare.lib.inputs.InputDocumentationId
import page.ooooo.geoshare.lib.inputs.InputDocumentationItem
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.ui.components.InputsSettingsButton
import page.ooooo.geoshare.ui.components.NavigableBasicListDetailScaffold
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.components.SegmentedList
import page.ooooo.geoshare.ui.components.SegmentedListLabel
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputsScreen(
    initialDocumentationId: InputDocumentationId?,
    onBack: () -> Unit = {},
    viewModel: InputsViewModel = hiltViewModel(),
) {
    val allDocumentations by viewModel.allDocumentations.collectAsStateWithLifecycle()
    val recentDocumentations by viewModel.recentDocumentations.collectAsStateWithLifecycle()

    InputsScreen(
        initialDocumentationId = initialDocumentationId,
        allDocumentations = allDocumentations,
        recentDocumentations = recentDocumentations,
        onBack = {
            viewModel.setChangelogShown()
            onBack()
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InputsScreen(
    initialDocumentationId: InputDocumentationId?,
    allDocumentations: List<InputDocumentation>,
    recentDocumentations: List<InputDocumentation>,
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
    val currentDocumentation = remember(navigator.currentDestination, allDocumentations) {
        navigator.currentDestination?.contentKey?.let { id -> allDocumentations.find { it.id == id } }
    }

    BackHandler {
        onBack()
    }

    NavigableBasicListDetailScaffold(
        navigator = navigator,
        listPane = { wide, containerColor ->
            InputsListPane(
                currentDocumentationId = navigator.currentDestination?.contentKey,
                allDocumentations = allDocumentations,
                recentDocumentations = recentDocumentations,
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
    currentDocumentationId: InputDocumentationId?,
    allDocumentations: List<InputDocumentation>,
    recentDocumentations: List<InputDocumentation>,
    containerColor: Color,
    wide: Boolean,
    onBack: () -> Unit,
    onNavigateToDocumentation: (id: InputDocumentationId) -> Unit,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)

    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Do nothing
    }

    ScrollablePane(
        title = {
            Text(stringResource(R.string.inputs_title))
        },
        onBack = onBack,
        modifier = Modifier.padding(horizontal = spacing.windowPadding),
        containerColor = containerColor,
    ) {
        if (!wide) {
            item {
                Column {
                    ParagraphText(
                        stringResource(R.string.inputs_list_text, appName),
                        Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.mediumAdaptive),
                    )
                    InputsSettingsButton {
                        AndroidTools.showOpenByDefaultSettings(context, settingsLauncher)
                    }
                }
            }
        }
        if (recentDocumentations.isNotEmpty()) {
            item {
                SegmentedListLabel(stringResource(R.string.inputs_recent), MaterialTheme.colorScheme.error)
            }
            item {
                SegmentedList(
                    values = recentDocumentations,
                    itemHeadline = { stringResource(it.nameResId) },
                    itemIsSelected = { it.id == currentDocumentationId },
                    itemOnClick = { onNavigateToDocumentation(it.id) },
                    itemTestTag = { "geoShareInputsDocumentationRecent_${it.id}" },
                    sort = true,
                )
            }
            item {
                SegmentedListLabel(stringResource(R.string.inputs_all))
            }
        } else {
            item {
                Spacer(Modifier.height(spacing.mediumAdaptive))
            }
        }
        item {
            SegmentedList(
                values = allDocumentations,
                itemHeadline = { stringResource(it.nameResId) },
                itemIsSelected = { it.id == currentDocumentationId },
                itemOnClick = { onNavigateToDocumentation(it.id) },
                itemTestTag = { "geoShareInputsDocumentationAll_${it.id}" },
                sort = true,
            )
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
    val maxWidth = 600.dp

    var documentationInputDetailsList by remember(currentDocumentation) {
        mutableStateOf(getDocumentationInputDetails(currentDocumentation, context.packageManager))
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        documentationInputDetailsList = getDocumentationInputDetails(currentDocumentation, context.packageManager)
    }

    ScrollablePane(
        title = {
            Text(stringResource(currentDocumentation.nameResId))
        },
        onBack = onBack.takeUnless { wide },
        modifier = Modifier.padding(horizontal = spacing.windowPadding),
    ) {
        item {
            ParagraphText(
                stringResource(R.string.inputs_detail_text, appName),
                Modifier
                    .widthIn(max = maxWidth)
                    .padding(top = spacing.tinyAdaptive, bottom = spacing.mediumAdaptive),
            )
        }
        item {
            InputsSettingsButton {
                AndroidTools.showOpenByDefaultSettings(context, settingsLauncher)
            }
        }
        item {
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
        }
        item {
            HorizontalDivider()
        }
        documentationInputDetailsList.forEach { documentationInputDetails ->
            item {
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
                    onBack = {},
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
                InputsScreen(
                    initialDocumentationId = null,
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = emptyList(),
                    onBack = {},
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
                InputsScreen(
                    initialDocumentationId = null,
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = emptyList(),
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletNoRecentPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = null,
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = emptyList(),
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
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
                    allDocumentations = allInputs.map { it.documentation },
                    recentDocumentations = allInputs.map { it.documentation }.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
                    onBack = {},
                )
            }
        }
    }
}

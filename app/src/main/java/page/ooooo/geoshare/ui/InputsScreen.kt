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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.extensions.trimUrl
import page.ooooo.geoshare.lib.inputs.InputDocumentation
import page.ooooo.geoshare.lib.inputs.InputDocumentationGroup
import page.ooooo.geoshare.lib.inputs.InputDocumentationItem
import page.ooooo.geoshare.ui.components.InputsSettingsButton
import page.ooooo.geoshare.ui.components.NavigableStyledListDetailPaneScaffold
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.components.SegmentedList
import page.ooooo.geoshare.ui.components.SegmentedListLabel
import page.ooooo.geoshare.ui.components.StyledPaneScaffoldDefaults
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun InputsScreen(
    initialDocumentationGroup: InputDocumentationGroup?,
    onBack: () -> Unit = {},
    viewModel: InputViewModel = hiltViewModel(),
) {
    val allDocumentations by viewModel.allDocumentations.collectAsStateWithLifecycle()
    val recentDocumentations by viewModel.recentDocumentations.collectAsStateWithLifecycle()

    InputsScreen(
        initialDocumentationGroup = initialDocumentationGroup,
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
    initialDocumentationGroup: InputDocumentationGroup?,
    allDocumentations: List<InputDocumentation>,
    recentDocumentations: List<InputDocumentation>,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = listOf(
            if (initialDocumentationGroup == null) {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List)
            } else {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, initialDocumentationGroup)
            },
        ),
    )
    val currentDocumentation = remember(navigator.currentDestination, allDocumentations) {
        navigator.currentDestination?.contentKey?.let { id -> allDocumentations.find { it.group == id } }
    }

    BackHandler {
        onBack()
    }

    NavigableStyledListDetailPaneScaffold(
        navigator = navigator,
        listPane = { wide ->
            InputsListPane(
                currentDocumentationGroup = navigator.currentDestination?.contentKey,
                allDocumentations = allDocumentations,
                recentDocumentations = recentDocumentations,
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
        colors = StyledPaneScaffoldDefaults.colors(
            wideMainContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

@Composable
private fun InputsListPane(
    currentDocumentationGroup: InputDocumentationGroup?,
    allDocumentations: List<InputDocumentation>,
    recentDocumentations: List<InputDocumentation>,
    wide: Boolean,
    onBack: () -> Unit,
    onNavigateToDocumentation: (id: InputDocumentationGroup) -> Unit,
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
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = spacing.windowPadding)
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
                        itemHeadline = { stringResource(it.group.nameResId) },
                        itemIsSelected = { it.group == currentDocumentationGroup },
                        itemOnClick = { onNavigateToDocumentation(it.group) },
                        itemTestTag = { "geoShareInputsDocumentationRecent_${it.group}" },
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
                    itemHeadline = { stringResource(it.group.nameResId) },
                    itemIsSelected = { it.group == currentDocumentationGroup },
                    itemOnClick = { onNavigateToDocumentation(it.group) },
                    itemTestTag = { "geoShareInputsDocumentationAll_${it.group}" },
                    sort = true,
                )
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
    val maxWidth = 600.dp

    var documentationInputDetailsList by remember(currentDocumentation) {
        mutableStateOf(getDocumentationInputDetails(currentDocumentation, context.packageManager))
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        documentationInputDetailsList = getDocumentationInputDetails(currentDocumentation, context.packageManager)
    }

    ScrollablePane(
        title = {
            Text(stringResource(currentDocumentation.group.nameResId))
        },
        onBack = onBack.takeUnless { wide },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = spacing.windowPadding)
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
                                        documentationInputDetails.documentationInput.urlString.trimUrl()
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
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = null,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = null,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = null,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = null,
                    allDocumentations = allDocumentations,
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = null,
                    allDocumentations = allDocumentations,
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = null,
                    allDocumentations = allDocumentations,
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = InputDocumentationGroup.OPEN_STREET_MAP,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = InputDocumentationGroup.OPEN_STREET_MAP,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = InputDocumentationGroup.OPEN_STREET_MAP,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = InputDocumentationGroup.GEO_URI,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = InputDocumentationGroup.GEO_URI,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
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
                val allDocumentations = FakeInputRepository.all
                    .mapNotNull { input -> input.documentation }
                    .groupBy { documentation -> documentation.group }
                    .map { (group, documentations) ->
                        InputDocumentation(group, documentations.flatMap { it.items })
                    }
                InputsScreen(
                    initialDocumentationGroup = InputDocumentationGroup.GEO_URI,
                    allDocumentations = allDocumentations,
                    recentDocumentations = allDocumentations.filter { documentation ->
                        documentation.items.any { it.addedInVersionCode > 25 }
                    },
                    onBack = {},
                )
            }
        }
    }
}

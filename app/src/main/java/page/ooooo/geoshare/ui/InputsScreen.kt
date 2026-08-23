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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.extensions.trimUrl
import page.ooooo.geoshare.lib.inputs.InputChangelogItem
import page.ooooo.geoshare.lib.inputs.InputGroup
import page.ooooo.geoshare.lib.inputs.InputGroupId
import page.ooooo.geoshare.ui.components.InputsSettingsButton
import page.ooooo.geoshare.ui.components.LargeTopAppBarPane
import page.ooooo.geoshare.ui.components.NavigableStyledListDetailPaneScaffold
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.SegmentedList
import page.ooooo.geoshare.ui.components.SegmentedListLabel
import page.ooooo.geoshare.ui.components.StyledPaneScaffoldDefaults
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun InputsScreen(
    initialGroupId: InputGroupId?,
    onBack: () -> Unit = {},
    viewModel: InputViewModel = hiltViewModel(),
) {
    val allChangelogsByGroup by viewModel.allChangelogsByGroup.collectAsStateWithLifecycle()
    val recentChangelogsByGroup by viewModel.recentChangelogsByGroup.collectAsStateWithLifecycle()

    InputsScreen(
        initialGroupId = initialGroupId,
        allChangelogsByGroup = allChangelogsByGroup,
        recentChangelogsByGroup = recentChangelogsByGroup,
        onBack = {
            viewModel.setChangelogShown()
            onBack()
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InputsScreen(
    initialGroupId: InputGroupId?,
    allChangelogsByGroup: Map<InputGroup, ImmutableList<InputChangelogItem>>,
    recentChangelogsByGroup: Map<InputGroup, ImmutableList<InputChangelogItem>>,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = listOf(
            if (initialGroupId == null) {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List)
            } else {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, initialGroupId)
            },
        ),
    )
    val currentGroupId = remember(navigator.currentDestination, allChangelogsByGroup) {
        navigator.currentDestination?.contentKey
    }

    BackHandler {
        onBack()
    }

    NavigableStyledListDetailPaneScaffold(
        navigator = navigator,
        listPane = { wide ->
            InputsListPane(
                currentGroupId = currentGroupId,
                allChangelogsByGroup = allChangelogsByGroup,
                recentChangelogsByGroup = recentChangelogsByGroup,
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
                onNavigateToGroup = { id ->
                    coroutineScope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                },
            )
        },
        detailPane = { wide ->
            if (currentGroupId != null) {
                InputsDetailPane(
                    currentGroupId = currentGroupId,
                    allChangelogsByGroup = allChangelogsByGroup,
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
    currentGroupId: InputGroupId?,
    allChangelogsByGroup: Map<InputGroup, ImmutableList<InputChangelogItem>>,
    recentChangelogsByGroup: Map<InputGroup, ImmutableList<InputChangelogItem>>,
    wide: Boolean,
    onBack: () -> Unit,
    onNavigateToGroup: (id: InputGroupId) -> Unit,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)

    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Do nothing
    }

    LargeTopAppBarPane(
        modifier = Modifier.testTag("geoShareInputListPane"),
        title = { maxLines ->
            Text(stringResource(R.string.inputs_title), overflow = TextOverflow.Ellipsis, maxLines = maxLines)
        },
        onBack = onBack,
    ) {
        if (!wide) {
            item {
                Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                    ParagraphText(
                        stringResource(R.string.inputs_list_text, appName),
                        Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.mediumAdaptive),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    InputsSettingsButton {
                        AndroidTools.showOpenByDefaultSettings(context, settingsLauncher)
                    }
                }
            }
        }
        if (recentChangelogsByGroup.isNotEmpty()) {
            item {
                SegmentedListLabel(
                    stringResource(R.string.inputs_recent),
                    modifier = Modifier.padding(horizontal = spacing.windowPadding),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            item {
                SegmentedList(
                    values = recentChangelogsByGroup.keys.toList(),
                    modifier = Modifier.padding(horizontal = spacing.windowPadding),
                    itemHeadline = { stringResource(it.nameResId) },
                    itemIsSelected = { it.id == currentGroupId },
                    itemOnClick = { onNavigateToGroup(it.id) },
                    itemTestTag = { "geoShareInputListRecent_${it.id}" },
                    sort = true,
                )
            }
            item {
                SegmentedListLabel(
                    stringResource(R.string.inputs_all),
                    modifier = Modifier.padding(horizontal = spacing.windowPadding),
                )
            }
        } else {
            item {
                Spacer(Modifier.height(spacing.mediumAdaptive))
            }
        }
        item {
            SegmentedList(
                values = allChangelogsByGroup.keys.toList(),
                modifier = Modifier.padding(horizontal = spacing.windowPadding),
                itemHeadline = { stringResource(it.nameResId) },
                itemIsSelected = { it.id == currentGroupId },
                itemOnClick = { onNavigateToGroup(it.id) },
                itemTestTag = { "geoShareInputListAll_${it.id}" },
                sort = true,
            )
        }
    }
}

private data class ChangelogItemDetails(
    val changelogItem: InputChangelogItem,
    val defaultHandlerEnabled: Boolean?,
)

private fun getChangelogDetails(
    changelog: ImmutableList<InputChangelogItem>,
    packageManager: PackageManager,
): ImmutableList<ChangelogItemDetails> =
    changelog.map { changelogItem ->
        ChangelogItemDetails(
            changelogItem,
            if (changelogItem is InputChangelogItem.Url) {
                AndroidTools.isDefaultHandlerEnabled(packageManager, changelogItem.urlString)
            } else {
                null
            },
        )
    }.toImmutableList()

@Composable
private fun InputsDetailPane(
    currentGroupId: InputGroupId?,
    allChangelogsByGroup: Map<InputGroup, ImmutableList<InputChangelogItem>>,
    wide: Boolean,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)
    val maxWidth = 600.dp

    val (group, changelog) = remember(currentGroupId) {
        allChangelogsByGroup.entries.firstOrNull { (group) -> group.id == currentGroupId }
    } ?: return
    var changelogDetails by remember(changelog) {
        mutableStateOf(getChangelogDetails(changelog, context.packageManager))
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        changelogDetails = getChangelogDetails(changelog, context.packageManager)
    }

    LargeTopAppBarPane(
        title = { maxLines ->
            Text(
                stringResource(group.nameResId),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines,
            )
        },
        onBack = onBack.takeUnless { wide },
    ) {
        item {
            ParagraphText(
                stringResource(R.string.inputs_detail_text, appName),
                Modifier
                    .widthIn(max = maxWidth)
                    .padding(horizontal = spacing.windowPadding)
                    .padding(top = spacing.tinyAdaptive, bottom = spacing.mediumAdaptive),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            InputsSettingsButton(Modifier.padding(horizontal = spacing.windowPadding)) {
                AndroidTools.showOpenByDefaultSettings(context, settingsLauncher)
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.windowPadding)
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
            HorizontalDivider(Modifier.padding(horizontal = spacing.windowPadding))
        }
        changelogDetails.forEach { changelogDetails ->
            item {
                Row(
                    Modifier
                        .padding(horizontal = spacing.windowPadding)
                        .padding(vertical = spacing.smallAdaptive),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SelectionContainer(Modifier.weight(1f)) {
                        Text(
                            when (changelogDetails.changelogItem) {
                                is InputChangelogItem.Text -> changelogDetails.changelogItem.text()
                                is InputChangelogItem.Url -> changelogDetails.changelogItem.urlString.trimUrl()
                            },
                            Modifier.padding(end = spacing.tiny),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        stringResource(
                            when (changelogDetails.defaultHandlerEnabled) {
                                true -> R.string.yes
                                false -> R.string.no
                                null -> R.string.not_available
                            },
                            appName,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                HorizontalDivider(Modifier.padding(horizontal = spacing.windowPadding))
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = null,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = null,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = null,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = null,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = emptyMap(),
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = null,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = emptyMap(),
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = null,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = emptyMap(),
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = InputGroupId.OPEN_STREET_MAP,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = InputGroupId.OPEN_STREET_MAP,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = InputGroupId.OPEN_STREET_MAP,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = InputGroupId.GEO_URI,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = InputGroupId.GEO_URI,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
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
                val allChangelogsByGroup = FakeInputRepository.all
                    .mapNotNull { input -> input.group?.let { group -> group to input } }
                    .groupBy { (group) -> group }
                    .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
                InputsScreen(
                    initialGroupId = InputGroupId.GEO_URI,
                    allChangelogsByGroup = allChangelogsByGroup,
                    recentChangelogsByGroup = allChangelogsByGroup.filterValues { changelog ->
                        changelog.any { it.addedInVersionCode > 25 }
                    },
                    onBack = {},
                )
            }
        }
    }
}

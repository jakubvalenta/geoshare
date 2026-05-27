package page.ooooo.geoshare.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGoogleMapsServer
import page.ooooo.geoshare.data.di.defaultFakeServers
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.ui.components.ServerForm
import page.ooooo.geoshare.ui.components.BasicListDetailScaffold
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.components.segmentedListColors
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ServerScreen(
    onBack: () -> Unit,
    viewModel: ServerViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current
    val all by viewModel.all.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    ServerScreen(
        destination = viewModel.destination,
        all = all,
        selected = selected,
        message = message,
        apiKey = viewModel.apiKey,
        apiKeyHeader = viewModel.apiKeyHeader,
        authType = viewModel.authType,
        baseUrl = viewModel.baseUrl,
        onBack = onBack,
        onDelete = { viewModel.delete(resources) },
        onDismissMessage = { viewModel.dismissMessage() },
        onNavigateTo = {
            coroutineScope.launch {
                viewModel.navigateTo(it)
            }
        },
        onRestoreInitialData = { viewModel.restoreInitialData(resources) },
        onSaveForm = { viewModel.saveForm(resources) },
        onSelect = { viewModel.select(it) },
        onSetApiKey = { viewModel.apiKey = it },
        onSetApiKeyHeader = { viewModel.apiKeyHeader = it },
        onSetAuthType = { viewModel.authType = it },
        onSetBaseUrl = { viewModel.baseUrl = it },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ServerScreen(
    destination: Int?,
    all: List<Server>,
    selected: Server?,
    message: Message?,
    apiKey: String,
    apiKeyHeader: String,
    authType: ServerAuthType,
    baseUrl: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onDismissMessage: () -> Unit,
    onNavigateTo: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
    onSaveForm: () -> Unit,
    onSelect: (Int?) -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ServerAuthType) -> Unit,
    onSetBaseUrl: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Drive the scaffold navigator from view model, so that the UI state survives process death.

    val navigator = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = listOf(
            if (destination == null) {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List)
            } else {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, destination)
            },
        ),
    )

    LaunchedEffect(destination) {
        if (destination != null) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, destination)
        } else if (navigator.canNavigateBack()) {
            navigator.navigateBack()
        }
    }

    BackHandler {
        if (navigator.canNavigateBack()) {
            onNavigateTo(null)
        } else {
            onBack()
        }
    }

    // Message

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(MessageSnackbarVisuals(message))
            onDismissMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            MessageSnackbarHost(snackbarHostState)
        },
    ) {
        // Use BasicListDetailScaffold instead of NavigableBasicListDetailScaffold, because the latter navigates using
        // the navigator when back button is pressed, but we want to do all navigation ourselves using the view model.
        BasicListDetailScaffold(
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            listPane = { _, containerColor ->
                // Use destination coming from view model, because if we use navigator.currentDestination?.contentKey,
                // fields get briefly rendered with empty values when switching from detail to list.
                ServerListPane(
                    destination = destination,
                    containerColor = containerColor,
                    all = all,
                    selected = selected,
                    onBack = onBack,
                    onNavigateToContentKey = onNavigateTo,
                    onRestoreInitialData = onRestoreInitialData,
                    onSelect = onSelect,
                )
            },
            detailPane = { wide ->
                // Use destination coming from view model, because if we use navigator.currentDestination?.contentKey,
                // fields get briefly rendered with empty values when switching from detail to list.
                if (destination != null) {
                    ServerDetailPane(
                        destination = destination,
                        wide = wide,
                        baseUrl = baseUrl,
                        apiKey = apiKey,
                        apiKeyHeader = apiKeyHeader,
                        authType = authType,
                        onBack = { onNavigateTo(null) },
                        onDelete = onDelete,
                        onSaveForm = onSaveForm,
                        onSetApiKey = onSetApiKey,
                        onSetApiKeyHeader = onSetApiKeyHeader,
                        onSetAuthType = onSetAuthType,
                        onSetBaseUrl = onSetBaseUrl,
                    )
                }
            },
            listContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ServerListPane(
    destination: Int?,
    containerColor: Color,
    all: List<Server>,
    selected: Server?,
    onBack: () -> Unit,
    onSelect: (Int?) -> Unit,
    onNavigateToContentKey: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val (restoreInitialDataDialogOpen, setRestoreInitialDataDialogOpen) = retain { mutableStateOf(false) }

    ScrollablePane(
        title = {
            Text(stringResource(R.string.server_title))
        },
        onBack = onBack,
        modifier = Modifier
            .padding(horizontal = spacing.windowPadding)
            .testTag("geoShareServerListPane"),
        containerColor = containerColor,
    ) {
        item {
            ParagraphText(
                stringResource(
                    R.string.server_description,
                    stringResource(R.string.app_name),
                ),
                Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.smallAdaptive),
            )
        }
        item {
            Button(
                { onNavigateToContentKey(-1) },
                Modifier.testTag("geoShareServerListInsert"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(stringResource(R.string.server_insert))
            }
        }
        item {
            Column(
                modifier = Modifier
                    .padding(top = spacing.medium)
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                val colors = segmentedListColors()
                SegmentedListItem(
                    selected = selected == null,
                    onClick = { onSelect(null) },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = all.size + 1),
                    modifier = Modifier.testTag("geoShareServerListItem_null"),
                    leadingContent = {
                        val selected = selected == null
                        RadioButton(
                            selected = selected,
                            // Null recommended for accessibility with screen readers
                            onClick = null,
                            modifier = Modifier.testTag("geoShareServerListItemRadio_null_selected_${selected}"),
                        )
                    },
                    colors = colors,
                ) {
                    Text(stringResource(R.string.server_none_selected), style = MaterialTheme.typography.bodyLarge)
                }
                all.forEachIndexed { i, item ->
                    val valid = item.isValid()

                    SegmentedListItem(
                        selected = item.uid == destination,
                        onClick = { onSelect(item.uid) },
                        shapes = ListItemDefaults.segmentedShapes(index = i + 1, count = all.size + 1),
                        modifier = Modifier.testTag("geoShareServerListItem_${item.uuid}"),
                        enabled = valid,
                        leadingContent = {
                            val selected = item.uid == selected?.uid
                            RadioButton(
                                selected = selected,
                                // Null recommended for accessibility with screen readers
                                onClick = null,
                                modifier = Modifier.testTag("geoShareServerListItemRadio_${item.uuid}_selected_${selected}"),
                                enabled = valid,
                            )
                        },
                        trailingContent = {
                            var expanded by remember { mutableStateOf(false) }

                            Box {
                                IconButton(
                                    { expanded = true },
                                    Modifier.testTag("geoShareServerListItemMenu_${item.uuid}"),
                                ) {
                                    Icon(
                                        painterResource(R.drawable.more_vert_24px),
                                        contentDescription = stringResource(R.string.nav_menu_content_description),
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.semantics { testTagsAsResourceId = true },
                                    shape = ShapeDefaults.Large,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.server_update)) },
                                        modifier = Modifier.testTag("geoShareServerListItemMenuDetail_${item.uuid}"),
                                        onClick = {
                                            expanded = false
                                            onNavigateToContentKey(item.uid)
                                        },
                                    )
                                }
                            }
                        },
                        supportingContent = if (!valid) {
                            { Text(stringResource(R.string.server_invalid)) }
                        } else {
                            null
                        },
                        colors = colors,
                    ) {
                        Text(
                            item.name,
                            Modifier.testTag("geoShareServerListItemContent"),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
        item {
            TextButton(
                onClick = { setRestoreInitialDataDialogOpen(true) },
                modifier = Modifier
                    .testTag("geoShareServerRestoreInitialButton")
                    .padding(top = spacing.mediumAdaptive, bottom = spacing.tinyAdaptive),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.server_restore_initial_data))
            }
        }
    }

    if (restoreInitialDataDialogOpen) {
        ConfirmationDialog(
            stringResource(R.string.server_restore_initial_data_title),
            stringResource(R.string.conversion_permission_common_grant),
            stringResource(R.string.conversion_permission_common_deny),
            onConfirmation = {
                onRestoreInitialData()
                setRestoreInitialDataDialogOpen(false)
            },
            onDismissRequest = { setRestoreInitialDataDialogOpen(false) },
            modifier = Modifier
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareServerRestoreInitialDialog"),
        ) {
            Text(stringResource(R.string.server_restore_initial_data_text))
        }
    }
}

@Composable
private fun ServerDetailPane(
    destination: Int,
    wide: Boolean,
    baseUrl: String,
    apiKey: String,
    apiKeyHeader: String,
    authType: ServerAuthType,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ServerAuthType) -> Unit,
    onSetBaseUrl: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (deleteDialogOpen, setDeleteDialogOpen) = retain { mutableStateOf(false) }

    Box {
        Column {
            ScrollablePane(
                title = {
                    Text(
                        stringResource(if (destination == -1) R.string.server_insert else R.string.server_update),
                        Modifier.padding(horizontal = spacing.windowPadding),
                    )
                },
                onBack = onBack.takeUnless { wide },
                modifier = Modifier.testTag("geoShareServerDetailPane"),
                actions = {
                    if (destination != -1) {
                        IconButton(
                            onClick = { setDeleteDialogOpen(true) },
                            modifier = Modifier.testTag("geoShareServerDetailDelete"),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.delete))
                        }
                    }
                },
                navigationImageVector = Icons.Default.Close,
            ) {
                item {
                    ServerForm(
                        apiKey = apiKey,
                        apiKeyHeader = apiKeyHeader,
                        authType = authType,
                        baseUrl = baseUrl,
                        onSaveForm = onSaveForm,
                        onSetApiKey = onSetApiKey,
                        onSetApiKeyHeader = onSetApiKeyHeader,
                        onSetAuthType = onSetAuthType,
                        onSetBaseUrl = onSetBaseUrl,
                        modifier = Modifier
                            .width(600.dp)
                            .padding(top = spacing.smallAdaptive, bottom = spacing.tinyAdaptive),
                    )
                }
            }
        }
    }

    if (deleteDialogOpen) {
        ConfirmationDialog(
            title = stringResource(R.string.server_delete_title),
            confirmText = stringResource(R.string.conversion_permission_common_grant),
            dismissText = stringResource(R.string.conversion_permission_common_deny),
            onConfirmation = {
                onDelete()
                setDeleteDialogOpen(false)
            },
            onDismissRequest = { setDeleteDialogOpen(false) },
            modifier = Modifier
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareServerDeleteDialog"),
        ) {
            Text(stringResource(R.string.server_delete_text, baseUrl))
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
                ServerScreen(
                    destination = null,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    baseUrl = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
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
                ServerScreen(
                    destination = null,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    baseUrl = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
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
                ServerScreen(
                    destination = null,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    baseUrl = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsertPreview() {
    AppTheme {
        Surface {
            Column {
                ServerScreen(
                    destination = -1,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.ATTESTATION,
                    baseUrl = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkInsertPreview() {
    AppTheme {
        Surface {
            Column {
                ServerScreen(
                    destination = -1,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.ATTESTATION,
                    baseUrl = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletInsertPreview() {
    AppTheme {
        Surface {
            Column {
                ServerScreen(
                    destination = -1,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.ATTESTATION,
                    baseUrl = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true)
@Composable
private fun UpdatePreview() {
    AppTheme {
        Surface {
            Column {
                val item = FakeGoogleMapsServer
                ServerScreen(
                    destination = item.uid,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    baseUrl = item.baseUrl,
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkUpdatePreview() {
    AppTheme {
        Surface {
            Column {
                val item = FakeGoogleMapsServer
                ServerScreen(
                    destination = item.uid,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    baseUrl = item.baseUrl,
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletUpdatePreview() {
    AppTheme {
        Surface {
            Column {
                val item = FakeGoogleMapsServer
                ServerScreen(
                    destination = item.uid,
                    all = defaultFakeServers,
                    selected = null,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    baseUrl = item.baseUrl,
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelect = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                )
            }
        }
    }
}

package page.ooooo.geoshare.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
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
import page.ooooo.geoshare.data.di.FakeGoogleMapsApiPreset
import page.ooooo.geoshare.data.di.defaultFakeApiPresets
import page.ooooo.geoshare.data.local.database.ApiAuthType
import page.ooooo.geoshare.data.local.database.ApiPreset
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.ui.components.ApiPresetForm
import page.ooooo.geoshare.ui.components.BasicListDetailScaffold
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.components.SegmentedList
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ApiPresetScreen(
    onBack: () -> Unit,
    viewModel: ApiPresetViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current
    val all by viewModel.all.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    ApiPresetScreen(
        destination = viewModel.destination,
        all = all,
        message = message,
        apiKey = viewModel.apiKey,
        apiKeyHeader = viewModel.apiKeyHeader,
        authType = viewModel.authType,
        baseUrl = viewModel.baseUrl,
        enabled = viewModel.enabled,
        onBack = onBack,
        onDelete = { viewModel.delete(resources) },
        onDisable = { viewModel.disable(it) },
        onDismissMessage = { viewModel.dismissMessage() },
        onEnable = { viewModel.enable(it) },
        onNavigateTo = {
            coroutineScope.launch {
                viewModel.navigateTo(it)
            }
        },
        onRestoreInitialData = { viewModel.restoreInitialData(resources) },
        onSaveForm = { viewModel.saveForm(resources) },
        onSetApiKey = { viewModel.apiKey = it },
        onSetApiKeyHeader = { viewModel.apiKeyHeader = it },
        onSetAuthType = { viewModel.authType = it },
        onSetBaseUrl = { viewModel.baseUrl = it },
        onSetEnabled = { viewModel.enabled = it },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ApiPresetScreen(
    destination: Int?,
    all: List<ApiPreset>,
    message: Message?,
    apiKey: String,
    apiKeyHeader: String,
    authType: ApiAuthType,
    baseUrl: String,
    enabled: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onDisable: (uid: Int) -> Unit,
    onDismissMessage: () -> Unit,
    onEnable: (uid: Int) -> Unit,
    onNavigateTo: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ApiAuthType) -> Unit,
    onSetBaseUrl: (String) -> Unit,
    onSetEnabled: (Boolean) -> Unit,
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
                ApiPresetListPane(
                    destination = destination,
                    containerColor = containerColor,
                    all = all,
                    onBack = onBack,
                    onDisable = onDisable,
                    onEnable = onEnable,
                    onNavigateToContentKey = onNavigateTo,
                    onRestoreInitialData = onRestoreInitialData,
                )
            },
            detailPane = { wide ->
                // Use destination coming from view model, because if we use navigator.currentDestination?.contentKey,
                // fields get briefly rendered with empty values when switching from detail to list.
                if (destination != null) {
                    ApiPresetDetailPane(
                        destination = destination,
                        wide = wide,
                        enabled = enabled,
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
                        onSetEnabled = onSetEnabled,
                    )
                }
            },
            listContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
private fun ApiPresetListPane(
    destination: Int?,
    containerColor: Color,
    all: List<ApiPreset>,
    onBack: () -> Unit,
    onDisable: (uid: Int) -> Unit,
    onEnable: (uid: Int) -> Unit,
    onNavigateToContentKey: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val (restoreInitialDataDialogOpen, setRestoreInitialDataDialogOpen) = retain { mutableStateOf(false) }

    ScrollablePane(
        title = {
            Text(stringResource(R.string.api_presets_title))
        },
        onBack = onBack,
        modifier = Modifier
            .padding(horizontal = spacing.windowPadding)
            .testTag("geoShareApiPresetListPane"),
        containerColor = containerColor,
    ) {
        item {
            ParagraphText(
                stringResource(R.string.api_presets_description),
                Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.smallAdaptive),
            )
        }
        item {
            Button(
                { onNavigateToContentKey(-1) },
                Modifier.testTag("geoShareApiPresetListInsert"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(stringResource(R.string.links_insert))
            }
        }
        item {
            SegmentedList(
                values = all,
                itemHeadline = { it.baseUrl },
                itemIsSelected = { it.uid == destination },
                itemOnClick = { onNavigateToContentKey(it.uid) },
                itemTrailingContent = {
                    {
                        Switch(
                            checked = it.enabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    onEnable(it.uid)
                                } else {
                                    onDisable(it.uid)
                                }
                            },
                            modifier = Modifier.testTag("geoShareApiPresetListItemToggle_${it.uuid}")
                        )
                    }
                },
                itemTestTag = { "geoShareApiPresetListItem_${it.uuid}" },
                sort = true,
            )
        }
        item {
            TextButton(
                onClick = { setRestoreInitialDataDialogOpen(true) },
                modifier = Modifier
                    .testTag("geoShareApiPresetRestoreInitialButton")
                    .padding(top = spacing.mediumAdaptive, bottom = spacing.tinyAdaptive),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.links_restore_initial_data))
            }
        }
    }

    if (restoreInitialDataDialogOpen) {
        ConfirmationDialog(
            stringResource(R.string.links_restore_initial_data_title),
            stringResource(R.string.conversion_permission_common_grant),
            stringResource(R.string.conversion_permission_common_deny),
            onConfirmation = {
                onRestoreInitialData()
                setRestoreInitialDataDialogOpen(false)
            },
            onDismissRequest = { setRestoreInitialDataDialogOpen(false) },
            modifier = Modifier
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareApiPresetRestoreInitialDialog"),
        ) {
            Text(stringResource(R.string.links_restore_initial_data_text))
        }
    }
}

@Composable
private fun ApiPresetDetailPane(
    destination: Int,
    wide: Boolean,
    baseUrl: String,
    apiKey: String,
    apiKeyHeader: String,
    authType: ApiAuthType,
    enabled: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ApiAuthType) -> Unit,
    onSetBaseUrl: (String) -> Unit,
    onSetEnabled: (Boolean) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (deleteDialogOpen, setDeleteDialogOpen) = retain { mutableStateOf(false) }

    Box {
        Column {
            ScrollablePane(
                title = {
                    Text(
                        stringResource(if (destination == -1) R.string.links_insert else R.string.links_update),
                        Modifier.padding(horizontal = spacing.windowPadding),
                    )
                },
                onBack = onBack.takeUnless { wide },
                modifier = Modifier.testTag("geoShareApiPresetDetailPane"),
                actions = {
                    if (destination != -1) {
                        IconButton(
                            onClick = { setDeleteDialogOpen(true) },
                            modifier = Modifier.testTag("geoShareApiPresetDetailDelete"),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.links_delete))
                        }
                    }
                },
                navigationImageVector = Icons.Default.Close,
            ) {
                item {
                    ApiPresetForm(
                        apiKey = apiKey,
                        apiKeyHeader = apiKeyHeader,
                        authType = authType,
                        baseUrl = baseUrl,
                        enabled = enabled,
                        onSaveForm = onSaveForm,
                        onSetApiKey = onSetApiKey,
                        onSetApiKeyHeader = onSetApiKeyHeader,
                        onSetAuthType = onSetAuthType,
                        onSetBaseUrl = onSetBaseUrl,
                        onSetEnabled = onSetEnabled,
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
            title = stringResource(R.string.links_delete_title),
            confirmText = stringResource(R.string.conversion_permission_common_grant),
            dismissText = stringResource(R.string.conversion_permission_common_deny),
            onConfirmation = {
                onDelete()
                setDeleteDialogOpen(false)
            },
            onDismissRequest = { setDeleteDialogOpen(false) },
            modifier = Modifier
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareApiPresetDeleteDialog"),
        ) {
            Text(stringResource(R.string.links_delete_text, baseUrl))
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
                ApiPresetScreen(
                    destination = null,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ApiAuthType.API_KEY,
                    baseUrl = "",
                    enabled = false,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
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
                ApiPresetScreen(
                    destination = null,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ApiAuthType.API_KEY,
                    baseUrl = "",
                    enabled = false,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
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
                ApiPresetScreen(
                    destination = null,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ApiAuthType.API_KEY,
                    baseUrl = "",
                    enabled = false,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
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
                ApiPresetScreen(
                    destination = -1,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ApiAuthType.ATTESTATION,
                    baseUrl = "",
                    enabled = false,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
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
                ApiPresetScreen(
                    destination = -1,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ApiAuthType.ATTESTATION,
                    baseUrl = "",
                    enabled = false,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
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
                ApiPresetScreen(
                    destination = -1,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ApiAuthType.ATTESTATION,
                    baseUrl = "",
                    enabled = false,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
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
                val apiPreset = FakeGoogleMapsApiPreset
                ApiPresetScreen(
                    destination = -1,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = apiPreset.apiKey,
                    apiKeyHeader = apiPreset.apiKeyHeader,
                    authType = apiPreset.authType,
                    baseUrl = apiPreset.baseUrl,
                    enabled = apiPreset.enabled,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
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
                val apiPreset = FakeGoogleMapsApiPreset
                ApiPresetScreen(
                    destination = -1,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = apiPreset.apiKey,
                    apiKeyHeader = apiPreset.apiKeyHeader,
                    authType = apiPreset.authType,
                    baseUrl = apiPreset.baseUrl,
                    enabled = apiPreset.enabled,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
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
                val apiPreset = FakeGoogleMapsApiPreset
                ApiPresetScreen(
                    destination = -1,
                    all = defaultFakeApiPresets,
                    message = null,
                    apiKey = apiPreset.apiKey,
                    apiKeyHeader = apiPreset.apiKeyHeader,
                    authType = apiPreset.authType,
                    baseUrl = apiPreset.baseUrl,
                    enabled = apiPreset.enabled,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetBaseUrl = {},
                    onSetEnabled = {},
                )
            }
        }
    }
}

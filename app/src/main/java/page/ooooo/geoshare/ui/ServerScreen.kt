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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGoogleMapsAddressServer
import page.ooooo.geoshare.data.di.defaultFakeServers
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.LargeTopAppBarPane
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.SegmentedList
import page.ooooo.geoshare.ui.components.ServerForm
import page.ooooo.geoshare.ui.components.StyledListDetailPaneScaffold
import page.ooooo.geoshare.ui.components.StyledPaneScaffoldDefaults
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ServerScreen(
    initialUid: Int?,
    onBack: () -> Unit,
    viewModel: ServerViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current

    LaunchedEffect(initialUid) {
        if (initialUid != null) {
            viewModel.navigateTo(initialUid)
        }
    }

    ServerScreen(
        destination = viewModel.destination,
        servers = viewModel.all,
        message = viewModel.message,
        apiKey = viewModel.apiKey,
        apiKeyHeader = viewModel.apiKeyHeader,
        authType = viewModel.authType,
        challengeUrl = viewModel.challengeUrl,
        name = viewModel.name,
        loginUrl = viewModel.loginUrl,
        registerUrl = viewModel.registerUrl,
        urlTemplate = viewModel.urlTemplate,
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
        onSetApiKey = { viewModel.setApiKey(it) },
        onSetApiKeyHeader = { viewModel.setApiKeyHeader(it) },
        onSetAuthType = { viewModel.setAuthType(it) },
        onSetChallengeUrl = { viewModel.setChallengeUrl(it) },
        onSetLoginUrl = { viewModel.setLoginUrl(it) },
        onSetName = { viewModel.setName(it) },
        onSetRegisterUrl = { viewModel.setRegisterUrl(it) },
        onSetUrlTemplate = { viewModel.setUrlTemplate(it) },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ServerScreen(
    destination: StateFlow<Int?>,
    servers: StateFlow<List<Server>>,
    message: StateFlow<Message?>,
    apiKey: StateFlow<String>,
    apiKeyHeader: StateFlow<String>,
    authType: StateFlow<ServerAuthType>,
    challengeUrl: StateFlow<String>,
    loginUrl: StateFlow<String>,
    name: StateFlow<String>,
    registerUrl: StateFlow<String>,
    urlTemplate: StateFlow<String>,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onDismissMessage: () -> Unit,
    onNavigateTo: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ServerAuthType) -> Unit,
    onSetChallengeUrl: (String) -> Unit,
    onSetLoginUrl: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetRegisterUrl: (String) -> Unit,
    onSetUrlTemplate: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val destination by destination.collectAsStateWithLifecycle()
    val message by message.collectAsStateWithLifecycle()

    // Drive the scaffold navigator from view model, so that the UI state survives process death.

    val defaultDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
    val navigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = defaultDirective.copy(
            horizontalPartitionSpacerSize = LocalSpacing.current.windowPadding,
        ),
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
        message?.let { message ->
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
        StyledListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            listPane = {
                // Use destination coming from view model, because if we use navigator.currentDestination?.contentKey,
                // fields get briefly rendered with empty values when switching from detail to list.
                ServerListPane(
                    destination = destination,
                    servers = servers,
                    onBack = onBack,
                    onNavigateToContentKey = onNavigateTo,
                    onRestoreInitialData = onRestoreInitialData,
                )
            },
            detailPane = { wide ->
                // Use destination coming from view model, because if we use navigator.currentDestination?.contentKey,
                // fields get briefly rendered with empty values when switching from detail to list.
                destination?.let { destination ->
                    ServerDetailPane(
                        destination = destination,
                        wide = wide,
                        apiKey = apiKey,
                        apiKeyHeader = apiKeyHeader,
                        authType = authType,
                        challengeUrl = challengeUrl,
                        loginUrl = loginUrl,
                        onBack = { onNavigateTo(null) },
                        onDelete = onDelete,
                        name = name,
                        registerUrl = registerUrl,
                        urlTemplate = urlTemplate,
                        onSaveForm = onSaveForm,
                        onSetApiKey = onSetApiKey,
                        onSetApiKeyHeader = onSetApiKeyHeader,
                        onSetAuthType = onSetAuthType,
                        onSetChallengeUrl = onSetChallengeUrl,
                        onSetLoginUrl = onSetLoginUrl,
                        onSetName = onSetName,
                        onSetRegisterUrl = onSetRegisterUrl,
                        onSetUrlTemplate = onSetUrlTemplate,
                    )
                }
            },
            colors = StyledPaneScaffoldDefaults.colors(
                wideMainContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ServerListPane(
    destination: Int?,
    servers: StateFlow<List<Server>>,
    onBack: () -> Unit,
    onNavigateToContentKey: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
) {
    val spacing = LocalSpacing.current

    var restoreInitialDataDialogOpen by retain { mutableStateOf(false) }
    val servers by servers.collectAsStateWithLifecycle()

    LargeTopAppBarPane(
        modifier = Modifier.testTag("geoShareServerListPane"),
        title = { maxLines ->
            Text(stringResource(R.string.server_list_title), overflow = TextOverflow.Ellipsis, maxLines = maxLines)
        },
        onBack = onBack,
    ) {
        item(key = "description", contentType = "paragraph_text") {
            ParagraphText(
                stringResource(R.string.server_list_description, stringResource(R.string.app_name)),
                Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .padding(top = spacing.tiny, bottom = spacing.small),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item(key = "add", contentType = "button") {
            Button(
                { onNavigateToContentKey(-1) },
                Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .testTag("geoShareServerListInsert"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(stringResource(R.string.server_insert))
            }
        }
        item("list", contentType = "segmented_list") {
            SegmentedList(
                values = servers,
                modifier = Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .padding(top = spacing.medium),
                itemHeadline = { server -> server.name },
                itemIsSelected = { server -> server.uid == destination },
                itemOnClick = { server -> onNavigateToContentKey(server.uid) },
                itemTestTag = { server -> "geoShareServerListItem_${server.uid}" },
                sort = true,
            )
        }
        item(key = "restore", contentType = "text_button") {
            TextButton(
                onClick = { restoreInitialDataDialogOpen = true },
                modifier = Modifier
                    .testTag("geoShareServerRestoreInitialButton")
                    .padding(horizontal = spacing.windowPadding)
                    .padding(top = spacing.small, bottom = spacing.tiny),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
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
                restoreInitialDataDialogOpen = false
            },
            onDismissRequest = { restoreInitialDataDialogOpen = false },
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
    apiKey: StateFlow<String>,
    apiKeyHeader: StateFlow<String>,
    authType: StateFlow<ServerAuthType>,
    challengeUrl: StateFlow<String>,
    loginUrl: StateFlow<String>,
    name: StateFlow<String>,
    registerUrl: StateFlow<String>,
    urlTemplate: StateFlow<String>,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ServerAuthType) -> Unit,
    onSetChallengeUrl: (String) -> Unit,
    onSetLoginUrl: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetRegisterUrl: (String) -> Unit,
    onSetUrlTemplate: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (deleteDialogOpen, setDeleteDialogOpen) = retain { mutableStateOf(false) }

    Box {
        Column {
            LargeTopAppBarPane(
                modifier = Modifier.testTag("geoShareServerDetailPane"),
                title = { maxLines ->
                    Text(
                        stringResource(if (destination == -1) R.string.server_insert else R.string.server_update),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = maxLines,
                    )
                },
                onBack = onBack.takeUnless { wide },
                actions = {
                    if (destination != -1) {
                        IconButton(
                            onClick = { setDeleteDialogOpen(true) },
                            modifier = Modifier.testTag("geoShareServerDetailDelete"),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.links_delete))
                        }
                    }
                },
                backIcon = Icons.Default.Close,
            ) {
                item(key = "server_form", contentType = "server_form") {
                    ServerForm(
                        apiKey = apiKey,
                        apiKeyHeader = apiKeyHeader,
                        authType = authType,
                        challengeUrl = challengeUrl,
                        loginUrl = loginUrl,
                        name = name,
                        registerUrl = registerUrl,
                        urlTemplate = urlTemplate,
                        onSaveForm = onSaveForm,
                        onSetApiKey = onSetApiKey,
                        onSetApiKeyHeader = onSetApiKeyHeader,
                        onSetAuthType = onSetAuthType,
                        onSetChallengeUrl = onSetChallengeUrl,
                        onSetLoginUrl = onSetLoginUrl,
                        onSetName = onSetName,
                        onSetRegisterUrl = onSetRegisterUrl,
                        onSetUrlTemplate = onSetUrlTemplate,
                        modifier = Modifier
                            .width(600.dp)
                            .padding(top = spacing.small, bottom = spacing.tiny),
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
            val name by name.collectAsStateWithLifecycle()
            Text(stringResource(R.string.server_delete_text, name))
        }
    }
}

// Previews

@Preview(showBackground = true, device = "spec:width=1080px,height=4200px,dpi=440")
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                ServerScreen(
                    destination = MutableStateFlow(null),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(""),
                    apiKeyHeader = MutableStateFlow(""),
                    authType = MutableStateFlow(ServerAuthType.API_KEY),
                    challengeUrl = MutableStateFlow(""),
                    loginUrl = MutableStateFlow(""),
                    name = MutableStateFlow(""),
                    registerUrl = MutableStateFlow(""),
                    urlTemplate = MutableStateFlow(""),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=4200px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column {
                ServerScreen(
                    destination = MutableStateFlow(null),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(""),
                    apiKeyHeader = MutableStateFlow(""),
                    authType = MutableStateFlow(ServerAuthType.API_KEY),
                    challengeUrl = MutableStateFlow(""),
                    loginUrl = MutableStateFlow(""),
                    name = MutableStateFlow(""),
                    registerUrl = MutableStateFlow(""),
                    urlTemplate = MutableStateFlow(""),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
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
                    destination = MutableStateFlow(null),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(""),
                    apiKeyHeader = MutableStateFlow(""),
                    authType = MutableStateFlow(ServerAuthType.API_KEY),
                    challengeUrl = MutableStateFlow(""),
                    loginUrl = MutableStateFlow(""),
                    name = MutableStateFlow(""),
                    registerUrl = MutableStateFlow(""),
                    urlTemplate = MutableStateFlow(""),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
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
                    destination = MutableStateFlow(-1),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(""),
                    apiKeyHeader = MutableStateFlow(""),
                    authType = MutableStateFlow(ServerAuthType.API_KEY),
                    challengeUrl = MutableStateFlow(""),
                    loginUrl = MutableStateFlow(""),
                    name = MutableStateFlow(""),
                    registerUrl = MutableStateFlow(""),
                    urlTemplate = MutableStateFlow(""),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
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
                    destination = MutableStateFlow(-1),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(""),
                    apiKeyHeader = MutableStateFlow(""),
                    authType = MutableStateFlow(ServerAuthType.ATTESTATION),
                    challengeUrl = MutableStateFlow(""),
                    loginUrl = MutableStateFlow(""),
                    name = MutableStateFlow(""),
                    registerUrl = MutableStateFlow(""),
                    urlTemplate = MutableStateFlow(""),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
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
                    destination = MutableStateFlow(-1),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(""),
                    apiKeyHeader = MutableStateFlow(""),
                    authType = MutableStateFlow(ServerAuthType.ATTESTATION),
                    challengeUrl = MutableStateFlow(""),
                    loginUrl = MutableStateFlow(""),
                    name = MutableStateFlow(""),
                    registerUrl = MutableStateFlow(""),
                    urlTemplate = MutableStateFlow(""),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
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
                val item = FakeGoogleMapsAddressServer
                ServerScreen(
                    destination = MutableStateFlow(item.uid),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(item.apiKey),
                    apiKeyHeader = MutableStateFlow(item.apiKeyHeader),
                    authType = MutableStateFlow(item.authType),
                    challengeUrl = MutableStateFlow(item.challengeUrl),
                    loginUrl = MutableStateFlow(item.loginUrl),
                    name = MutableStateFlow(item.name),
                    registerUrl = MutableStateFlow(item.registerUrl),
                    urlTemplate = MutableStateFlow(item.urlTemplate),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
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
                val item = FakeGoogleMapsAddressServer
                ServerScreen(
                    destination = MutableStateFlow(item.uid),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(item.apiKey),
                    apiKeyHeader = MutableStateFlow(item.apiKeyHeader),
                    authType = MutableStateFlow(item.authType),
                    challengeUrl = MutableStateFlow(item.challengeUrl),
                    loginUrl = MutableStateFlow(item.loginUrl),
                    name = MutableStateFlow(item.name),
                    registerUrl = MutableStateFlow(item.registerUrl),
                    urlTemplate = MutableStateFlow(item.urlTemplate),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
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
                val item = FakeGoogleMapsAddressServer
                ServerScreen(
                    destination = MutableStateFlow(item.uid),
                    servers = MutableStateFlow(defaultFakeServers),
                    message = MutableStateFlow(null),
                    apiKey = MutableStateFlow(item.apiKey),
                    apiKeyHeader = MutableStateFlow(item.apiKeyHeader),
                    authType = MutableStateFlow(item.authType),
                    challengeUrl = MutableStateFlow(item.challengeUrl),
                    loginUrl = MutableStateFlow(item.loginUrl),
                    name = MutableStateFlow(item.name),
                    registerUrl = MutableStateFlow(item.registerUrl),
                    urlTemplate = MutableStateFlow(item.urlTemplate),
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetApiKey = {},
                    onSetApiKeyHeader = {},
                    onSetAuthType = {},
                    onSetChallengeUrl = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
                )
            }
        }
    }
}

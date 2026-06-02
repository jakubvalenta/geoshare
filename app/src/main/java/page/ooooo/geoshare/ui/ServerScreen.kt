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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGoogleMapsAddressServer
import page.ooooo.geoshare.data.di.defaultFakeServers
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.ui.components.BasicListDetailScaffold
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.components.SegmentedList
import page.ooooo.geoshare.ui.components.ServerForm
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
    val message by viewModel.message.collectAsStateWithLifecycle()

    ServerScreen(
        destination = viewModel.destination,
        all = all,
        message = message,
        apiKey = viewModel.apiKey,
        apiKeyHeader = viewModel.apiKeyHeader,
        authType = viewModel.authType,
        challengeUrl = viewModel.challengeUrl,
        description = viewModel.description,
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
        onSetApiKey = { viewModel.apiKey = it },
        onSetApiKeyHeader = { viewModel.apiKeyHeader = it },
        onSetAuthType = { viewModel.authType = it },
        onSetChallengeUrl = { viewModel.challengeUrl = it },
        onSetDescription = { viewModel.description = it },
        onSetLoginUrl = { viewModel.loginUrl = it },
        onSetName = { viewModel.name = it },
        onSetRegisterUrl = { viewModel.registerUrl = it },
        onSetUrlTemplate = { viewModel.urlTemplate = it },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ServerScreen(
    destination: Int?,
    all: List<Server>,
    message: Message?,
    apiKey: String,
    apiKeyHeader: String,
    authType: ServerAuthType,
    challengeUrl: String,
    description: String,
    loginUrl: String,
    name: String,
    registerUrl: String,
    urlTemplate: String,
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
    onSetDescription: (String) -> Unit,
    onSetLoginUrl: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetRegisterUrl: (String) -> Unit,
    onSetUrlTemplate: (String) -> Unit,
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
                    onBack = onBack,
                    onNavigateToContentKey = onNavigateTo,
                    onRestoreInitialData = onRestoreInitialData,
                )
            },
            detailPane = { wide ->
                // Use destination coming from view model, because if we use navigator.currentDestination?.contentKey,
                // fields get briefly rendered with empty values when switching from detail to list.
                if (destination != null) {
                    ServerDetailPane(
                        destination = destination,
                        wide = wide,
                        apiKey = apiKey,
                        apiKeyHeader = apiKeyHeader,
                        authType = authType,
                        challengeUrl = challengeUrl,
                        description = description,
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
                        onSetDescription = onSetDescription,
                        onSetLoginUrl = onSetLoginUrl,
                        onSetName = onSetName,
                        onSetRegisterUrl = onSetRegisterUrl,
                        onSetUrlTemplate = onSetUrlTemplate,
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
    onBack: () -> Unit,
    onNavigateToContentKey: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val (restoreInitialDataDialogOpen, setRestoreInitialDataDialogOpen) = retain { mutableStateOf(false) }

    ScrollablePane(
        title = {
            Text(stringResource(R.string.server_list_title))
        },
        onBack = onBack,
        modifier = Modifier
            .padding(horizontal = spacing.windowPadding)
            .testTag("geoShareServerListPane"),
        containerColor = containerColor,
    ) {
        item {
            ParagraphText(
                stringResource(R.string.server_list_description),
                Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.smallAdaptive),
            )
        }
        item {
            Button(
                { onNavigateToContentKey(-1) },
                Modifier
                    .padding(top = spacing.smallAdaptive)
                    .testTag("geoShareServerListInsert"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(stringResource(R.string.server_insert))
            }
        }
        item {
            SegmentedList(
                values = all,
                modifier = Modifier.padding(top = spacing.medium),
                itemHeadline = { item -> item.name },
                itemIsSelected = { item -> item.uid == destination },
                itemOnClick = { item -> onNavigateToContentKey(item.uid) },
                itemSupportingContent = { item ->
                    item.description.takeIf { it.isNotEmpty() }?.let { description ->
                        {
                            Text(
                                description,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                itemTrailingContent = { item ->
                    if (!item.isValid()) {
                        {
                            Text(
                                stringResource(R.string.server_invalid),
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else {
                        null
                    }
                },
                itemTestTag = { "geoShareServerListItem_${it.uuid}" },
            )
        }
        item {
            TextButton(
                onClick = { setRestoreInitialDataDialogOpen(true) },
                modifier = Modifier
                    .testTag("geoShareServerRestoreInitialButton")
                    .padding(top = spacing.mediumAdaptive, bottom = spacing.tinyAdaptive),
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
    apiKey: String,
    apiKeyHeader: String,
    authType: ServerAuthType,
    challengeUrl: String,
    loginUrl: String,
    name: String,
    description: String,
    registerUrl: String,
    urlTemplate: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ServerAuthType) -> Unit,
    onSetChallengeUrl: (String) -> Unit,
    onSetDescription: (String) -> Unit,
    onSetLoginUrl: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetRegisterUrl: (String) -> Unit,
    onSetUrlTemplate: (String) -> Unit,
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
                        challengeUrl = challengeUrl,
                        description = description,
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
                        onSetDescription = onSetDescription,
                        onSetRegisterUrl = onSetRegisterUrl,
                        onSetUrlTemplate = onSetUrlTemplate,
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
            Text(stringResource(R.string.server_delete_text, name))
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
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    challengeUrl = "",
                    description = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    urlTemplate = "",
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
                    onSetDescription = {},
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
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column {
                ServerScreen(
                    destination = null,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    challengeUrl = "",
                    description = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    urlTemplate = "",
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
                    onSetDescription = {},
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
                    destination = null,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    challengeUrl = "",
                    description = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    urlTemplate = "",
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
                    onSetDescription = {},
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
                    destination = -1,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    challengeUrl = "",
                    description = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    urlTemplate = "",
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
                    onSetDescription = {},
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
                    destination = -1,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.ATTESTATION,
                    challengeUrl = "",
                    description = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    urlTemplate = "",
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
                    onSetDescription = {},
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
                    destination = -1,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.ATTESTATION,
                    challengeUrl = "",
                    description = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    urlTemplate = "",
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
                    onSetDescription = {},
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
                    destination = item.uid,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    challengeUrl = item.challengeUrl,
                    description = item.description,
                    loginUrl = item.loginUrl,
                    name = item.name,
                    registerUrl = item.registerUrl,
                    urlTemplate = item.urlTemplate,
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
                    onSetDescription = {},
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
                    destination = item.uid,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    challengeUrl = item.challengeUrl,
                    description = item.description,
                    loginUrl = item.loginUrl,
                    name = item.name,
                    registerUrl = item.registerUrl,
                    urlTemplate = item.urlTemplate,
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
                    onSetDescription = {},
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
                    destination = item.uid,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    challengeUrl = item.challengeUrl,
                    description = item.description,
                    loginUrl = item.loginUrl,
                    name = item.name,
                    registerUrl = item.registerUrl,
                    urlTemplate = item.urlTemplate,
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
                    onSetDescription = {},
                    onSetLoginUrl = {},
                    onSetName = {},
                    onSetRegisterUrl = {},
                    onSetUrlTemplate = {},
                )
            }
        }
    }
}

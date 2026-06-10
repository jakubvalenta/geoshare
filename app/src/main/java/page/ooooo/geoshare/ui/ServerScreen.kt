package page.ooooo.geoshare.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGeoShareGoogleMapsAddressServer
import page.ooooo.geoshare.data.di.FakeGeoShareGoogleMapsPlaceServer
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
import page.ooooo.geoshare.ui.components.SegmentedListLabel
import page.ooooo.geoshare.ui.components.ServerForm
import page.ooooo.geoshare.ui.components.StyledListDetailPaneScaffold
import page.ooooo.geoshare.ui.components.StyledPaneScaffoldDefaults
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
    val selectedServerGoogleMapsAddress by viewModel.selectedServerGoogleMapsAddress.collectAsStateWithLifecycle()
    val selectedServerGoogleMapsPlace by viewModel.selectedServerGoogleMapsPlace.collectAsStateWithLifecycle()
    val selectedServerSearch by viewModel.selectedServerSearch.collectAsStateWithLifecycle()

    ServerScreen(
        destination = viewModel.destination,
        all = all,
        message = message,
        apiKey = viewModel.apiKey,
        apiKeyHeader = viewModel.apiKeyHeader,
        authType = viewModel.authType,
        challengeUrl = viewModel.challengeUrl,
        name = viewModel.name,
        loginUrl = viewModel.loginUrl,
        registerUrl = viewModel.registerUrl,
        selectedServerGoogleMapsAddress = selectedServerGoogleMapsAddress,
        selectedServerGoogleMapsPlace = selectedServerGoogleMapsPlace,
        selectedServerSearch = selectedServerSearch,
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
        onSelectServerGoogleMapsAddress = { viewModel.selectServerGoogleMapsAddress(it?.uid) },
        onSelectServerGoogleMapsPlace = { viewModel.selectServerGoogleMapsPlace(it?.uid) },
        onSelectServerSearch = { viewModel.selectServerSearch(it?.uid) },
        onSetApiKey = { viewModel.apiKey = it },
        onSetApiKeyHeader = { viewModel.apiKeyHeader = it },
        onSetAuthType = { viewModel.authType = it },
        onSetChallengeUrl = { viewModel.challengeUrl = it },
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
    loginUrl: String,
    name: String,
    registerUrl: String,
    selectedServerGoogleMapsAddress: Server?,
    selectedServerGoogleMapsPlace: Server?,
    selectedServerSearch: Server?,
    urlTemplate: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onDismissMessage: () -> Unit,
    onNavigateTo: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
    onSaveForm: () -> Unit,
    onSelectServerGoogleMapsAddress: (Server?) -> Unit,
    onSelectServerGoogleMapsPlace: (Server?) -> Unit,
    onSelectServerSearch: (Server?) -> Unit,
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
        StyledListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            listPane = {
                // Use destination coming from view model, because if we use navigator.currentDestination?.contentKey,
                // fields get briefly rendered with empty values when switching from detail to list.
                ServerListPane(
                    destination = destination,
                    all = all,
                    selectedServerGoogleMapsAddress = selectedServerGoogleMapsAddress,
                    selectedServerGoogleMapsPlace = selectedServerGoogleMapsPlace,
                    selectedServerSearch = selectedServerSearch,
                    onBack = onBack,
                    onNavigateToContentKey = onNavigateTo,
                    onRestoreInitialData = onRestoreInitialData,
                    onSelectServerGoogleMapsAddress = onSelectServerGoogleMapsAddress,
                    onSelectServerGoogleMapsPlace = onSelectServerGoogleMapsPlace,
                    onSelectServerSearch = onSelectServerSearch,
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
    all: List<Server>,
    selectedServerGoogleMapsAddress: Server?,
    selectedServerGoogleMapsPlace: Server?,
    selectedServerSearch: Server?,
    onBack: () -> Unit,
    onNavigateToContentKey: (Int?) -> Unit,
    onRestoreInitialData: () -> Unit,
    onSelectServerGoogleMapsAddress: (Server?) -> Unit,
    onSelectServerGoogleMapsPlace: (Server?) -> Unit,
    onSelectServerSearch: (Server?) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (restoreInitialDataDialogOpen, setRestoreInitialDataDialogOpen) = retain { mutableStateOf(false) }

    LargeTopAppBarPane(
        modifier = Modifier.testTag("geoShareServerListPane"),
        title = {
            Text(stringResource(R.string.server_list_title))
        },
        onBack = onBack,
    ) {
        item {
            ParagraphText(
                stringResource(R.string.server_list_description, stringResource(R.string.app_name)),
                Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .padding(top = spacing.tinyAdaptive, bottom = spacing.smallAdaptive),
            )
        }
        item {
            Button(
                { onNavigateToContentKey(-1) },
                Modifier
                    .padding(horizontal = spacing.windowPadding)
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
        serverListSection(
            destination = destination,
            all = all,
            itemTestTag = { "geoShareServerListItem_GoogleMapsAddress_${it?.name}" },
            title = { stringResource(R.string.server_list_google_maps_address_title) },
            noneDescription = { stringResource(R.string.server_list_google_maps_none_description) },
            selectedServer = selectedServerGoogleMapsAddress,
            onNavigateToContentKey = onNavigateToContentKey,
            onSelectServer = onSelectServerGoogleMapsAddress,
        )
        serverListSection(
            destination = destination,
            all = all,
            itemTestTag = { "geoShareServerListItem_GoogleMapsPlace_${it?.name}" },
            title = { stringResource(R.string.server_list_google_maps_place_title) },
            noneDescription = { stringResource(R.string.server_list_google_maps_none_description) },
            selectedServer = selectedServerGoogleMapsPlace,
            onNavigateToContentKey = onNavigateToContentKey,
            onSelectServer = onSelectServerGoogleMapsPlace,
        )
        if (BuildConfig.DEBUG) {
            serverListSection(
                destination = destination,
                all = all,
                itemTestTag = { "geoShareServerSearchListItem_${it?.name}" },
                title = { stringResource(R.string.server_list_search_title) },
                noneDescription = { stringResource(R.string.server_list_search_none_description) },
                selectedServer = selectedServerSearch,
                onNavigateToContentKey = onNavigateToContentKey,
                onSelectServer = onSelectServerSearch,
            )
        }
        item {
            TextButton(
                onClick = { setRestoreInitialDataDialogOpen(true) },
                modifier = Modifier
                    .testTag("geoShareServerRestoreInitialButton")
                    .padding(horizontal = spacing.windowPadding)
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
    registerUrl: String,
    urlTemplate: String,
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
                title = {
                    Text(
                        stringResource(if (destination == -1) R.string.server_insert else R.string.server_update)
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
                navigationImageVector = Icons.Default.Close,
            ) {
                item {
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

fun LazyListScope.serverListSection(
    destination: Int?,
    all: List<Server>,
    itemTestTag: (Server?) -> String,
    title: @Composable () -> String,
    noneDescription: @Composable () -> String,
    selectedServer: Server?,
    onNavigateToContentKey: (Int?) -> Unit,
    onSelectServer: (Server?) -> Unit,
) {
    item {
        SegmentedListLabel(
            title(),
            Modifier.padding(horizontal = LocalSpacing.current.windowPadding),
        )
    }
    item {
        SegmentedList(
            values = listOf(null) + all,
            modifier = Modifier.padding(horizontal = LocalSpacing.current.windowPadding),
            itemHeadline = { item -> item?.name ?: stringResource(R.string.server_list_none) },
            itemIsSelected = { item -> item?.uid == destination },
            itemOnClick = onSelectServer,
            itemEnabled = { item -> item?.isValid() != false },
            itemLeadingContent = { item ->
                {
                    RadioButton(
                        selected = item == selectedServer,
                        // Null recommended for accessibility with screen readers
                        onClick = null,
                        enabled = item?.isValid() != false,
                    )
                }
            },
            itemSupportingContent = { item ->
                if (item == null) {
                    {
                        Text(
                            noneDescription(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else if (!item.isValid()) {
                    {
                        Text(
                            buildAnnotatedString {
                                withLink(
                                    LinkAnnotation.Clickable(
                                        "link",
                                        styles = TextLinkStyles(
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.tertiary,
                                                textDecoration = TextDecoration.Underline
                                            )
                                        ),
                                    ) {
                                        onNavigateToContentKey(item.uid)
                                    }
                                ) {
                                    append(stringResource(R.string.server_invalid))
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else {
                    null
                }
            },
            itemTrailingContent = { item ->
                if (item != null) {
                    {
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
                    }
                } else {
                    null
                }
            },
            itemTestTag = itemTestTag,
        )
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
                    destination = null,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    challengeUrl = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    destination = null,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    challengeUrl = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    destination = null,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    challengeUrl = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    destination = -1,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.API_KEY,
                    challengeUrl = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    destination = -1,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.ATTESTATION,
                    challengeUrl = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    destination = -1,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = "",
                    apiKeyHeader = "",
                    authType = ServerAuthType.ATTESTATION,
                    challengeUrl = "",
                    loginUrl = "",
                    name = "",
                    registerUrl = "",
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = "",
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    destination = item.uid,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    challengeUrl = item.challengeUrl,
                    loginUrl = item.loginUrl,
                    name = item.name,
                    registerUrl = item.registerUrl,
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = item.urlTemplate,
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    destination = item.uid,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    challengeUrl = item.challengeUrl,
                    loginUrl = item.loginUrl,
                    name = item.name,
                    registerUrl = item.registerUrl,
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = item.urlTemplate,
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    destination = item.uid,
                    all = defaultFakeServers,
                    message = null,
                    apiKey = item.apiKey,
                    apiKeyHeader = item.apiKeyHeader,
                    authType = item.authType,
                    challengeUrl = item.challengeUrl,
                    loginUrl = item.loginUrl,
                    name = item.name,
                    registerUrl = item.registerUrl,
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    urlTemplate = item.urlTemplate,
                    onBack = {},
                    onDelete = {},
                    onDismissMessage = {},
                    onNavigateTo = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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

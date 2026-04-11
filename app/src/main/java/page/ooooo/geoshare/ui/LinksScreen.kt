package page.ooooo.geoshare.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
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
import page.ooooo.geoshare.data.di.FakeGoogleMapsStreetViewLink
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.LinkType
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Srs
import page.ooooo.geoshare.ui.components.BasicListDetailScaffold
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.FeatureWall
import page.ooooo.geoshare.ui.components.FeatureBadgeSmall
import page.ooooo.geoshare.ui.components.FeatureBadged
import page.ooooo.geoshare.ui.components.LinkForm
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.components.SegmentedList
import page.ooooo.geoshare.ui.components.SegmentedListLabel
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun LinksScreen(
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    billingViewModel: BillingViewModel = hiltViewModel(),
    outputViewModel: OutputViewModel = hiltViewModel(),
    viewModel: LinkViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current
    val billingAppNameResId = billingViewModel.billingAppNameResId
    val billingFeatures = billingViewModel.billingFeatures
    val billingStatus by billingViewModel.billingStatus.collectAsStateWithLifecycle()
    val links by viewModel.all.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    LinksScreen(
        destination = viewModel.destination,
        billingAppNameResId = billingAppNameResId,
        billingFeatures = billingFeatures,
        billingStatus = billingStatus,
        links = links,
        message = message,
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
        onNavigateToBillingScreen = onNavigateToBillingScreen,
        onRestoreInitialData = { viewModel.restoreInitialData(resources) },
        onSaveForm = { viewModel.saveForm(resources) },
        appEnabled = viewModel.appEnabled,
        chipEnabled = viewModel.chipEnabled,
        coordsUriTemplate = viewModel.coordsUriTemplate,
        group = viewModel.group,
        name = viewModel.name,
        nameUriTemplate = viewModel.nameUriTemplate,
        sheetEnabled = viewModel.sheetEnabled,
        srs = viewModel.srs,
        type = viewModel.type,
        uriFormatter = outputViewModel.uriFormatter,
        onSetAppEnabled = { viewModel.appEnabled = it },
        onSetChipEnabled = { viewModel.chipEnabled = it },
        onSetCoordsUriTemplate = { viewModel.coordsUriTemplate = it },
        onSetGroup = { viewModel.group = it },
        onSetName = { viewModel.name = it },
        onSetNameUriTemplate = { viewModel.nameUriTemplate = it },
        onSetSheetEnabled = { viewModel.sheetEnabled = it },
        onSetSrs = { viewModel.srs = it },
        onSetType = { viewModel.type = it },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun LinksScreen(
    destination: Int?,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    links: List<Link>,
    message: Message?,
    appEnabled: Boolean,
    chipEnabled: Boolean,
    coordsUriTemplate: String,
    group: String,
    name: String,
    nameUriTemplate: String,
    sheetEnabled: Boolean,
    srs: Srs,
    type: LinkType,
    uriFormatter: UriFormatter,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onDisable: (uid: Int) -> Unit,
    onDismissMessage: () -> Unit,
    onEnable: (uid: Int) -> Unit,
    onSaveForm: () -> Unit,
    onNavigateTo: (Int?) -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onRestoreInitialData: () -> Unit,
    onSetAppEnabled: (Boolean) -> Unit,
    onSetChipEnabled: (Boolean) -> Unit,
    onSetCoordsUriTemplate: (String) -> Unit,
    onSetGroup: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetNameUriTemplate: (String) -> Unit,
    onSetSheetEnabled: (Boolean) -> Unit,
    onSetSrs: (Srs) -> Unit,
    onSetType: (LinkType) -> Unit,
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
                LinksListPane(
                    destination = destination,
                    billingFeatures = billingFeatures,
                    billingStatus = billingStatus,
                    containerColor = containerColor,
                    links = links,
                    onBack = onBack,
                    onDisable = onDisable,
                    onEnable = onEnable,
                    onNavigateToContentKey = onNavigateTo,
                    onRestoreInitialLinks = onRestoreInitialData,
                )
            },
            detailPane = { wide ->
                // Use destination coming from view model, because if we use navigator.currentDestination?.contentKey,
                // fields get briefly rendered with empty values when switching from detail to list.
                if (destination != null) {
                    LinksDetailPane(
                        destination = destination,
                        wide = wide,
                        appEnabled = appEnabled,
                        billingAppNameResId = billingAppNameResId,
                        billingFeatures = billingFeatures,
                        billingStatus = billingStatus,
                        chipEnabled = chipEnabled,
                        coordsUriTemplate = coordsUriTemplate,
                        group = group,
                        name = name,
                        nameUriTemplate = nameUriTemplate,
                        sheetEnabled = sheetEnabled,
                        srs = srs,
                        type = type,
                        uriFormatter = uriFormatter,
                        onBack = { onNavigateTo(null) },
                        onDelete = onDelete,
                        onNavigateToBillingScreen = onNavigateToBillingScreen,
                        onSaveForm = onSaveForm,
                        onSetAppEnabled = onSetAppEnabled,
                        onSetChipEnabled = onSetChipEnabled,
                        onSetCoordsUriTemplate = onSetCoordsUriTemplate,
                        onSetGroup = onSetGroup,
                        onSetName = onSetName,
                        onSetNameUriTemplate = onSetNameUriTemplate,
                        onSetSheetEnabled = onSetSheetEnabled,
                        onSetSrs = onSetSrs,
                        onSetType = onSetType,
                    )
                }
            },
            listContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
private fun LinksListPane(
    destination: Int?,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    containerColor: Color,
    links: List<Link>,
    onBack: () -> Unit,
    onDisable: (uid: Int) -> Unit,
    onEnable: (uid: Int) -> Unit,
    onNavigateToContentKey: (Int?) -> Unit,
    onRestoreInitialLinks: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val (restoreInitialLinksDialogOpen, setRestoreInitialLinksDialogOpen) = retain { mutableStateOf(false) }

    ScrollablePane(
        title = {
            Text(stringResource(R.string.links_title))
        },
        onBack = onBack,
        modifier = Modifier
            .padding(horizontal = spacing.windowPadding)
            .testTag("geoShareLinksListPane"),
        containerColor = containerColor,
    ) {
        item {
            ParagraphText(
                stringResource(R.string.links_description),
                Modifier.padding(top = spacing.tinyAdaptive, bottom = spacing.smallAdaptive),
            )
        }
        item {
            FeatureBadged(
                enabled = billingStatus is BillingStatus.NotPurchased && CustomLinkFeature in billingFeatures,
                badge = { modifier ->
                    FeatureBadgeSmall(
                        { onNavigateToContentKey(-1) },
                        modifier.testTag("geoShareCustomLinkFeatureBadge"),
                    )
                },
            ) { modifier ->
                Button(
                    { onNavigateToContentKey(-1) },
                    modifier.testTag("geoShareLinksListInsert"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                ) {
                    Text(stringResource(R.string.links_insert))
                }
            }
        }
        links
            .groupBy { it.groupOrName }
            .toSortedMap()
            .forEach { (group, links) ->
                item {
                    if (group.isNotEmpty()) {
                        SegmentedListLabel(group)
                    } else {
                        Spacer(Modifier.height(spacing.mediumAdaptive))
                    }
                }
                item {
                    SegmentedList(
                        values = links,
                        itemHeadline = { it.name },
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
                                    modifier = Modifier.testTag("geoShareLinksListItemToggle_${it.uuid}")
                                )
                            }
                        },
                        itemTestTag = { "geoShareLinksListItem_${it.uuid}" },
                        sort = true,
                    )
                }
            }
        item {
            TextButton(
                onClick = { setRestoreInitialLinksDialogOpen(true) },
                modifier = Modifier
                    .testTag("geoShareLinksRestoreInitialButton")
                    .padding(top = spacing.mediumAdaptive, bottom = spacing.tinyAdaptive),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.links_restore_initial_data))
            }
        }
    }

    if (restoreInitialLinksDialogOpen) {
        ConfirmationDialog(
            stringResource(R.string.links_restore_initial_data_title),
            stringResource(R.string.conversion_permission_common_grant),
            stringResource(R.string.conversion_permission_common_deny),
            onConfirmation = {
                onRestoreInitialLinks()
                setRestoreInitialLinksDialogOpen(false)
            },
            onDismissRequest = { setRestoreInitialLinksDialogOpen(false) },
            modifier = Modifier
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareLinksRestoreInitialDialog"),
        ) {
            Text(stringResource(R.string.links_restore_initial_data_text))
        }
    }
}

@Composable
private fun LinksDetailPane(
    destination: Int,
    wide: Boolean,
    appEnabled: Boolean,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    chipEnabled: Boolean,
    coordsUriTemplate: String,
    group: String,
    name: String,
    nameUriTemplate: String,
    sheetEnabled: Boolean,
    srs: Srs,
    type: LinkType,
    uriFormatter: UriFormatter,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onSaveForm: () -> Unit,
    onSetAppEnabled: (Boolean) -> Unit,
    onSetChipEnabled: (Boolean) -> Unit,
    onSetCoordsUriTemplate: (String) -> Unit,
    onSetGroup: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetNameUriTemplate: (String) -> Unit,
    onSetSheetEnabled: (Boolean) -> Unit,
    onSetSrs: (Srs) -> Unit,
    onSetType: (LinkType) -> Unit,
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
                modifier = Modifier.testTag("geoShareLinkDetailPane"),
                actions = {
                    if (destination != -1) {
                        IconButton(
                            onClick = { setDeleteDialogOpen(true) },
                            modifier = Modifier.testTag("geoShareLinksDetailDelete"),
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
                    LinkForm(
                        appEnabled = appEnabled,
                        chipEnabled = chipEnabled,
                        coordsUriTemplate = coordsUriTemplate,
                        group = group,
                        name = name,
                        nameUriTemplate = nameUriTemplate,
                        sheetEnabled = sheetEnabled,
                        srs = srs,
                        type = type,
                        uriFormatter = uriFormatter,
                        onSaveForm = onSaveForm,
                        onSetAppEnabled = onSetAppEnabled,
                        onSetChipEnabled = onSetChipEnabled,
                        onSetCoordsUriTemplate = onSetCoordsUriTemplate,
                        onSetGroup = onSetGroup,
                        onSetName = onSetName,
                        onSetNameUriTemplate = onSetNameUriTemplate,
                        onSetSheetEnabled = onSetSheetEnabled,
                        onSetSrs = onSetSrs,
                        onSetType = onSetType,
                        modifier = Modifier
                            .width(600.dp)
                            .padding(top = spacing.smallAdaptive, bottom = spacing.tinyAdaptive),
                        enabled = billingStatus is BillingStatus.Purchased && CustomLinkFeature in billingFeatures,
                    )
                }
            }
        }
        if (billingStatus is BillingStatus.NotPurchased && CustomLinkFeature in billingFeatures) {
            FeatureWall(
                billingAppNameResId = billingAppNameResId,
                modifier = Modifier.testTag("geoShareCustomLinkFeatureWall"),
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            )
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
                .testTag("geoShareLinkDeleteDialog"),
        ) {
            Text(stringResource(R.string.links_delete_text, name))
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = null,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = null,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = null,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = -1,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = -1,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = -1,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsertNotPurchasedPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = -1,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.NotPurchased(pending = false),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkInsertNotPurchasedPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = -1,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.NotPurchased(pending = false),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletInsertNotPurchasedPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = -1,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = false,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.NotPurchased(pending = false),
                    chipEnabled = false,
                    coordsUriTemplate = "",
                    group = "",
                    name = "",
                    nameUriTemplate = "",
                    sheetEnabled = false,
                    srs = Srs.WGS84,
                    type = LinkType.DISPLAY,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val link = FakeGoogleMapsStreetViewLink
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = link.uid,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = link.appEnabled,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = link.chipEnabled,
                    coordsUriTemplate = link.coordsUriTemplate,
                    group = link.group,
                    name = link.name,
                    nameUriTemplate = link.nameUriTemplate,
                    sheetEnabled = link.sheetEnabled,
                    srs = link.srs,
                    type = link.type,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val link = FakeGoogleMapsStreetViewLink
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = link.uid,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = link.appEnabled,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = link.chipEnabled,
                    coordsUriTemplate = link.coordsUriTemplate,
                    group = link.group,
                    name = link.name,
                    nameUriTemplate = link.nameUriTemplate,
                    sheetEnabled = link.sheetEnabled,
                    srs = link.srs,
                    type = link.type,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
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
                val context = LocalContext.current
                val chinaGeometry = ChinaGeometry(context)
                val coordinateConverter = CoordinateConverter(chinaGeometry)
                val link = FakeGoogleMapsStreetViewLink
                val uriFormatter = UriFormatter(coordinateConverter)
                LinksScreen(
                    destination = link.uid,
                    links = defaultFakeLinks,
                    message = null,
                    appEnabled = link.appEnabled,
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                    ),
                    chipEnabled = link.chipEnabled,
                    coordsUriTemplate = link.coordsUriTemplate,
                    group = link.group,
                    name = link.name,
                    nameUriTemplate = link.nameUriTemplate,
                    sheetEnabled = link.sheetEnabled,
                    srs = link.srs,
                    type = link.type,
                    uriFormatter = uriFormatter,
                    onBack = {},
                    onDelete = {},
                    onDisable = {},
                    onDismissMessage = {},
                    onEnable = {},
                    onNavigateTo = {},
                    onNavigateToBillingScreen = {},
                    onRestoreInitialData = {},
                    onSaveForm = {},
                    onSetAppEnabled = {},
                    onSetChipEnabled = {},
                    onSetCoordsUriTemplate = {},
                    onSetGroup = {},
                    onSetName = {},
                    onSetNameUriTemplate = {},
                    onSetSheetEnabled = {},
                    onSetSrs = {},
                    onSetType = {},
                )
            }
        }
    }
}

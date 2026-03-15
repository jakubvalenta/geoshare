package page.ooooo.geoshare.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.findByUUID
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.BillingCachedProductIdPreference
import page.ooooo.geoshare.data.local.preferences.ChangelogShownForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.data.local.preferences.CoordinateFormatPreference
import page.ooooo.geoshare.data.local.preferences.IntroShowForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.OptionsPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.TextUserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.DataTypes
import page.ooooo.geoshare.lib.android.OSMAND_PLUS_PACKAGE_NAME
import page.ooooo.geoshare.lib.billing.FeatureStatus
import page.ooooo.geoshare.lib.formats.CoordsFormat
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.components.FeatureBadgeLarge
import page.ooooo.geoshare.ui.components.FeatureBadgeSmall
import page.ooooo.geoshare.ui.components.IconFromDescriptor
import page.ooooo.geoshare.ui.components.NavigableBasicListDetailScaffold
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.ui.components.RadioButtonGroup
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.components.SegmentedList
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Keep
enum class UserPreferencesGroupId {
    AUTOMATION,
    AUTOMATION_DELAY,
    CONNECTION_PERMISSION,
    COORDINATE_FORMAT,
    DEVELOPER_OPTIONS,
    LINKS,
}

private data class UserPreferencesGroup(
    val id: UserPreferencesGroupId,
    val headline: @Composable () -> String,
    val enabled: Boolean = true,
    val trailingContent: (@Composable () -> Unit)? = null,
    val supportingContent: (@Composable () -> Unit)? = null,
    val onClick: () -> Unit,
)

@Composable
fun UserPreferencesScreen(
    initialGroupId: UserPreferencesGroupId?,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToLinksScreen: () -> Unit,
    billingViewModel: BillingViewModel,
    viewModel: UserPreferencesViewModel = hiltViewModel(),
) {
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val appDetails by viewModel.appDetails.collectAsStateWithLifecycle()
    val automationFeatureStatus by billingViewModel.automationFeatureStatus.collectAsStateWithLifecycle()
    val billingAppNameResId = billingViewModel.billingAppNameResId
    val links by viewModel.links.collectAsStateWithLifecycle()
    val userPreferencesValues by viewModel.values.collectAsStateWithLifecycle()

    UserPreferencesScreen(
        initialGroupId = initialGroupId,
        apps = apps,
        appDetails = appDetails,
        automationFeatureStatus = automationFeatureStatus,
        billingAppNameResId = billingAppNameResId,
        links = links,
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
        onNavigateToLinksScreen = onNavigateToLinksScreen,
        onValueChange = { transform: (preferences: MutablePreferences) -> Unit ->
            viewModel.editUserPreferences(transform)
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferencesScreen(
    initialGroupId: UserPreferencesGroupId?,
    apps: DataTypes,
    appDetails: AppDetails,
    automationFeatureStatus: FeatureStatus,
    billingAppNameResId: Int,
    links: List<Link>,
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToLinksScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
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
    val currentGroupId = remember(navigator.currentDestination) {
        navigator.currentDestination?.contentKey
    }

    NavigableBasicListDetailScaffold(
        navigator = navigator,
        listPane = { _, _ ->
            UserPreferencesListPane(
                currentGroupId = currentGroupId,
                appDetails = appDetails,
                automationFeatureStatus = automationFeatureStatus,
                links = links,
                values = userPreferencesValues,
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
                onNavigateToLinksScreen = onNavigateToLinksScreen,
            )
        },
        detailPane = { wide ->
            if (currentGroupId != null) {
                UserPreferencesDetailPane(
                    currentGroupId = currentGroupId,
                    apps = apps,
                    appDetails = appDetails,
                    automationFeatureStatus = automationFeatureStatus,
                    billingAppNameResId = billingAppNameResId,
                    links = links,
                    values = userPreferencesValues,
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
                    onNavigateToBillingScreen = onNavigateToBillingScreen,
                    onValueChange = onValueChange,
                )
            }
        },
        listContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UserPreferencesListPane(
    currentGroupId: UserPreferencesGroupId?,
    values: UserPreferencesValues,
    appDetails: AppDetails,
    automationFeatureStatus: FeatureStatus,
    links: List<Link>,
    onBack: () -> Unit,
    onNavigateToGroup: (id: UserPreferencesGroupId) -> Unit,
    onNavigateToLinksScreen: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var automationDelayEnabled by retain { mutableStateOf(true) }

    LaunchedEffect(values, automationFeatureStatus, links) {
        automationDelayEnabled = automationFeatureStatus == FeatureStatus.AVAILABLE &&
            values.automation.toOutput { links.findByUUID(it) } is Output.HasAutomationDelay
    }

    ScrollablePane(
        title = {
            Text(stringResource(R.string.user_preferences_title))
        },
        onBack = onBack,
        modifier = Modifier.padding(horizontal = spacing.windowPadding),
    ) {
        item {
            val values = buildList {
                add(
                    UserPreferencesGroup(
                        id = UserPreferencesGroupId.CONNECTION_PERMISSION,
                        headline = { stringResource(R.string.user_preferences_connection_title) },
                        supportingContent = {
                            ConnectionPermissionPreferenceValue(
                                value = ConnectionPermissionPreference.getValue(values),
                            )
                        },
                        onClick = { onNavigateToGroup(UserPreferencesGroupId.CONNECTION_PERMISSION) },
                    )
                )
                add(
                    UserPreferencesGroup(
                        id = UserPreferencesGroupId.LINKS,
                        headline = { stringResource(R.string.links_title) },
                        onClick = { onNavigateToLinksScreen() },
                    )
                )
                add(
                    UserPreferencesGroup(
                        id = UserPreferencesGroupId.AUTOMATION,
                        headline = { stringResource(R.string.user_preferences_automation_title) },
                        trailingContent = if (automationFeatureStatus == FeatureStatus.NOT_AVAILABLE) {
                            {
                                FeatureBadgeSmall(onClick = { onNavigateToGroup(UserPreferencesGroupId.AUTOMATION) })
                            }
                        } else {
                            null
                        },
                        supportingContent = {
                            AutomationPreferenceValue(
                                value = AutomationPreference.getValue(values),
                                appDetails = appDetails,
                                links = links,
                                descriptionEnabled = false,
                            )
                        },
                        onClick = { onNavigateToGroup(UserPreferencesGroupId.AUTOMATION) },
                    )
                )
                add(
                    UserPreferencesGroup(
                        id = UserPreferencesGroupId.AUTOMATION_DELAY,
                        headline = { stringResource(R.string.user_preferences_automation_delay_sec_title) },
                        enabled = automationDelayEnabled,
                        supportingContent = {
                            AutomationDelayPreferenceValue(
                                value = AutomationDelayPreference.getValue(values),
                            )
                        },
                        onClick = { onNavigateToGroup(UserPreferencesGroupId.AUTOMATION_DELAY) },
                    )
                )
                add(
                    UserPreferencesGroup(
                        id = UserPreferencesGroupId.COORDINATE_FORMAT,
                        headline = { stringResource(R.string.user_preferences_coordinate_format_title) },
                        supportingContent = {
                            CoordinateFormatPreferenceValue(
                                value = CoordinateFormatPreference.getValue(values),
                            )
                        },
                        onClick = { onNavigateToGroup(UserPreferencesGroupId.COORDINATE_FORMAT) },
                    )
                )
                if (BuildConfig.DEBUG) {
                    add(
                        UserPreferencesGroup(
                            id = UserPreferencesGroupId.DEVELOPER_OPTIONS,
                            headline = { stringResource(R.string.user_preferences_developer_title) },
                            onClick = { onNavigateToGroup(UserPreferencesGroupId.DEVELOPER_OPTIONS) },
                        )
                    )
                }
            }
            SegmentedList(
                values = values,
                modifier = Modifier.padding(top = spacing.mediumAdaptive),
                itemHeadline = { it.headline() },
                itemIsSelected = { it.id == currentGroupId },
                itemOnClick = { it.onClick() },
                itemEnabled = { it.enabled },
                itemTrailingContent = { it.trailingContent },
                itemSupportingContent = { it.supportingContent },
                itemTestTag = { "geoShareUserPreferencesGroup_${it.id}" },
            )
        }
    }
}

@Composable
private fun ConnectionPermissionPreferenceValue(value: Permission) {
    Text(
        stringResource(
            when (value) {
                Permission.ALWAYS -> R.string.yes
                Permission.ASK -> R.string.user_preferences_connection_option_ask
                Permission.NEVER -> R.string.no
            }
        )
    )
}

@Composable
private fun CoordinateFormatPreferenceValue(value: CoordinateFormat) {
    Text(
        stringResource(
            when (value) {
                CoordinateFormat.DEC -> R.string.user_preferences_coordinate_format_option_dec
                CoordinateFormat.DEG_MIN_SEC -> R.string.user_preferences_coordinate_format_option_deg_min_sec
            }
        )
    )
}

@Composable
private fun AutomationPreferenceValue(
    value: Automation,
    appDetails: AppDetails,
    links: List<Link>,
    descriptionEnabled: Boolean = true,
) {
    var output by retain { mutableStateOf<Output?>(null) }
    LaunchedEffect(value, links) {
        output = value.toOutput { links.findByUUID(it) }
    }
    output?.let { output ->
        val label = output.automationLabel(appDetails)
        val description = output.takeIf { descriptionEnabled }?.getAutomationDescription()
        Row(
            horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.tiny),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            output.getIcon(appDetails)?.let {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    IconFromDescriptor(it, contentDescription = null)
                }
            }
            if (description != null) {
                Column {
                    Text(label)
                    Text(
                        description(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                Text(label)
            }
        }
    }
}

@Composable
private fun AutomationDelayPreferenceValue(value: Duration) {
    val seconds = value.toInt(DurationUnit.SECONDS)
    Text(pluralStringResource(R.plurals.seconds, seconds, seconds))
}

@Composable
private fun UserPreferencesDetailPane(
    currentGroupId: UserPreferencesGroupId,
    apps: DataTypes,
    appDetails: AppDetails,
    automationFeatureStatus: FeatureStatus,
    billingAppNameResId: Int,
    links: List<Link>,
    values: UserPreferencesValues,
    wide: Boolean,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
) {
    when (currentGroupId) {
        UserPreferencesGroupId.CONNECTION_PERMISSION ->
            UserPreferencesControls(
                titleResId = R.string.user_preferences_connection_title,
                description = {
                    stringResource(
                        R.string.user_preferences_connection_description,
                        stringResource(R.string.app_name),
                    )
                },
                billingAppNameResId = billingAppNameResId,
                wide = wide,
                onBack = onBack,
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            ) {
                userPreferencesOptionsControl(
                    userPreference = ConnectionPermissionPreference,
                    values = values,
                    onValueChange = onValueChange,
                    optionGroups = ConnectionPermissionPreference.getOptionGroups(),
                    getItemTestTag = { option ->
                        "geoShareUserPreferenceConnectionPermission_${option}"
                    },
                ) { option ->
                    ConnectionPermissionPreferenceValue(option)
                }
            }

        UserPreferencesGroupId.COORDINATE_FORMAT ->
            UserPreferencesControls(
                titleResId = R.string.user_preferences_coordinate_format_title,
                description = {
                    stringResource(R.string.user_preferences_coordinate_format_description)
                },
                billingAppNameResId = billingAppNameResId,
                wide = wide,
                onBack = onBack,
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            ) {
                userPreferencesOptionsControl(
                    userPreference = CoordinateFormatPreference,
                    values = values,
                    onValueChange = onValueChange,
                    optionGroups = CoordinateFormatPreference.getOptionGroups(),
                    getItemTestTag = { option ->
                        "geoShareUserPreferenceCoordinateFormat_${option}"
                    },
                ) { option ->
                    Column {
                        CoordinateFormatPreferenceValue(option)
                        Text(
                            Point.example.let { examplePoint ->
                                when (option) {
                                    CoordinateFormat.DEC -> CoordsFormat.formatDecCoords(examplePoint)
                                    CoordinateFormat.DEG_MIN_SEC -> CoordsFormat.formatDegMinSecCoords(examplePoint)
                                }
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

        UserPreferencesGroupId.AUTOMATION ->
            UserPreferencesControls(
                titleResId = R.string.user_preferences_automation_title,
                billingAppNameResId = billingAppNameResId,
                wide = wide,
                description = {
                    stringResource(R.string.user_preferences_automation_description)
                },
                featureStatus = automationFeatureStatus,
                onBack = onBack,
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            ) {
                userPreferencesOptionsControl(
                    userPreference = AutomationPreference,
                    optionGroups = AutomationPreference.getOptionGroups(apps, appDetails, links),
                    values = values,
                    enabled = automationFeatureStatus == FeatureStatus.AVAILABLE,
                    getItemTestTag = { option ->
                        try {
                            Json.encodeToString(option)
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                            .let { serializedString -> "geoShareUserPreferenceAutomation_$serializedString" }
                    },
                    onValueChange = onValueChange,
                ) { value ->
                    AutomationPreferenceValue(value, appDetails, links)
                }
            }

        UserPreferencesGroupId.AUTOMATION_DELAY ->
            UserPreferencesControls(
                titleResId = R.string.user_preferences_automation_delay_sec_title,
                billingAppNameResId = billingAppNameResId,
                wide = wide,
                description = {
                    stringResource(R.string.user_preferences_automation_delay_sec_description)
                },
                featureStatus = automationFeatureStatus,
                onBack = onBack,
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            ) {
                userPreferencesTextControl(
                    userPreference = AutomationDelayPreference,
                    values = values,
                    onValueChange = onValueChange,
                    error = {
                        stringResource(
                            R.string.user_preferences_number_error_range,
                            AutomationDelayPreference.minSec,
                            AutomationDelayPreference.maxSec,
                        )
                    },
                    suffix = {
                        stringResource(R.string.seconds_unit)
                    },
                )
            }

        UserPreferencesGroupId.DEVELOPER_OPTIONS ->
            UserPreferencesControls(
                titleResId = R.string.user_preferences_developer_title,
                billingAppNameResId = billingAppNameResId,
                wide = wide,
                onBack = onBack,
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            ) {
                item {
                    ParagraphHtml(
                        stringResource(R.string.user_preferences_changelog_shown_for_version_code_title),
                        Modifier.padding(bottom = LocalSpacing.current.smallAdaptive),
                    )
                }
                userPreferencesTextControl(
                    userPreference = ChangelogShownForVersionCodePreference,
                    values = values,
                    onValueChange = onValueChange,
                    modifier = Modifier.testTag("geoShareUserPreferenceChangelogShownForVersionCode"),
                )
                item {
                    ParagraphHtml(
                        stringResource(R.string.user_preferences_last_run_version_code_title),
                        Modifier.padding(
                            top = LocalSpacing.current.mediumAdaptive,
                            bottom = LocalSpacing.current.smallAdaptive,
                        ),
                    )
                }
                userPreferencesTextControl(
                    userPreference = IntroShowForVersionCodePreference,
                    values = values,
                    onValueChange = onValueChange,
                )
                item {
                    ParagraphHtml(
                        stringResource(R.string.user_preferences_billing_cached_product_id),
                        Modifier.padding(
                            top = LocalSpacing.current.mediumAdaptive,
                            bottom = LocalSpacing.current.smallAdaptive,
                        ),
                    )
                }
                userPreferencesTextControl(
                    userPreference = BillingCachedProductIdPreference,
                    values = values,
                    onValueChange = onValueChange,
                )
            }

        UserPreferencesGroupId.LINKS -> {}
    }
}

@Composable
fun UserPreferencesControls(
    titleResId: Int,
    billingAppNameResId: Int,
    wide: Boolean,
    description: (@Composable () -> String)? = null,
    featureStatus: FeatureStatus = FeatureStatus.AVAILABLE,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val spacing = LocalSpacing.current

    Box {
        Column {
            ScrollablePane(
                title = {
                    Text(
                        stringResource(titleResId),
                        Modifier.padding(bottom = spacing.tiny), // Align with the first item of the list pane
                    )
                },
                onBack = onBack.takeUnless { wide },
                modifier = Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .testTag("geoShareUserPreferencesControlsPane"),
            ) {
                description?.let { description ->
                    item {
                        ParagraphHtml(
                            description(),
                            Modifier
                                .padding(bottom = spacing.mediumAdaptive)
                                .run {
                                    if (featureStatus == FeatureStatus.NOT_AVAILABLE) {
                                        alpha(0.7f)
                                    } else {
                                        this
                                    }
                                },
                        )
                    }
                }
                this.content()
            }
        }
        if (featureStatus == FeatureStatus.NOT_AVAILABLE) {
            FeatureBadgeLarge(
                billingAppNameResId = billingAppNameResId,
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            )
        }
    }
}

fun <T> LazyListScope.userPreferencesOptionsControl(
    userPreference: OptionsPreference<T>,
    values: UserPreferencesValues,
    optionGroups: List<List<T>>,
    enabled: Boolean = true,
    getItemTestTag: ((option: T) -> String)? = null,
    onValueChange: ((MutablePreferences) -> Unit) -> Unit,
    option: @Composable (option: T) -> Unit,
) {
    val value = if (enabled) {
        userPreference.getValue(values)
    } else {
        userPreference.default
    }
    optionGroups.forEachIndexed { i, values ->
        item {
            RadioButtonGroup(
                selectedValue = value,
                onSelect = {
                    onValueChange { preferences ->
                        userPreference.setValue(preferences, it)
                    }
                },
                values = values,
                enabled = enabled,
                modifier = Modifier.run {
                    if (i == 0) {
                        padding(top = LocalSpacing.current.tinyAdaptive)
                    } else {
                        this
                    }
                },
                getTestTag = getItemTestTag,
                option = option,
            )
        }
        if (i < optionGroups.size - 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(vertical = LocalSpacing.current.tinyAdaptive),
                    thickness = Dp.Hairline,
                )
            }
        }
    }
}

fun <T> LazyListScope.userPreferencesTextControl(
    userPreference: TextUserPreference<T>,
    values: UserPreferencesValues,
    onValueChange: ((MutablePreferences) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: (@Composable () -> String)? = null,
    suffix: (@Composable () -> String)? = null,
) {
    item {
        val value = userPreference.getValue(values)
        val (inputValue, setInputValue) = remember { mutableStateOf(userPreference.serialize(value)) }
        val isValid = userPreference.isValid(inputValue)

        TextField(
            value = inputValue,
            onValueChange = {
                setInputValue(it)
                onValueChange { preferences ->
                    userPreference.setValue(preferences, userPreference.deserialize(it))
                }
            },
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            suffix = suffix?.let { suffix ->
                {
                    Text(
                        suffix(),
                        color = if (isValid) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
            },
            trailingIcon = {
                IconButton({
                    setInputValue(userPreference.serialize(userPreference.default))
                    onValueChange { preferences ->
                        userPreference.setValue(preferences, userPreference.default)
                    }
                }) {
                    Icon(
                        Icons.Default.Refresh,
                        stringResource(R.string.reset),
                    )
                }
            },
            supportingText = if (!isValid && error != null) {
                {
                    Text(error())
                }
            } else {
                null
            },
            isError = !isValid,
            singleLine = true,
        )
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = null,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
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
                UserPreferencesScreen(
                    initialGroupId = null,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
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
                UserPreferencesScreen(
                    initialGroupId = null,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionPermissionPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkConnectionPermissionPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletConnectionPermissionPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoordinateFormatPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.COORDINATE_FORMAT,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkCoordinateFormatPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.COORDINATE_FORMAT,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletCoordinateFormatPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.COORDINATE_FORMAT,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    apps = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    ),
                    appDetails = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    automationFeatureStatus = FeatureStatus.AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    apps = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    ),
                    appDetails = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    automationFeatureStatus = FeatureStatus.AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletAutomationPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    apps = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    ),
                    appDetails = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    automationFeatureStatus = FeatureStatus.AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    apps = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    ),
                    appDetails = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    automationFeatureStatus = FeatureStatus.NOT_AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    apps = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    ),
                    appDetails = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    automationFeatureStatus = FeatureStatus.NOT_AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletAutomationFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    apps = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    ),
                    appDetails = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    automationFeatureStatus = FeatureStatus.NOT_AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationDelayPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION_DELAY,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationDelayPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION_DELAY,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TableAutomationDelayPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.AUTOMATION_DELAY,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.AVAILABLE,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeveloperOptionsPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.DEVELOPER_OPTIONS,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDeveloperOptionsPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.DEVELOPER_OPTIONS,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TableDeveloperOptionsPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    initialGroupId = UserPreferencesGroupId.DEVELOPER_OPTIONS,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    automationFeatureStatus = FeatureStatus.LOADING,
                    billingAppNameResId = R.string.app_name_pro,
                    links = defaultFakeLinks,
                    userPreferencesValues = UserPreferencesValues(
                        automation = SavePointsGpxAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinksScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

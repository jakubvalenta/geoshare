package page.ooooo.geoshare.ui

import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.converters.*
import page.ooooo.geoshare.ui.Filter.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

private data class Documentations(
    val documentations: List<Documentation>,
    val defaultHandlersEnabled: Map<String, Boolean>,
    val newChangelogShownForVersionCode: Int,
)

private sealed class Filter(val titleResId: Int) {
    class All : Filter(R.string.url_converters_filter_all)
    class Recent : Filter(R.string.url_converters_filter_recent)
    class Enabled : Filter(R.string.url_converters_default_handler_enabled)
    class Disabled : Filter(R.string.url_converters_default_handler_disabled)
}

private fun getDocumentations(
    filter: Filter,
    changelogShownForVersionCode: Int?,
    isDefaultHandlerEnabled: (uriString: String) -> Boolean,
    urlConverters: List<UrlConverter>,
): Documentations {
    val defaultHandlersEnabled = mutableMapOf<String, Boolean>()
    var newChangelogShownForVersionCode = 1
    val filteredUrlConverterDocumentations = urlConverters.mapNotNull { urlConverter ->
        val filteredInputs = urlConverter.documentation.inputs.filter { input ->
            if (filter is Recent && changelogShownForVersionCode != null && input.addedInVersionCode <= changelogShownForVersionCode) {
                return@filter false
            }
            if (input.addedInVersionCode > newChangelogShownForVersionCode) {
                newChangelogShownForVersionCode = input.addedInVersionCode
            }
            if (input is DocumentationInput.Url) {
                val defaultHandlerEnabled = isDefaultHandlerEnabled(input.urlString)
                if (filter is Enabled) {
                    if (!defaultHandlerEnabled) {
                        return@filter false
                    }
                } else if (filter is Disabled) {
                    if (defaultHandlerEnabled) {
                        return@filter false
                    }
                }
                defaultHandlersEnabled[input.urlString] = defaultHandlerEnabled
            } else if (filter is Disabled) {
                return@filter false
            }
            true
        }
        if (filteredInputs.isEmpty()) {
            return@mapNotNull null
        }
        urlConverter.documentation.copy(inputs = filteredInputs)
    }
    return Documentations(
        documentations = filteredUrlConverterDocumentations,
        defaultHandlersEnabled = defaultHandlersEnabled.toMap(),
        newChangelogShownForVersionCode = newChangelogShownForVersionCode,
    )
}

private fun getDocumentations(
    context: Context,
    filter: Filter,
    changelogShownForVersionCode: Int?,
    viewModel: ConversionViewModel,
) = getDocumentations(
    filter,
    changelogShownForVersionCode,
    isDefaultHandlerEnabled = { viewModel.isDefaultHandlerEnabled(context.packageManager, it) },
    viewModel.urlConverters,
)

private val trimHttpsRegex = """^https://""".toRegex()

private fun trimHttps(urlString: String): String = urlString.replace(trimHttpsRegex, "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlConvertersScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val changelogShown by viewModel.changelogShown.collectAsState()
    val changelogShownForVersionCode by viewModel.changelogShownForVersionCode.collectAsState()
    val filters = buildList {
        add(All())
        if (!changelogShown) {
            add(Recent())
        }
        add(Enabled())
        add(Disabled())
    }
    var filter by remember { mutableStateOf(if (!changelogShown) Recent() else All()) }
    var documentations by remember(filter, changelogShownForVersionCode) {
        mutableStateOf(getDocumentations(context, filter, changelogShownForVersionCode, viewModel))
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        documentations = getDocumentations(context, filter, changelogShownForVersionCode, viewModel)
    }

    UrlConvertersScreen(
        documentations = documentations,
        filter = filter,
        filters = filters,
        onBack = {
            viewModel.setChangelogShownForVersionCode(documentations.newChangelogShownForVersionCode)
            onBack()
        },
        onChangeFilter = { filter = it },
        onShowOpenByDefaultSettings = { viewModel.showOpenByDefaultSettings(context, settingsLauncher) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrlConvertersScreen(
    documentations: Documentations,
    filter: Filter,
    filters: List<Filter>,
    onBack: () -> Unit,
    onChangeFilter: (filter: Filter) -> Unit,
    onShowOpenByDefaultSettings: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    var filterExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.url_converters_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back_content_description)
                        )
                    }
                },
                actions = {
                    IconButton(
                        { onShowOpenByDefaultSettings() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            stringResource(R.string.url_converters_settings_button_content_description),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.url_converters_text, appName),
                Modifier.padding(top = Spacing.tiny),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineBreak = LineBreak.Paragraph,
                ),
            )
            Box(Modifier.padding(vertical = Spacing.medium - 7.dp)) {
                ElevatedFilterChip(
                    selected = false,
                    onClick = { filterExpanded = true },
                    label = { Text(stringResource(filter.titleResId, appName)) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false },
                ) {
                    filters.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.titleResId, appName)) },
                            onClick = {
                                filterExpanded = false
                                onChangeFilter(it)
                                scope.launch {
                                    listState.scrollToItem(0)
                                }
                            },
                        )
                    }
                }
            }
            if (filter is Recent) {
                ElevatedCard(
                    Modifier
                        .testTag("geoShareUrlConvertersRecentCard")
                        .padding(bottom = Spacing.medium),
                    shape = OutlinedTextFieldDefaults.shape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(Spacing.small),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BadgedBox(badge = { Badge() }) {
                            Icon(Icons.Default.Notifications, null)
                        }
                        Text(
                            stringResource(R.string.url_converters_recent_info, appName),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            LazyColumn(state = listState) {
                documentations.documentations.forEach { urlConverterDocumentation ->
                    item {
                        Text(
                            stringResource(urlConverterDocumentation.nameResId),
                            Modifier
                                .testTag("geoShareUrlConvertersHeadline")
                                .padding(bottom = Spacing.small),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    item {
                        Column(
                            Modifier.padding(bottom = Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            urlConverterDocumentation.inputs.forEach { input ->
                                when (input) {
                                    is DocumentationInput.Text -> {
                                        Text(
                                            stringResource(input.descriptionResId),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }

                                    is DocumentationInput.Url -> {
                                        FlowRow(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalArrangement = Arrangement.spacedBy(1.dp),
                                            itemVerticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                trimHttps(input.urlString),
                                                Modifier.padding(end = Spacing.tiny),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                            Text(
                                                stringResource(
                                                    if (
                                                        documentations.defaultHandlersEnabled.getOrDefault(
                                                            input.urlString, false
                                                        )
                                                    ) {
                                                        R.string.url_converters_default_handler_enabled
                                                    } else {
                                                        R.string.url_converters_default_handler_disabled
                                                    },
                                                    appName,
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
        UrlConvertersScreen(
            filter = Recent(),
            filters = listOf(Recent(), All(), Enabled(), Disabled()),
            documentations = Documentations(
                documentations = listOf(
                    GeoUrlConverter(),
                    GoogleMapsUrlConverter(),
                    OpenStreetMapUrlConverter(),
                    CoordinatesUrlConverter(),
                ).map { it.documentation },
                defaultHandlersEnabled = mapOf(
                    "https://maps.apple" to true,
                    "https://maps.apple.com" to false,
                ),
                newChangelogShownForVersionCode = 20,
            ),
            onBack = {},
            onChangeFilter = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UrlConvertersScreen(
            filter = Recent(),
            filters = listOf(Recent(), All(), Enabled(), Disabled()),
            documentations = Documentations(
                documentations = listOf(
                    GeoUrlConverter(),
                    GoogleMapsUrlConverter(),
                    OpenStreetMapUrlConverter(),
                    CoordinatesUrlConverter(),
                ).map { it.documentation },
                defaultHandlersEnabled = mapOf(
                    "https://maps.apple" to true,
                    "https://maps.apple.com" to false,
                ),
                newChangelogShownForVersionCode = 20,
            ),
            onBack = {},
            onChangeFilter = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AllPreview() {
    AppTheme {
        UrlConvertersScreen(
            filter = All(),
            filters = listOf(All(), Enabled(), Disabled()),
            documentations = Documentations(
                documentations = listOf(
                    GeoUrlConverter(),
                    GoogleMapsUrlConverter(),
                    OpenStreetMapUrlConverter(),
                    CoordinatesUrlConverter(),
                ).map { it.documentation },
                defaultHandlersEnabled = mapOf(
                    "https://maps.apple" to true,
                    "https://maps.apple.com" to false,
                ),
                newChangelogShownForVersionCode = 20,
            ),
            onBack = {},
            onChangeFilter = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAllPreview() {
    AppTheme {
        UrlConvertersScreen(
            filter = All(),
            filters = listOf(All(), Enabled(), Disabled()),
            documentations = Documentations(
                documentations = listOf(
                    GeoUrlConverter(),
                    GoogleMapsUrlConverter(),
                    OpenStreetMapUrlConverter(),
                    CoordinatesUrlConverter(),
                ).map { it.documentation },
                defaultHandlersEnabled = mapOf(
                    "https://maps.apple" to true,
                    "https://maps.apple.com" to false,
                ),
                newChangelogShownForVersionCode = 20,
            ),
            onBack = {},
            onChangeFilter = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

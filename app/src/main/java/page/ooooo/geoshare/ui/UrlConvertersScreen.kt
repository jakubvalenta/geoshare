package page.ooooo.geoshare.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.converters.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

data class Documentations(
    val documentations: List<Documentation>,
    val defaultHandlersEnabled: Map<String, Boolean>,
    val newLastInputVersionCode: Int,
)

sealed class Filter(val titleResId: Int) {
    class All : Filter(R.string.url_converters_filter_all)
    class Recent : Filter(R.string.url_converters_filter_recent)
    class Enabled : Filter(R.string.url_converters_default_handler_enabled)
    class Disabled : Filter(R.string.url_converters_default_handler_disabled)
}

fun getDocumentations(
    filter: Filter,
    intentTools: IntentTools,
    lastInputVersionCode: Int?,
    packageManager: PackageManager,
    urlConverters: List<UrlConverter>,
): Documentations {
    val defaultHandlersEnabled = mutableMapOf<String, Boolean>()
    var newLastInputVersionCode = 1
    val filteredUrlConverterDocumentations = urlConverters.mapNotNull { urlConverter ->
        val filteredInputs = urlConverter.documentation.inputs.filter { input ->
            if (filter is Filter.Recent && lastInputVersionCode != null && input.addedInVersionCode <= lastInputVersionCode) {
                return@filter false
            }
            if (input.addedInVersionCode > newLastInputVersionCode) {
                newLastInputVersionCode = input.addedInVersionCode
            }
            if (input is DocumentationInput.Url) {
                val defaultHandlerEnabled = intentTools.isDefaultHandlerEnabled(packageManager, input.urlString)
                if (filter is Filter.Enabled) {
                    if (!defaultHandlerEnabled) {
                        return@filter false
                    }
                } else if (filter is Filter.Disabled) {
                    if (defaultHandlerEnabled) {
                        return@filter false
                    }
                }
                defaultHandlersEnabled[input.urlString] = defaultHandlerEnabled
            } else if (filter is Filter.Disabled) {
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
        newLastInputVersionCode = newLastInputVersionCode,
    )
}

fun getDocumentations(
    context: Context,
    filter: Filter,
    lastInputVersionCode: Int?,
    viewModel: ConversionViewModel,
) = getDocumentations(
    filter,
    viewModel.intentTools,
    lastInputVersionCode,
    context.packageManager,
    viewModel.urlConverters,
)

private val trimHttpsRegex = """^https://""".toRegex()

fun trimHttps(urlString: String): String = urlString.replace(trimHttpsRegex, "")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun UrlConvertersScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lastInputShown by viewModel.lastInputShown.collectAsState()
    val lastInputVersionCode by viewModel.lastInputVersionCode.collectAsState()
    val filters = buildList {
        if (!lastInputShown) {
            add(Filter.Recent())
        }
        add(Filter.All())
        add(Filter.Enabled())
        add(Filter.Disabled())
    }
    val initialFilter = if (!lastInputShown) Filter.Recent() else Filter.All()
    var filter by remember { mutableStateOf(initialFilter) }
    var documentations = getDocumentations(context, filter, lastInputVersionCode, viewModel)
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        // TODO Test this refresh.
        documentations = getDocumentations(context, filter, lastInputVersionCode, viewModel)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.setLastInputVersionCode(documentations.newLastInputVersionCode)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    UrlConvertersScreen(
        filter = filter,
        filters = filters,
        documentations = documentations,
        onBack = onBack,
        onChangeFilter = { filter = it },
        onShowOpenByDefaultSettings = { viewModel.intentTools.showOpenByDefaultSettings(context, settingsLauncher) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlConvertersScreen(
    filter: Filter,
    filters: List<Filter>,
    documentations: Documentations,
    onBack: () -> Unit,
    onChangeFilter: (filter: Filter) -> Unit,
    onShowOpenByDefaultSettings: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    var filterExpanded by remember { mutableStateOf(false) }

    Scaffold(
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
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Text(
                stringResource(R.string.url_converters_text, appName),
                Modifier.padding(top = Spacing.tiny),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineBreak = LineBreak.Paragraph,
                ),
            )
            Box {
                FilterChip(
                    selected = true,
                    onClick = { filterExpanded = true },
                    label = { Text(stringResource(filter.titleResId, appName)) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
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
                            },
                        )
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                documentations.documentations.forEach { urlConverterDocumentation ->
                    item {
                        Text(
                            stringResource(urlConverterDocumentation.nameResId),
                            Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    urlConverterDocumentation.inputs.forEach { input ->
                        item {
                            when (input) {
                                is DocumentationInput.Text -> {
                                    Text(
                                        stringResource(input.descriptionResId),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                                is DocumentationInput.Url -> {
                                    val defaultHandlerEnabled =
                                        documentations.defaultHandlersEnabled.getOrDefault(
                                            input.urlString, false
                                        )
                                    if (defaultHandlerEnabled) {
                                        Column {
                                            Text(
                                                trimHttps(input.urlString),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                            Text(
                                                stringResource(
                                                    R.string.url_converters_default_handler_enabled,
                                                    appName,
                                                ),
                                                Modifier.padding(vertical = Spacing.tiny),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    trimHttps(input.urlString),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                                Text(
                                                    stringResource(
                                                        R.string.url_converters_default_handler_disabled,
                                                        appName,
                                                    ),
                                                    Modifier.padding(vertical = Spacing.tiny),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                            VerticalDivider(
                                                Modifier
                                                    .height(26.dp)
                                                    .padding(end = 12.dp)
                                            )
                                            IconButton({ onShowOpenByDefaultSettings() }) {
                                                Icon(
                                                    Icons.Default.Settings,
                                                    stringResource(R.string.url_converters_settings_button_content_description),
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
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UrlConvertersScreen(
            filter = Filter.Recent(),
            filters = listOf(Filter.Recent(), Filter.All(), Filter.Enabled(), Filter.Disabled()),
            documentations = Documentations(
                documentations = listOf(
                    GeoUrlConverter(),
                    AppleMapsUrlConverter(),
                    CoordinatesUrlConverter(),
                ).map { it.documentation },
                defaultHandlersEnabled = mapOf(
                    "https://maps.apple" to true,
                    "https://maps.apple.com" to false,
                ),
                newLastInputVersionCode = 20,
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
            filter = Filter.Recent(),
            filters = listOf(Filter.Recent(), Filter.All(), Filter.Enabled(), Filter.Disabled()),
            documentations = Documentations(
                documentations = listOf(
                    GeoUrlConverter(),
                    AppleMapsUrlConverter(),
                    CoordinatesUrlConverter(),
                ).map { it.documentation },
                defaultHandlersEnabled = mapOf(
                    "https://maps.apple" to true,
                    "https://maps.apple.com" to false,
                ),
                newLastInputVersionCode = 20,
            ),
            onBack = {},
            onChangeFilter = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

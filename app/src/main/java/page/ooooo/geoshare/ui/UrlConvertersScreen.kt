package page.ooooo.geoshare.ui

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.converters.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun UrlConvertersScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(urlConverterDocumentationFilters[0]) }
    val userPreferencesValues = viewModel.userPreferencesValues.collectAsStateWithLifecycle()
    var urlConverterDocumentations = getUrlConverterDocumentations(
        viewModel.urlConverters,
        selectedFilter,
        userPreferencesValues.value.changelogShownForVersionCodeValue,
        context.packageManager
    )
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        urlConverterDocumentations = getUrlConverterDocumentations(
            viewModel.urlConverters,
            selectedFilter,
            userPreferencesValues.value.changelogShownForVersionCodeValue,
            context.packageManager
        )
    }

    UrlConvertersScreen(
        selectedFilter = selectedFilter,
        urlConverterDocumentations = urlConverterDocumentations,
        onBack = onBack,
        onSelectFilter = { selectedFilter = it },
        onShowOpenByDefaultSettings = { viewModel.showOpenByDefaultSettings(context, settingsLauncher) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlConvertersScreen(
    selectedFilter: UrlConverterDocumentationFilter,
    urlConverterDocumentations: UrlConverterDocumentations,
    onBack: () -> Unit,
    onSelectFilter: (filter: UrlConverterDocumentationFilter) -> Unit,
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
                    label = { Text(stringResource(selectedFilter.titleResId, appName)) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                )
                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false },
                ) {
                    urlConverterDocumentationFilters.forEach { filter ->
                        if (filter != selectedFilter) {
                            DropdownMenuItem(
                                text = { Text(stringResource(filter.titleResId, appName)) },
                                onClick = {
                                    filterExpanded = false
                                    onSelectFilter(filter)
                                },
                            )
                        }
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                urlConverterDocumentations.documentations.forEach { urlConverterDocumentation ->
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
                                is UrlConverterDocumentationInput.Text -> {
                                    Text(
                                        stringResource(input.descriptionResId),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                                is UrlConverterDocumentationInput.Url -> {
                                    val defaultHandlerEnabled =
                                        urlConverterDocumentations.defaultHandlersEnabled.getOrDefault(
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

private val trimHttpsRegex = """^https://""".toRegex()

fun trimHttps(urlString: String): String = urlString.replace(trimHttpsRegex, "")

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UrlConvertersScreen(
            selectedFilter = urlConverterDocumentationFilters[0],
            urlConverterDocumentations = UrlConverterDocumentations(
                documentations = listOf(
                    GeoUrlConverter(),
                    AppleMapsUrlConverter(),
                    CoordinatesUrlConverter(),
                ).map { it.documentation },
                defaultHandlersEnabled = mapOf(
                    "https://maps.apple" to true,
                    "https://maps.apple.com" to false,
                ),
            ),
            onBack = {},
            onSelectFilter = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UrlConvertersScreen(
            selectedFilter = urlConverterDocumentationFilters[0],
            urlConverterDocumentations = UrlConverterDocumentations(
                documentations = listOf(
                    GeoUrlConverter(),
                    AppleMapsUrlConverter(),
                    CoordinatesUrlConverter(),
                ).map { it.documentation },
                defaultHandlersEnabled = mapOf(
                    "https://maps.apple" to true,
                    "https://maps.apple.com" to false,
                ),
            ),
            onBack = {},
            onSelectFilter = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

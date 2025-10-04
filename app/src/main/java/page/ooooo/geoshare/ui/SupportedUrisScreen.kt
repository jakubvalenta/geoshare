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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.ConversionViewModel.SupportedUri
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.UrlConverter
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

sealed class FilterOption(val titleResId: Int) {
    // TODO Add 'New'
    class All : FilterOption(R.string.supported_uris_filter_all)
    class Enabled : FilterOption(R.string.supported_uris_default_handler_enabled)
    class Disabled : FilterOption(R.string.supported_uris_default_handler_disabled)
}

private val filterOptions = listOf(FilterOption.All(), FilterOption.Enabled(), FilterOption.Disabled())

private val trimHttpsRegex = """^https://""".toRegex()

fun trimHttps(string: String) = string.replace(trimHttpsRegex, "")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun SupportedUrisScreen(
    onNavigateToMainScreen: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    var urlConvertersAndSupportedUris by remember { mutableStateOf(viewModel.getUrlConvertersAndSupportedUris(context.packageManager)) }
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        urlConvertersAndSupportedUris = viewModel.getUrlConvertersAndSupportedUris(context.packageManager)
    }

    SupportedUrisScreen(
        urlConvertersAndSupportedUris = urlConvertersAndSupportedUris,
        onNavigateToMainScreen = onNavigateToMainScreen,
        onShowOpenByDefaultSettings = { viewModel.showOpenByDefaultSettings(context, settingsLauncher) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportedUrisScreen(
    urlConvertersAndSupportedUris: List<Pair<UrlConverter, List<SupportedUri>>>,
    onNavigateToMainScreen: () -> Unit = {},
    onShowOpenByDefaultSettings: () -> Unit = {},
) {
    val appName = stringResource(R.string.app_name)
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedFilterOption by remember { mutableStateOf(filterOptions[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.supported_uris_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToMainScreen) {
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
                stringResource(R.string.supported_uris_text, appName),
                Modifier.padding(top = Spacing.tiny),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineBreak = LineBreak.Paragraph,
                ),
            )
            Box {
                FilterChip(
                    selected = true,
                    onClick = { filterExpanded = true },
                    label = { Text(stringResource(selectedFilterOption.titleResId, appName)) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                )
                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false },
                ) {
                    filterOptions.forEach { filterOption ->
                        if (filterOption != selectedFilterOption) {
                            DropdownMenuItem(
                                text = { Text(stringResource(filterOption.titleResId, appName)) },
                                modifier = Modifier.testTag("geoShareMainMenuUserPreferences"),
                                onClick = {
                                    filterExpanded = false
                                    selectedFilterOption = filterOption
                                },
                            )
                        }
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                val filteredUrlConvertersAndSupportedUris = when (selectedFilterOption) {
                    is FilterOption.All -> urlConvertersAndSupportedUris

                    is FilterOption.Enabled -> urlConvertersAndSupportedUris.mapNotNull { (urlConverter, supportedUris) ->
                        supportedUris.filter { it.defaultHandlerEnabled }.takeIf { it.isNotEmpty() }
                            ?.let { filteredSupportedUris ->
                                urlConverter to filteredSupportedUris
                            }
                    }

                    is FilterOption.Disabled -> urlConvertersAndSupportedUris.mapNotNull { (urlConverter, supportedUris) ->
                        supportedUris.filter { !it.defaultHandlerEnabled }.takeIf { it.isNotEmpty() }
                            ?.let { filteredSupportedUris ->
                                urlConverter to filteredSupportedUris
                            }
                    }
                }
                filteredUrlConvertersAndSupportedUris.forEach { (urlConverter, supportedUris) ->
                    // TODO Show only http links or text description.
                    item {
                        Text(
                            stringResource(urlConverter.nameResId),
                            Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    supportedUris.forEach { supportedUri ->
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        trimHttps(supportedUri.uriString),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        stringResource(
                                            if (supportedUri.defaultHandlerEnabled) {
                                                R.string.supported_uris_default_handler_enabled
                                            } else {
                                                R.string.supported_uris_default_handler_disabled
                                            },
                                            appName,
                                        ),
                                        Modifier.padding(vertical = Spacing.tiny),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                if (!supportedUri.defaultHandlerEnabled) {
                                    VerticalDivider(
                                        Modifier
                                            .height(26.dp)
                                            .padding(end = 12.dp)
                                    )
                                    IconButton({ onShowOpenByDefaultSettings() }) {
                                        Icon(Icons.Default.Settings, null)
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
        SupportedUrisScreen(
            urlConvertersAndSupportedUris = listOf(
                GoogleMapsUrlConverter() to listOf(
                    SupportedUri("https://maps.app.goo.gl", true),
                    SupportedUri("https://app.goo.gl/maps", false),
                    SupportedUri("https://maps.google.com", true),
                    SupportedUri("https://goo.gl/maps", true),
                    SupportedUri("https://google.com/maps", false),
                    SupportedUri("https://www.google.com/maps", true),
                    SupportedUri("https://g.co/kgs", false),
                ),
                AppleMapsUrlConverter() to listOf(
                    SupportedUri("https://maps.apple", true),
                    SupportedUri("https://maps.apple.com", false),
                ),
            ),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        SupportedUrisScreen(
            urlConvertersAndSupportedUris = listOf(
                GoogleMapsUrlConverter() to listOf(
                    SupportedUri("https://maps.app.goo.gl", true),
                    SupportedUri("https://app.goo.gl/maps", false),
                    SupportedUri("https://maps.google.com", true),
                    SupportedUri("https://goo.gl/maps", true),
                    SupportedUri("https://google.com/maps", false),
                    SupportedUri("https://www.google.com/maps", true),
                    SupportedUri("https://g.co/kgs", false),
                ), AppleMapsUrlConverter() to listOf(
                    SupportedUri("https://maps.apple", true),
                    SupportedUri("https://maps.apple.com", false),
                )
            ),
        )
    }
}

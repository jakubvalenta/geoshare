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
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.CoordinatesUrlConverter
import page.ooooo.geoshare.lib.converters.GeoUrlConverter
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.SupportedInput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

private val trimHttpsRegex = """^https://""".toRegex()

fun trimHttps(string: String) = string.replace(trimHttpsRegex, "")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun MapServicesScreen(
    onNavigateToMainScreen: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    var selectedMapServiceFilter by remember { mutableStateOf(viewModel.mapServiceFilters[0]) }
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // TODO Refresh
    }

    MapServicesScreen(
        mapServiceFilters = viewModel.mapServiceFilters,
        mapServices = viewModel.getMapServices(context.packageManager, selectedMapServiceFilter),
        selectedMapServiceFilter = selectedMapServiceFilter,
        onChangeMapServiceFilter = { selectedMapServiceFilter = it },
        onNavigateToMainScreen = onNavigateToMainScreen,
        onShowOpenByDefaultSettings = { viewModel.showOpenByDefaultSettings(context, settingsLauncher) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapServicesScreen(
    mapServiceFilters: List<ConversionViewModel.MapServiceFilter>,
    mapServices: List<ConversionViewModel.MapService>,
    selectedMapServiceFilter: ConversionViewModel.MapServiceFilter,
    onChangeMapServiceFilter: (newMapServiceFilter: ConversionViewModel.MapServiceFilter) -> Unit,
    onNavigateToMainScreen: () -> Unit,
    onShowOpenByDefaultSettings: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    var filterExpanded by remember { mutableStateOf(false) }

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
                    label = { Text(stringResource(selectedMapServiceFilter.titleResId, appName)) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                )
                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false },
                ) {
                    mapServiceFilters.forEach { filterOption ->
                        if (filterOption != selectedMapServiceFilter) {
                            DropdownMenuItem(
                                text = { Text(stringResource(filterOption.titleResId, appName)) },
                                onClick = {
                                    filterExpanded = false
                                    onChangeMapServiceFilter(filterOption)
                                },
                            )
                        }
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                mapServices.forEach { mapService ->
                    item {
                        Text(
                            stringResource(mapService.urlConverter.nameResId),
                            Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    mapService.inputs.forEach { input ->
                        item {
                            when (input.supportedInput) {
                                is SupportedInput.Url -> {
                                    when (input.defaultHandlerEnabled) {
                                        true -> {
                                            Column {
                                                Text(
                                                    trimHttps(input.supportedInput.urlString),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                                Text(
                                                    stringResource(
                                                        R.string.supported_uris_default_handler_enabled,
                                                        appName,
                                                    ),
                                                    Modifier.padding(vertical = Spacing.tiny),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                        }

                                        false -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        trimHttps(input.supportedInput.urlString),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                    )
                                                    Text(
                                                        stringResource(
                                                            R.string.supported_uris_default_handler_disabled,
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
                                                    Icon(Icons.Default.Settings, null)
                                                }
                                            }
                                        }

                                        null -> {

                                        }
                                    }
                                }

                                is SupportedInput.Uri -> {
                                    Text(
                                        input.supportedInput.uriString, // TODO
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                                is SupportedInput.Text -> {
                                    Text(
                                        stringResource(input.supportedInput.descriptionResId),
                                        style = MaterialTheme.typography.bodyMedium,
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

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        MapServicesScreen(
            mapServiceFilters = listOf(
                ConversionViewModel.MapServiceFilter.All(),
                ConversionViewModel.MapServiceFilter.Recent(),
                ConversionViewModel.MapServiceFilter.Enabled(),
                ConversionViewModel.MapServiceFilter.Disabled(),
            ),
            selectedMapServiceFilter = ConversionViewModel.MapServiceFilter.All(),
            mapServices = listOf(
                ConversionViewModel.MapService(
                    urlConverter = GeoUrlConverter(),
                    inputs = listOf(
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Uri("geo:", 3),
                            null,
                        ),
                    ),
                ),
                ConversionViewModel.MapService(
                    urlConverter = GoogleMapsUrlConverter(),
                    inputs = listOf(
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://maps.app.goo.gl", 5),
                            true,
                        ),
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://app.goo.gl/maps", 5),
                            false,
                        ),
                    ),
                ),
                ConversionViewModel.MapService(
                    urlConverter = CoordinatesUrlConverter(),
                    inputs = listOf(
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Text(R.string.converter_coordinates_input_description, 20),
                            null,
                        )
                    ),
                ),
            ),
            onChangeMapServiceFilter = {},
            onNavigateToMainScreen = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        MapServicesScreen(
            mapServiceFilters = listOf(
                ConversionViewModel.MapServiceFilter.All(),
                ConversionViewModel.MapServiceFilter.Recent(),
                ConversionViewModel.MapServiceFilter.Enabled(),
                ConversionViewModel.MapServiceFilter.Disabled(),
            ),
            selectedMapServiceFilter = ConversionViewModel.MapServiceFilter.All(),
            mapServices = listOf(
                ConversionViewModel.MapService(
                    urlConverter = GoogleMapsUrlConverter(),
                    inputs = listOf(
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://maps.app.goo.gl", 5),
                            true,
                        ),
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://app.goo.gl/maps", 5),
                            false,
                        ),
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://maps.google.com", 5),
                            true,
                        ),
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://goo.gl/maps", 5),
                            true,
                        ),
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://google.com/maps", 5),
                            false,
                        ),
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://www.google.com/maps", 5),
                            true,
                        ),
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://g.co/kgs", 10),
                            true,
                        ),
                    )
                ),
                ConversionViewModel.MapService(
                    urlConverter = AppleMapsUrlConverter(),
                    inputs = listOf(
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://maps.apple", 5),
                            true,
                        ),
                        ConversionViewModel.MapServiceInput(
                            SupportedInput.Url("https://maps.apple.com", 5),
                            false,
                        ),
                    )
                ),
            ),
            onChangeMapServiceFilter = {},
            onNavigateToMainScreen = {},
            onShowOpenByDefaultSettings = {},
        )
    }
}

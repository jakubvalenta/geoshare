package page.ooooo.geoshare.ui

import android.R.attr.onClick
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.ConversionViewModel.SupportedUri
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun SupportedUrisScreen(
    onNavigateToMainScreen: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    var supportedUris by remember { mutableStateOf(viewModel.getSupportedUris(context.packageManager)) }
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        supportedUris = viewModel.getSupportedUris(context.packageManager)
    }

    SupportedUrisScreen(
        supportedUris = supportedUris,
        onNavigateToMainScreen = onNavigateToMainScreen,
        onShowOpenByDefaultSettings = { viewModel.showOpenByDefaultSettings(context, settingsLauncher) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportedUrisScreen(
    supportedUris: List<Pair<Int, List<SupportedUri>>>,
    onNavigateToMainScreen: () -> Unit = {},
    onShowOpenByDefaultSettings: () -> Unit = {},
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
                .fillMaxWidth(),
        ) {
            Button(
                { onShowOpenByDefaultSettings() },
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = Spacing.tiny, bottom = Spacing.medium),
            ) {
                Text(stringResource(R.string.supported_uris_settings_button))
            }
            Box {
                FilterChip(selected = true, onClick = {
                    // TODO
                }, label = {
                    Text("All links")
                }, trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown, null
                    )
                })
                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("All links") },
                        modifier = Modifier.testTag("geoShareMainMenuUserPreferences"),
                        onClick = {
                            filterExpanded = false
                            // TODO
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Links that open by default in Geo Share") },
                        modifier = Modifier.testTag("geoShareMainMenuUserPreferences"),
                        onClick = {
                            filterExpanded = false
                            // TODO
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Links that don't open by default in Geo Share") },
                        modifier = Modifier.testTag("geoShareMainMenuUserPreferences"),
                        onClick = {
                            filterExpanded = false
                            // TODO
                        },
                    )
                }
            }
            LazyColumn(
                Modifier.padding(top = Spacing.tiny),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                supportedUris.forEach { (nameResId, supportedUris) ->
                    stickyHeader {
                        Text(
                            stringResource(nameResId),
                            Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    items(
                        supportedUris.size,
                        key = { index -> supportedUris[index].uriString },
                    ) { index ->
                        val supportedUri = supportedUris[index]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                                Text(
                                    supportedUri.uriString,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    if (supportedUri.defaultHandlerEnabled) {
                                        stringResource(R.string.supported_uris_enabled, appName)
                                    } else {
                                        stringResource(R.string.supported_uris_disabled, appName)
                                    },
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

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        SupportedUrisScreen(
            supportedUris = listOf(
                R.string.converter_google_maps_name to listOf(
                    SupportedUri("https://maps.app.goo.gl", true),
                    SupportedUri("https://app.goo.gl/maps", false),
                    SupportedUri("https://maps.google.com", true),
                    SupportedUri("https://goo.gl/maps", true),
                    SupportedUri("https://google.com/maps", false),
                    SupportedUri("https://www.google.com/maps", true),
                    SupportedUri("https://g.co/kgs", false),
                ), R.string.converter_apple_maps_name to listOf(
                    SupportedUri("https://maps.apple", true),
                    SupportedUri("https://maps.apple.com", false),
                )
            ),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        SupportedUrisScreen(
            supportedUris = listOf(
                R.string.converter_google_maps_name to listOf(
                    SupportedUri("https://maps.app.goo.gl", true),
                    SupportedUri("https://app.goo.gl/maps", false),
                    SupportedUri("https://maps.google.com", true),
                    SupportedUri("https://goo.gl/maps", true),
                    SupportedUri("https://google.com/maps", false),
                    SupportedUri("https://www.google.com/maps", true),
                    SupportedUri("https://g.co/kgs", false),
                ), R.string.converter_apple_maps_name to listOf(
                    SupportedUri("https://maps.apple", true),
                    SupportedUri("https://maps.apple.com", false),
                )
            ),
        )
    }
}

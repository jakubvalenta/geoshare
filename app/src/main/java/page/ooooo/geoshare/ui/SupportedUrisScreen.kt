package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // Do nothing.
    }

    SupportedUrisScreen(
        getSupportedUris = { viewModel.getSupportedUris(context.packageManager) },
        onNavigateToMainScreen = onNavigateToMainScreen,
        onShowOpenByDefaultSettings = { viewModel.showOpenByDefaultSettings(context, settingsLauncher) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportedUrisScreen(
    getSupportedUris: () -> List<Pair<Int, List<SupportedUri>>>,
    onNavigateToMainScreen: () -> Unit = {},
    onShowOpenByDefaultSettings: () -> Unit = {},
) {
    val appName = stringResource(R.string.app_name)

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
                .fillMaxWidth()
        ) {
            LazyColumn(
                Modifier.padding(top = Spacing.tiny),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                getSupportedUris().forEach { (nameResId, supportedUris) ->
                    stickyHeader {
                        Text(stringResource(nameResId), style = MaterialTheme.typography.bodyLarge)
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
                                    style = MaterialTheme.typography.bodyMedium,
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
                            VerticalDivider(
                                Modifier
                                    .height(28.dp)
                                    .padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.outline,
                            )
                            IconButton({ onShowOpenByDefaultSettings() }) {
                                Icon(
                                    Icons.Filled.Settings,
                                    stringResource(R.string.intro_open_by_default_app_button, appName)
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
            getSupportedUris = {
                listOf(
                    R.string.converter_google_maps_name to listOf(
                        SupportedUri("https://maps.app.goo.gl", true),
                        SupportedUri("https://app.goo.gl/maps", false),
                        SupportedUri("https://maps.google.com", true),
                        SupportedUri("https://goo.gl/maps", true),
                        SupportedUri("https://google.com/maps", false),
                        SupportedUri("https://www.google.com/maps", true),
                        SupportedUri("https://g.co/kgs", false),
                    ),
                    R.string.converter_apple_maps_name to listOf(
                        SupportedUri("https://maps.apple", true),
                        SupportedUri("https://maps.apple.com", false),
                    )
                )
            },
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        SupportedUrisScreen(
            getSupportedUris = {
                listOf(
                    R.string.converter_google_maps_name to listOf(
                        SupportedUri("https://maps.app.goo.gl", true),
                        SupportedUri("https://app.goo.gl/maps", false),
                        SupportedUri("https://maps.google.com", true),
                        SupportedUri("https://goo.gl/maps", true),
                        SupportedUri("https://google.com/maps", false),
                        SupportedUri("https://www.google.com/maps", true),
                        SupportedUri("https://g.co/kgs", false),
                    ),
                    R.string.converter_apple_maps_name to listOf(
                        SupportedUri("https://maps.apple", true),
                        SupportedUri("https://maps.apple.com", false),
                    )
                )
            },
        )
    }
}

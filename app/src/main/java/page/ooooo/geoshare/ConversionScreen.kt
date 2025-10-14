package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import page.ooooo.geoshare.components.ConfirmationScaffold
import page.ooooo.geoshare.components.PermissionDialog
import page.ooooo.geoshare.components.ResultErrorCard
import page.ooooo.geoshare.components.ResultSuccessCard
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.State
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ConversionScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    viewModel: ConversionViewModel,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    val loadingIndicatorTitleResId by viewModel.loadingIndicatorTitleResId.collectAsStateWithLifecycle()
    val lastInputShown by viewModel.lastInputShown.collectAsState()
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.save(context, it)
    }

    ConversionScreen(
        currentState = currentState,
        lastInputShown = lastInputShown,
        loadingIndicatorTitleResId = loadingIndicatorTitleResId,
        queryGeoUriApps = { viewModel.intentTools.queryGeoUriApps(context.packageManager) },
        onBack = onBack,
        onCancel = {
            viewModel.cancel()
            onCancel()
        },
        onCopy = { text -> viewModel.copy(context, clipboard, text) },
        onDeny = { doNotAsk -> viewModel.deny(doNotAsk) },
        onGrant = { doNotAsk -> viewModel.grant(doNotAsk) },
        onNavigateToAboutScreen = onNavigateToAboutScreen,
        onNavigateToFaqScreen = onNavigateToFaqScreen,
        onNavigateToIntroScreen = onNavigateToIntroScreen,
        onNavigateToUrlConvertersScreen = onNavigateToUrlConvertersScreen,
        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
        onOpenApp = { packageName, uriString -> viewModel.intentTools.openApp(context, packageName, uriString) },
        onOpenChooser = { uriString -> viewModel.intentTools.openChooser(context, uriString) },
        onRetry = { newUriString ->
            viewModel.updateInput(newUriString)
            viewModel.start()
        },
        onSave = { viewModel.launchSave(context, saveLauncher) },
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConversionScreen(
    currentState: State,
    lastInputShown: Boolean,
    @StringRes loadingIndicatorTitleResId: Int?,
    queryGeoUriApps: () -> List<IntentTools.App>,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onCopy: (text: String) -> Unit,
    onDeny: (doNotAsk: Boolean) -> Unit,
    onGrant: (doNotAsk: Boolean) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onOpenApp: (packageName: String, uriString: String) -> Unit,
    onOpenChooser: (uriString: String) -> Unit,
    onRetry: (newUriString: String) -> Unit,
    onSave: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val coroutineScope = rememberCoroutineScope()
    val (retryLoadingIndicatorVisible, setRetryLoadingIndicator) = remember { mutableStateOf(false) }

    ConfirmationScaffold(
        title = when {
            loadingIndicatorTitleResId != null -> {
                stringResource(loadingIndicatorTitleResId)
            }

            currentState is HasError -> {
                stringResource(R.string.conversion_error_title)
            }

            currentState is HasResult -> {
                stringResource(R.string.conversion_succeeded_title)
            }

            else -> null
        },
        navigationIcon = {
            IconButton(onClick = { onBack() }) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    stringResource(R.string.nav_back_content_description)
                )
            }
        },
        startButton = when {
            loadingIndicatorTitleResId != null -> {
                {
                    OutlinedButton(onCancel) {
                        Text(stringResource(R.string.conversion_loading_indicator_cancel))
                    }
                }
            }

            currentState is HasError -> {
                {
                    TextButton({ onCopy(currentState.inputUriString) }) {
                        Text(stringResource(R.string.conversion_succeeded_skip))
                    }
                }
            }

            currentState is HasResult -> {
                {
                    TextButton({ onCopy(currentState.inputUriString) }) {
                        Text(stringResource(R.string.conversion_succeeded_skip))
                    }
                }
            }

            else -> null
        },
        endButton = when (currentState) {
            is HasError, is HasResult -> {
                {
                    OutlinedButton(onBack, Modifier.testTag("geoShareConversionDoneButton")) {
                        Text(stringResource(R.string.conversion_done))
                    }
                }
            }

            else -> null
        },
        lastInputShown = lastInputShown,
        onNavigateToAboutScreen = onNavigateToAboutScreen,
        onNavigateToFaqScreen = onNavigateToFaqScreen,
        onNavigateToIntroScreen = onNavigateToIntroScreen,
        onNavigateToUrlConvertersScreen = onNavigateToUrlConvertersScreen,
        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
    ) {
        when {
            (loadingIndicatorTitleResId != null) -> {
                LoadingIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
                if (currentState is HasLoadingIndicator) {
                    currentState.loadingIndicatorDescription()?.let { text ->
                        Text(
                            text,
                            Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            currentState is RequestedUnshortenPermission -> {
                PermissionDialog(
                    title = stringResource(currentState.permissionTitleResId),
                    confirmText = stringResource(R.string.conversion_permission_common_grant),
                    dismissText = stringResource(R.string.conversion_permission_common_deny),
                    onConfirmation = onGrant,
                    onDismissRequest = onDeny,
                    modifier = Modifier
                        .semantics { testTagsAsResourceId = true }
                        .testTag("geoShareUnshortenPermissionDialog"),
                ) {
                    Text(
                        AnnotatedString.fromHtml(
                            stringResource(
                                R.string.conversion_permission_common_text,
                                truncateMiddle(currentState.uri.toString()),
                                appName,
                            )
                        ),
                        style = TextStyle(lineBreak = LineBreak.Paragraph),
                    )
                }
            }

            currentState is RequestedParseHtmlPermission -> {
                PermissionDialog(
                    title = stringResource(currentState.permissionTitleResId),
                    confirmText = stringResource(R.string.conversion_permission_common_grant),
                    dismissText = stringResource(R.string.conversion_permission_common_deny),
                    onConfirmation = onGrant,
                    onDismissRequest = onDeny,
                    modifier = Modifier
                        .semantics { testTagsAsResourceId = true }
                        .testTag("geoShareParseHtmlPermissionDialog"),
                ) {
                    Text(
                        AnnotatedString.fromHtml(
                            stringResource(
                                R.string.conversion_permission_common_text,
                                truncateMiddle(currentState.uri.toString()),
                                appName,
                            )
                        ),
                        style = TextStyle(lineBreak = LineBreak.Paragraph),
                    )
                }
            }

            currentState is HasError -> {
                ResultErrorCard(
                    currentState.errorMessageResId,
                    currentState.inputUriString,
                    retryLoadingIndicatorVisible,
                    onNavigateToUrlConvertersScreen = onNavigateToUrlConvertersScreen,
                    onRetry = {
                        coroutineScope.launch {
                            // Show a loading indicator for a while to indicate that conversion is being retried.
                            setRetryLoadingIndicator(true)
                            delay(1000)
                            setRetryLoadingIndicator(false)
                            onRetry(currentState.inputUriString)
                        }
                    },
                )
            }

            currentState is HasResult -> {
                ResultSuccessCard(
                    queryGeoUriApps(),
                    currentState.position,
                    onCopy = onCopy,
                    onOpenApp = { packageName ->
                        onOpenApp(packageName, currentState.position.toGeoUriString())
                    },
                    onShare = { onOpenChooser(currentState.position.toGeoUriString()) },
                    onSave = onSave,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionSucceeded(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position("50.123456", "11.123456"),
            ),
            lastInputShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionSucceeded(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position("50.123456", "11.123456"),
            ),
            lastInputShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionPreview() {
    AppTheme {
        ConversionScreen(
            currentState = RequestedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsUrlConverter(),
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
            ),
            lastInputShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPermissionPreview() {
    AppTheme {
        ConversionScreen(
            currentState = RequestedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsUrlConverter(),
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
            ),
            lastInputShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionFailed(
                R.string.conversion_failed_parse_url_error,
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            lastInputShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionFailed(
                R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            lastInputShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = GrantedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsUrlConverter(),
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            lastInputShown = true,
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = GrantedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsUrlConverter(),
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            lastInputShown = true,
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialPreview() {
    AppTheme {
        ConversionScreen(
            currentState = Initial(),
            lastInputShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkInitialPreview() {
    AppTheme {
        ConversionScreen(
            currentState = Initial(),
            lastInputShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = {},
        )
    }
}

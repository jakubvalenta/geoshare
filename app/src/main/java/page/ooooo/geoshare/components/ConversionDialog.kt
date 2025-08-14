package page.ooooo.geoshare.components

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import androidx.core.net.toUri
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing
import java.net.URL

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConversionDialog(
    currentState: State,
    loadingIndicatorTitleResId: Int?,
    queryGeoUriApps: (Context) -> List<Pair<String, Int>>,
    onGrant: (Boolean) -> Unit,
    onDeny: (Boolean) -> Unit,
    onCopy: () -> Unit,
    onShare: (ManagedActivityResultLauncher<Intent, ActivityResult>) -> Unit,
    onSkip: (ManagedActivityResultLauncher<Intent, ActivityResult>) -> Unit,
    onCancel: () -> Unit,
    onFinish: () -> Unit = {},
) {
    val appName = stringResource(R.string.app_name)
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        onGrant(false)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (loadingIndicatorTitleResId != null) {
                        Text(stringResource(loadingIndicatorTitleResId))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(horizontal = Spacing.windowPadding)
        ) {
            when (currentState) {
                is HasResult -> {
                    ResultCard(
                        queryGeoUriApps(context),
                        currentState.geoUri,
                        onCopy,
                        onShare = { onShare(settingsLauncher) },
                        onSkip = { onSkip(settingsLauncher) },
                    )
                }
            }

            when (currentState) {
                is HasError -> {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = OutlinedTextFieldDefaults.shape,
                        colors = CardDefaults.cardColors(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Row(Modifier.padding(Spacing.small)) {
                            SelectionContainer {
                                Text(
                                    stringResource(currentState.errorMessageResId),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            }

            when (currentState) {
                is RequestedUnshortenPermission -> {
                    PermissionDialog(
                        title = stringResource(currentState.urlConverter.permissionTitleResId),
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
                                    currentState.url.toString(),
                                    appName,
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Paragraph),
                        )
                    }
                }

                is RequestedParseHtmlPermission -> {
                    PermissionDialog(
                        title = stringResource(currentState.urlConverter.permissionTitleResId),
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
                                    currentState.url.toString(),
                                    appName,
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Paragraph),
                        )
                    }
                }

                is RequestedParseHtmlToGetCoordsPermission -> {
                    PermissionDialog(
                        title = stringResource(currentState.urlConverter.permissionTitleResId),
                        confirmText = stringResource(R.string.conversion_permission_common_grant),
                        dismissText = stringResource(R.string.conversion_permission_parse_html_to_get_coords_deny),
                        onConfirmation = onGrant,
                        onDismissRequest = onDeny,
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareParseHtmlToGetCoordsPermissionDialog")) {
                        Text(
                            AnnotatedString.fromHtml(
                                stringResource(
                                    R.string.conversion_permission_parse_html_to_get_coords_text,
                                    truncateMiddle(currentState.url.toString()),
                                    appName,
                                    currentState.urlConverter.name,
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Paragraph),
                        )
                    }
                }

                is RequestedSharePermission -> {
                    ConfirmationDialog(
                        title = stringResource(R.string.conversion_permission_xiaomi_title),
                        confirmText = stringResource(R.string.conversion_permission_xiaomi_grant),
                        dismissText = stringResource(R.string.conversion_permission_xiaomi_deny),
                        onConfirmation = { onGrant(false) },
                        onDismissRequest = { onGrant(false) },
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareXiaomiPermissionDialog"),
                    ) {
                        Text(
                            AnnotatedString.fromHtml(
                                stringResource(
                                    R.string.conversion_permission_xiaomi_text, appName
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Paragraph),
                        )
                    }
                }
            }

            when (currentState) {
                is ConversionFailed,
                is SharingSucceeded,
                is SharingFailed,
                    -> {
                    onFinish()
                }
            }

            if (loadingIndicatorTitleResId != null) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    LoadingIndicator(Modifier.size(120.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onCancel) {
                        Text(stringResource(R.string.conversion_loading_indicator_cancel))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        ConversionDialog(
            currentState = ConversionSucceeded(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA".toUri(),
                "geo:50.123456,11.123456",
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        ConversionDialog(
            currentState = ConversionSucceeded(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA".toUri(),
                "geo:50.123456,11.123456",
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionPreview() {
    AppTheme {
        ConversionDialog(
            currentState = RequestedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                    XiaomiTools(),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA".toUri(),
                GoogleMapsUrlConverter(),
                URL("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPermissionPreview() {
    AppTheme {
        ConversionDialog(
            currentState = RequestedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                    XiaomiTools(),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA".toUri(),
                GoogleMapsUrlConverter(),
                URL("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        ConversionDialog(
            currentState = ConversionFailed(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA".toUri(),
                R.string.conversion_failed_parse_url_error,
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        ConversionDialog(
            currentState = ConversionFailed(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA".toUri(),
                R.string.conversion_failed_parse_url_error,
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        ConversionDialog(
            currentState = Initial(),
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        ConversionDialog(
            currentState = Initial(),
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

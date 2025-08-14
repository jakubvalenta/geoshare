package page.ooooo.geoshare

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.ConfirmationLayout
import page.ooooo.geoshare.components.PermissionDialog
import page.ooooo.geoshare.components.ResultCard
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing
import java.net.URL

@Composable
fun ConversionScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    val loadingIndicatorTitleResId by viewModel.loadingIndicatorTitleResId.collectAsStateWithLifecycle()

    ConversionScreen(
        currentState,
        loadingIndicatorTitleResId,
        queryGeoUriApps = { context -> viewModel.queryGeoUriApps(context.packageManager) },
        onBack = onBack,
        onGrant = { doNotAsk -> viewModel.grant(doNotAsk) },
        onDeny = { doNotAsk -> viewModel.deny(doNotAsk) },
        onCopy = {
            viewModel.copy(context, clipboard)
            onFinish()
        },
        onShare = { packageName ->
            viewModel.share(context, packageName)
            onFinish()
        },
        onSkip = {
            viewModel.skip(context)
            onFinish()
        },
        onCancel = {
            viewModel.cancel()
            onFinish()
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConversionScreen(
    currentState: State,
    @StringRes loadingIndicatorTitleResId: Int?,
    queryGeoUriApps: (Context) -> List<ConversionViewModel.App>,
    onBack: () -> Unit,
    onGrant: (Boolean) -> Unit,
    onDeny: (Boolean) -> Unit,
    onCopy: () -> Unit,
    onShare: (String) -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back") // TODO
                    }
                },
                title = {
                    Text(appName)
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = Spacing.small)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            when (currentState) {
                is HasResult -> {
                    ConfirmationLayout(
                        startButton = {
                            OutlinedButton(onSkip) {
                                Text(stringResource(R.string.main_result_skip))
                            }
                        },
                        endButton = {
                            Button(onBack) {
                                Text(stringResource(R.string.conversion_done))
                            }
                        },
                    ) {
                        ResultCard(
                            queryGeoUriApps(context),
                            currentState.position,
                            onCopy,
                            onShare,
                        )
                    }
                }
            }

            when (currentState) {
                is HasError -> {
                    ConfirmationLayout(
                        endButton = {
                            Button(onBack) {
                                Text(stringResource(R.string.conversion_done))
                            }
                        },
                    ) {
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
            }

            if (loadingIndicatorTitleResId != null) {
                ConfirmationLayout(
                    startButton = {
                        OutlinedButton(onCancel) {
                            Text(stringResource(R.string.conversion_loading_indicator_cancel))
                        }
                    },
                ) {
                    LoadingIndicator(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(120.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(loadingIndicatorTitleResId),
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
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
                Position(Position.Coords("50.123456", "11.123456")),
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
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
        ConversionScreen(
            currentState = ConversionSucceeded(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position(Position.Coords("50.123456", "11.123456")),
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
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
        ConversionScreen(
            currentState = RequestedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                    XiaomiTools(),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsUrlConverter(),
                URL("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
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
        ConversionScreen(
            currentState = RequestedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                    XiaomiTools(),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsUrlConverter(),
                URL("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
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
        ConversionScreen(
            currentState = ConversionFailed(
                R.string.conversion_failed_parse_url_error,
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
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
        ConversionScreen(
            currentState = ConversionFailed(
                R.string.conversion_failed_parse_url_error,
            ),
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
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
        ConversionScreen(
            currentState = Initial(),
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onBack = {},
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
        ConversionScreen(
            currentState = Initial(),
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onGrant = {},
            onDeny = {},
            onCopy = {},
            onShare = {},
            onSkip = {},
            onCancel = {},
        )
    }
}

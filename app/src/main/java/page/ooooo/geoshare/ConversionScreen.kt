package page.ooooo.geoshare

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.ConfirmationScaffold
import page.ooooo.geoshare.components.PermissionDialog
import page.ooooo.geoshare.components.ResultCard
import page.ooooo.geoshare.components.ResultErrorCard
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.ui.theme.AppTheme
import androidx.core.net.toUri

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
        onCopy = { text ->
            viewModel.copy(context, clipboard, text)
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
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val context = LocalContext.current

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

            currentState is HasResult -> {
                {
                    TextButton({ onSkip() }) {
                        Text(stringResource(R.string.conversion_succeeded_skip))
                    }
                }
            }

            else -> null
        },
        endButton = when (currentState) {
            is HasError, is HasResult -> {
                {
                    Button(onBack, Modifier.testTag("geoShareConversionDoneButton")) {
                        Text(stringResource(R.string.conversion_done))
                    }
                }
            }

            else -> null
        }
    ) {
        when {
            (loadingIndicatorTitleResId != null) -> {
                LoadingIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
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
                                currentState.uri.toString(),
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
                                currentState.uri.toString(),
                                appName,
                            )
                        ),
                        style = TextStyle(lineBreak = LineBreak.Paragraph),
                    )
                }
            }

            currentState is RequestedParseHtmlToGetCoordsPermission -> {
                PermissionDialog(
                    title = stringResource(currentState.permissionTitleResId),
                    confirmText = stringResource(R.string.conversion_permission_common_grant),
                    dismissText = stringResource(R.string.conversion_permission_parse_html_to_get_coords_deny),
                    onConfirmation = onGrant,
                    onDismissRequest = onDeny,
                    modifier = Modifier
                        .semantics { testTagsAsResourceId = true }
                        .testTag("geoShareParseHtmlToGetCoordsPermissionDialog")
                ) {
                    Text(
                        AnnotatedString.fromHtml(
                            stringResource(
                                R.string.conversion_permission_parse_html_to_get_coords_text,
                                truncateMiddle(currentState.uri.toString()),
                                appName,
                            )
                        ),
                        style = TextStyle(lineBreak = LineBreak.Paragraph),
                    )
                }
            }

            currentState is HasError -> {
                ResultErrorCard(currentState.errorMessageResId)
            }

            currentState is HasResult -> {
                ResultCard(
                    queryGeoUriApps(context),
                    currentState.position,
                    onCopy,
                    onShare,
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
                Position("50.123456", "11.123456"),
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
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsUrlConverter(),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA".toUri(),
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
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsUrlConverter(),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA".toUri(),
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

@Preview(showBackground = true)
@Composable
private fun InitialPreview() {
    AppTheme {
        ConversionScreen(
            currentState = Initial(),
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
private fun DarkInitialPreview() {
    AppTheme {
        ConversionScreen(
            currentState = Initial(),
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

package page.ooooo.geoshare.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.MainScreen
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConversionDialog(onFinish: () -> Unit = {}, viewModel: ConversionViewModel = hiltViewModel()) {
    val appName = stringResource(R.string.app_name)
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.grant(doNotAsk = false)
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(
                context,
                it.resId,
                if (it.type == Message.Type.SUCCESS) {
                    Toast.LENGTH_SHORT
                } else {
                    Toast.LENGTH_LONG
                },
            ).show()
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    viewModel.loadingIndicatorTitleResId?.let { titleResId ->
                        Text(stringResource(titleResId))
                    }
                })
        }) { innerPadding ->
        Column(
            modifier = Modifier.Companion
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(horizontal = Spacing.windowPadding)
        ) {
            viewModel.resultGeoUri.takeIf { it.isNotEmpty() }?.let { resultGeoUri ->
                val geoUriApps = viewModel.queryGeoUriApps(context)
                ResultCard(
                    geoUriApps,
                    resultGeoUri,
                    viewModel.resultErrorMessageResId,
                    onCopy = {
                        viewModel.copy(clipboard)
                    },
                    onShare = {
                        viewModel.share(
                            context, ManagedActivityResultLauncherWrapper(settingsLauncher)
                        )
                    },
                    onSkip = {
                        viewModel.skip(
                            context, ManagedActivityResultLauncherWrapper(settingsLauncher),
                        )
                    },
                )
            }

            when (currentState) {
                is RequestedUnshortenPermission -> (currentState as RequestedUnshortenPermission).let { currentState ->
                    PermissionDialog(
                        title = stringResource(currentState.urlConverter.permissionTitleResId),
                        confirmText = stringResource(R.string.conversion_permission_common_grant),
                        dismissText = stringResource(R.string.conversion_permission_common_deny),
                        onConfirmation = { viewModel.grant(it) },
                        onDismissRequest = { viewModel.deny(it) },
                        modifier = Modifier.Companion
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareUnshortenPermissionDialog"),
                    ) {
                        Text(
                            AnnotatedString.Companion.fromHtml(
                                stringResource(
                                    R.string.conversion_permission_common_text,
                                    currentState.url.toString(),
                                    appName,
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Companion.Paragraph),
                        )
                    }
                }

                is RequestedParseHtmlPermission -> (currentState as RequestedParseHtmlPermission).let { currentState ->
                    PermissionDialog(
                        title = stringResource(currentState.urlConverter.permissionTitleResId),
                        confirmText = stringResource(R.string.conversion_permission_common_grant),
                        dismissText = stringResource(R.string.conversion_permission_common_deny),
                        onConfirmation = { viewModel.grant(it) },
                        onDismissRequest = { viewModel.deny(it) },
                        modifier = Modifier.Companion
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareParseHtmlPermissionDialog"),
                    ) {
                        Text(
                            AnnotatedString.Companion.fromHtml(
                                stringResource(
                                    R.string.conversion_permission_common_text,
                                    currentState.url.toString(),
                                    appName,
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Companion.Paragraph),
                        )
                    }
                }

                is RequestedParseHtmlToGetCoordsPermission -> (currentState as RequestedParseHtmlToGetCoordsPermission).let { currentState ->
                    PermissionDialog(
                        title = stringResource(currentState.urlConverter.permissionTitleResId),
                        confirmText = stringResource(R.string.conversion_permission_common_grant),
                        dismissText = stringResource(R.string.conversion_permission_parse_html_to_get_coords_deny),
                        onConfirmation = { viewModel.grant(it) },
                        onDismissRequest = { viewModel.deny(it) },
                        modifier = Modifier.Companion
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareParseHtmlToGetCoordsPermissionDialog")) {
                        Text(
                            AnnotatedString.Companion.fromHtml(
                                stringResource(
                                    R.string.conversion_permission_parse_html_to_get_coords_text,
                                    truncateMiddle(currentState.url.toString()),
                                    appName,
                                    currentState.urlConverter.name,
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Companion.Paragraph),
                        )
                    }
                }

                is RequestedSharePermission -> {
                    ConfirmationDialog(
                        title = stringResource(R.string.conversion_permission_xiaomi_title),
                        confirmText = stringResource(R.string.conversion_permission_xiaomi_grant),
                        dismissText = stringResource(R.string.conversion_permission_xiaomi_deny),
                        onConfirmation = { viewModel.grant(doNotAsk = false) },
                        onDismissRequest = { viewModel.deny(doNotAsk = false) },
                        modifier = Modifier.Companion
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareXiaomiPermissionDialog"),
                    ) {
                        Text(
                            AnnotatedString.Companion.fromHtml(
                                stringResource(
                                    R.string.conversion_permission_xiaomi_text, appName
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Companion.Paragraph),
                        )
                    }
                }

                is ConversionFailed -> {
                    onFinish()
                }

                is SharingSucceeded -> {
                    onFinish()
                }

                is SharingFailed -> {
                    onFinish()
                }
            }

            if (viewModel.loadingIndicatorTitleResId != null) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    LoadingIndicator(Modifier.size(120.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button({ viewModel.cancel() }) {
                        Text(stringResource(R.string.conversion_loading_indicator_cancel))
                    }
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        ConversionDialog(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultGeoUri" to "geo:50.123456,11.123456",
                    )
                ),
            )
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        ConversionDialog(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultGeoUri" to "geo:50.123456,11.123456",
                    )
                ),
            )
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun DonePreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultGeoUri" to "geo:50.123456,11.123456",
                    )
                ),
            )
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDonePreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultGeoUri" to "geo:50.123456,11.123456",
                    )
                ),
            )
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        ConversionDialog(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultErrorMessage" to R.string.conversion_failed_parse_url_error
                    )
                ),
            )
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        ConversionDialog(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultErrorMessage" to R.string.conversion_failed_parse_url_error
                    )
                ),
            )
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        ConversionDialog(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "loadingIndicatorTitleResId" to R.string.converter_google_maps_loading_indicator_title,
                    )
                ),
            )
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        ConversionDialog(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "loadingIndicatorTitleResId" to R.string.converter_google_maps_loading_indicator_title,
                    )
                ),
            )
        )
    }
}

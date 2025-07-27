package page.ooooo.geoshare

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.LineBreak
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.LoadingIndicatorDialog
import page.ooooo.geoshare.components.PermissionDialog
import page.ooooo.geoshare.lib.RequestedParseHtmlPermission
import page.ooooo.geoshare.lib.RequestedParseHtmlToGetCoordsPermission
import page.ooooo.geoshare.lib.RequestedUnshortenPermission
import page.ooooo.geoshare.lib.truncateMiddle

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConversionScreen(loadingIndicatorEnabled: Boolean = true, viewModel: ConversionViewModel = hiltViewModel()) {
    val appName = stringResource(R.string.app_name)
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()

    when (currentState) {
        is RequestedUnshortenPermission -> (currentState as RequestedUnshortenPermission).let { currentState ->
            PermissionDialog(
                title = stringResource(currentState.urlConverter.permissionTitleResId),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = { viewModel.grant(it) },
                onDismissRequest = { viewModel.deny(it) },
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

        is RequestedParseHtmlPermission -> (currentState as RequestedParseHtmlPermission).let { currentState ->
            PermissionDialog(
                title = stringResource(currentState.urlConverter.permissionTitleResId),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = { viewModel.grant(it) },
                onDismissRequest = { viewModel.deny(it) },
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

        is RequestedParseHtmlToGetCoordsPermission -> (currentState as RequestedParseHtmlToGetCoordsPermission).let { currentState ->
            PermissionDialog(
                title = stringResource(currentState.urlConverter.permissionTitleResId),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_parse_html_to_get_coords_deny),
                onConfirmation = { viewModel.grant(it) },
                onDismissRequest = { viewModel.deny(it) },
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

    if (loadingIndicatorEnabled) {
        viewModel.loadingIndicatorTitleResId?.let { loadingIndicatorTitleResId ->
            LoadingIndicatorDialog(
                titleResId = loadingIndicatorTitleResId,
                onDismissRequest = { viewModel.cancel() },
            )
        }
    }
}

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
import page.ooooo.geoshare.components.ConfirmationDialog
import page.ooooo.geoshare.lib.RequestedSharePermission

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShareScreen(loadingIndicatorEnabled: Boolean = true, viewModel: ConversionViewModel = hiltViewModel()) {
    val appName = stringResource(R.string.app_name)
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()

    ConversionScreen(loadingIndicatorEnabled, viewModel)

    when (currentState) {
        is RequestedSharePermission -> {
            ConfirmationDialog(
                title = stringResource(R.string.conversion_permission_xiaomi_title),
                confirmText = stringResource(R.string.conversion_permission_xiaomi_grant),
                dismissText = stringResource(R.string.conversion_permission_xiaomi_deny),
                onConfirmation = { viewModel.grant(doNotAsk = false) },
                onDismissRequest = { viewModel.deny(doNotAsk = false) },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .testTag("geoShareXiaomiPermissionDialog"),
            ) {
                Text(
                    AnnotatedString.fromHtml(
                        stringResource(
                            R.string.conversion_permission_xiaomi_text,
                            appName
                        )
                    ),
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }
        }
    }
}

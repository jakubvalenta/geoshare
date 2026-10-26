package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.string.FormatArg
import page.ooooo.geoshare.ui.string.annotatedStringResource
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.theme.tertiaryLinkStyles

@Composable
fun FaqPrivacyText(onOpenUriInDefaultApp: (uriString: String) -> Unit) {
    val appName = stringResource(R.string.app_name)
    val appServerName = stringResource(R.string.app_server_name)
    val appServerUrl = stringResource(R.string.app_server_url)

    ParagraphText(
        stringResource(R.string.faq_privacy_text, appName)
    )
    ParagraphText(
        annotatedStringResource(
            R.string.faq_privacy_text_server,
            tertiaryLinkStyles(),
            FormatArg.Text(appName),
            FormatArg.Text(appServerName),
            FormatArg.Link(appServerName, onClick = { onOpenUriInDefaultApp(appServerUrl) }),
        )
    )
    ParagraphText(
        stringResource(R.string.faq_privacy_text_html, appName)
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)) {
                FaqPrivacyText(onOpenUriInDefaultApp = {})
            }
        }
    }
}

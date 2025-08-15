package page.ooooo.geoshare.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun ResultErrorCard(
    @StringRes errorMessageResId: Int,
) {
    val appName = stringResource(R.string.app_name)

    Card(
        Modifier
            .fillMaxWidth()
            .testTag("geoShareConversionError"),
        shape = OutlinedTextFieldDefaults.shape,
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Row(Modifier.padding(Spacing.small)) {
            SelectionContainer {
                Text(
                    stringResource(errorMessageResId),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
    InfoCard(
        AnnotatedString.fromHtml(
            stringResource(R.string.main_info_box, appName),
            linkStyles = TextLinkStyles(
                SpanStyle(textDecoration = TextDecoration.Underline)
            ),
        ),
        Modifier.padding(top = Spacing.medium),
    )
}

package page.ooooo.geoshare.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun ParagraphHtml(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        AnnotatedString.fromHtml(
            text,
            linkStyles = TextLinkStyles(
                SpanStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline
                )
            )
        ),
        modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            lineBreak = LineBreak.Paragraph,
        ),
    )
}

package page.ooooo.geoshare.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak

@Composable
fun ParagraphText(
    text: String,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = null,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(
        text,
        modifier = modifier,
        fontStyle = fontStyle,
        style = style.copy(
            lineBreak = LineBreak.Paragraph,
            hyphens = Hyphens.Auto,
        ),
    )
}

@Composable
fun ParagraphText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = null,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(
        text,
        modifier = modifier,
        fontStyle = fontStyle,
        style = style.copy(
            lineBreak = LineBreak.Paragraph,
            hyphens = Hyphens.Auto,
        ),
    )
}

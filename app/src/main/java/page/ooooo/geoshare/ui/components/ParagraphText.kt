package page.ooooo.geoshare.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak

@Composable
fun ParagraphText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    fontStyle: FontStyle? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text,
        modifier = modifier,
        color = color,
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
    color: Color = LocalContentColor.current,
    fontStyle: FontStyle? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text,
        modifier = modifier,
        color = color,
        fontStyle = fontStyle,
        style = style.copy(
            lineBreak = LineBreak.Paragraph,
            hyphens = Hyphens.Auto,
        ),
    )
}

@Composable
fun ParagraphHtml(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    fontStyle: FontStyle? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    ParagraphText(
        AnnotatedString.fromHtml(text, linkStyles = AnnotatedString.linkStyles),
        modifier,
        color = color,
        fontStyle = fontStyle,
        style = style,
    )
}

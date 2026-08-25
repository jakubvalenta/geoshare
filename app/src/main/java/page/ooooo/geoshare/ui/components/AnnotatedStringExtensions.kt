package page.ooooo.geoshare.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import page.ooooo.geoshare.lib.Segment
import page.ooooo.geoshare.lib.parseFormatString

sealed interface FormatArg {
    data class Text(val text: String, val style: SpanStyle? = null) : FormatArg
    data class Link(val text: String, val onClick: () -> Unit) : FormatArg
}

@Composable
fun annotatedStringResource(@StringRes id: Int, vararg formatArgs: FormatArg): AnnotatedString = buildAnnotatedString {
    parseFormatString(LocalResources.current.getText(id) as String).forEach { segment ->
        when (segment) {
            is Segment.Text -> {
                append(segment.text)
            }

            is Segment.Arg -> {
                val argument = formatArgs.getOrNull(segment.index) ?: return@forEach
                when (argument) {
                    is FormatArg.Text ->
                        if (argument.style != null) {
                            withStyle(argument.style) {
                                append(argument.text)
                            }
                        } else {
                            append(argument.text)
                        }

                    is FormatArg.Link ->
                        ClickableLink(argument.text, onClick = argument.onClick)
                }
            }
        }
    }
}

@Composable
fun AnnotatedString.Builder.ClickableLink(
    text: String,
    styles: TextLinkStyles = AnnotatedString.DefaultLinkStyles,
    onClick: () -> Unit,
) {
    withLink(
        LinkAnnotation.Clickable(tag = "link", styles = styles) {
            onClick()
        }
    ) {
        append(text)
    }
}

val AnnotatedString.Companion.DefaultLinkStyles
    @Composable get() = TextLinkStyles(
        SpanStyle(
            color = MaterialTheme.colorScheme.tertiary,
            textDecoration = TextDecoration.Underline,
        )
    )

val AnnotatedString.Companion.UnderlinedLinkStyles
    @Composable get() = TextLinkStyles(
        SpanStyle(
            textDecoration = TextDecoration.Underline,
        )
    )

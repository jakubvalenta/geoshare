package page.ooooo.geoshare.ui.string

import androidx.annotation.StringRes
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

sealed interface FormatArg {
    data class Text(val text: String, val style: SpanStyle? = null) : FormatArg
    data class InlineContent(val id: String) : FormatArg
    data class Link(val text: String, val onClick: () -> Unit) : FormatArg
}

sealed interface Segment {
    data class Text(val text: String) : Segment
    data class Arg(val index: Int) : Segment
}

typealias SegmentedString = List<Segment>

/**
 * Parses a format string such as `"Foo %1$s bar %2$s baz"` into a [SegmentedString] such as
 * `[Segment.Text("Foo"), Segment.Arg(1), Segment.Text("bar"), Segment.Arg(2), Segment.Text("baz")]`.
 */
fun parseFormatString(input: String): SegmentedString =
    Regex($$"""([^%]+)|%(\d+)\$s""")
        .findAll(input)
        .mapNotNull { m ->
            // If the first regex group matches, the match is a text
            m.groupValues[1].takeIf { it.isNotEmpty() }?.let { Segment.Text(it) }
            // If the second regex group matches, the match is an argument
                ?: m.groupValues[2].toIntOrNull()?.let { Segment.Arg(it - 1) }
        }
        .toList()

/**
 * Loads a resources string [resId] and replaces each format argument (`%1$s`, `%2$s`, etc.) with [formatArgs].
 *
 * [formatArgs] support styles and links, see [FormatArg].
 */
@Composable
fun annotatedStringResource(
    @StringRes resId: Int,
    textLinkStyles: TextLinkStyles,
    vararg formatArgs: FormatArg,
): AnnotatedString = buildAnnotatedString {
    parseFormatString(LocalResources.current.getText(resId) as String).forEach { segment ->
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

                    is FormatArg.InlineContent ->
                        appendInlineContent(argument.id)

                    is FormatArg.Link ->
                        withLink(
                            LinkAnnotation.Clickable(tag = "link", styles = textLinkStyles) {
                                argument.onClick()
                            }
                        ) {
                            append(argument.text)
                        }
                }
            }
        }
    }
}

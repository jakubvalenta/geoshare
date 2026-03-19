package page.ooooo.geoshare.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

/**
 * Return a string resources [resId] with format [args] and apply [style] to each of the args.
 *
 * This only makes sense when [args] are unique and don't appear elsewhere in the string.
 */
@Composable
fun styledArgsString(@StringRes resId: Int, style: SpanStyle, vararg args: String): AnnotatedString {
    val formattedString = stringResource(resId, *args)
    return buildAnnotatedString {
        append(formattedString)
        args.forEach { arg ->
            val start = formattedString.indexOf(arg)
            if (start >= 0) {
                addStyle(style, start, start + arg.length)
            }
        }
    }
}

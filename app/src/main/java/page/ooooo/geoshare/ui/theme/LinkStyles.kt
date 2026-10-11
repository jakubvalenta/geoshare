package page.ooooo.geoshare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun defaultLinkStyles(): TextLinkStyles = TextLinkStyles(
    SpanStyle(
        textDecoration = TextDecoration.Underline,
    )
)

@Composable
fun tertiaryLinkStyles(): TextLinkStyles = TextLinkStyles(
    SpanStyle(
        color = MaterialTheme.colorScheme.tertiary,
        textDecoration = TextDecoration.Underline,
    )
)

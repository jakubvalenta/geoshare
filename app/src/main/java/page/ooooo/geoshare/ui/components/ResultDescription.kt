package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultDetails(details: String, modifier: Modifier = Modifier, initialExpanded: Boolean = false) {
    CompositionLocalProvider(
        LocalContentColor provides LocalContentColor.current.copy(alpha = 0.9f),
        LocalTextStyle provides MaterialTheme.typography.bodySmall,
    ) {
        val newLineIndex = details.indexOf('\n')
        if (newLineIndex > -1) {
            val firstLine = details.substring(0, newLineIndex)
            var expanded by remember { mutableStateOf(initialExpanded) }
            ExpandablePane(
                expanded = expanded,
                onSetExpanded = { expanded = it },
                title = {
                    Text(
                        firstLine,
                        Modifier.weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                modifier = modifier
            ) {
                Text(
                    details,
                    modifier.padding(vertical = LocalSpacing.current.tiny),
                    maxLines = 25,
                )
            }
        } else {
            Text(details, modifier)
        }
    }
}

@Composable
fun ResultUri(uri: String, modifier: Modifier = Modifier) {
    CompositionLocalProvider(
        LocalContentColor provides LocalContentColor.current.copy(alpha = 0.9f),
        LocalTextStyle provides MaterialTheme.typography.bodySmall,
    ) {
        if (uri.isNotEmpty()) {
            if (uri.startsWith("https://")) {
                val uriHandler = LocalUriHandler.current
                Text(
                    uri,
                    modifier = modifier.clickable { uriHandler.openUri(uri) },
                    textDecoration = TextDecoration.Underline,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            } else {
                Text(
                    uri,
                    modifier = modifier,
                    fontStyle = FontStyle.Italic,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
    }
}

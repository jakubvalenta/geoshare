package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R

@Composable
fun ResultCheck(text: String, modifier: Modifier = Modifier) {
    ResultCheck(modifier) {
        ParagraphText(text)
    }
}

@Composable
fun ResultCheck(text: AnnotatedString, modifier: Modifier = Modifier) {
    ResultCheck(modifier) {
        ParagraphText(text)
    }
}

@Composable
private fun ResultCheck(modifier: Modifier = Modifier, block: @Composable () -> Unit) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
        Row(modifier) {
            Icon(
                painterResource(R.drawable.warning_24px),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .requiredSize(16.dp),
            )
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelMedium) {
                block()
            }
        }
    }
}

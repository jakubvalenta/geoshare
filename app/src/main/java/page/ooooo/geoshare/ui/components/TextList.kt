package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

data class TextListScope(val bulletSpace: Dp, val bulletWidth: Dp)

@Composable
fun TextList(
    modifier: Modifier = Modifier,
    bulletSpace: Dp = 6.dp,
    bulletWidth: Dp = 4.dp,
    verticalSpace: Dp = 0.dp,
    content: @Composable TextListScope.() -> Unit,
) {
    val textListScope = TextListScope(bulletSpace = bulletSpace, bulletWidth = bulletWidth)
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpace),
    ) {
        textListScope.content()
    }
}

@Composable
fun TextListScope.TextListItem(
    modifier: Modifier = Modifier,
    bullet: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(bulletSpace)) {
        Column(
            Modifier.requiredWidth(bulletWidth),
            horizontalAlignment = Alignment.End,
        ) {
            bullet()
        }
        content()
    }
}

@Composable
fun TextListBullet() {
    Text(stringResource(R.string.text_list_bullet))
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val spacing = LocalSpacing.current
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                TextList(verticalSpace = spacing.tiny) {
                    listOf(
                        R.string.faq_how_text_2_item_http,
                        R.string.faq_how_text_2_item_html,
                        R.string.faq_how_text_2_item_web,
                    ).forEach { textResId ->
                        TextListItem(bullet = { TextListBullet() }) {
                            Text(stringResource(textResId))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val spacing = LocalSpacing.current
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                TextList(verticalSpace = spacing.tiny) {
                    listOf(
                        R.string.faq_how_text_2_item_http,
                        R.string.faq_how_text_2_item_html,
                        R.string.faq_how_text_2_item_web,
                    ).forEach { textResId ->
                        TextListItem(bullet = { TextListBullet() }) {
                            Text(stringResource(textResId))
                        }
                    }
                }
            }
        }
    }
}

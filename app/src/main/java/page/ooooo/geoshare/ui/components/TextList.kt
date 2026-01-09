package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme

data class TextListScope(val bulletSpace: Dp, val bulletWidth: Dp)

@Composable
fun TextList(
    modifier: Modifier = Modifier,
    bulletSpace: Dp = 6.dp,
    bulletWidth: Dp = 24.dp,
    verticalSpace: Dp = 0.dp,
    content: @Composable TextListScope.() -> Unit,
) {
    val textListScope = TextListScope(bulletSpace = bulletSpace, bulletWidth = bulletWidth)
    Column(
        modifier.padding(end = bulletWidth),
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
            TextList {
                TextListItem(Modifier.padding(bottom = 15.dp)) {
                    Text(stringResource(R.string.user_preferences_automation_description))
                }
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_open_app))
                }
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_navigate))
                }
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_copy))
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
            TextList {
                TextListItem(Modifier.padding(bottom = 15.dp)) {
                    Text(stringResource(R.string.user_preferences_automation_description))
                }
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_open_app))
                }
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_navigate))
                }
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_copy))
                }
            }
        }
    }
}

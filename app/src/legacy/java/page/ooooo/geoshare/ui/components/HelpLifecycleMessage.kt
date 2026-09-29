package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.composeEmail
import page.ooooo.geoshare.lib.android.openUriInDefaultApp
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun HelpLifecycleMessage(modifier: Modifier) {
    val context = LocalContext.current
    val installUrl = "https://play.google.com/apps/testing/page.ooooo.geoshare.pro"
    val supportEmail = stringResource(R.string.about_support_email)

    HelpMessageCard(
        title = {
            Text(stringResource(R.string.main_flavor_message_title, stringResource(R.string.app_name)))
        },
        visible = true,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        ParagraphText(stringResource(R.string.main_flavor_message_text_1))
        SelectionContainer {
            Text(
                installUrl,
                Modifier
                    .background(MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f))
                    .clickable {
                        context.openUriInDefaultApp(installUrl)
                    },
                textDecoration = TextDecoration.Underline,
            )
        }
        ParagraphText(
            annotatedStringResource(
                R.string.main_flavor_message_text_2,
                FormatArg.Link(supportEmail, AnnotatedString.UnderlinedLinkStyles) {
                    context.composeEmail(supportEmail)
                },
            )
        )
    }
}

@Suppress("RedundantNullableReturnType")
val helpLifecycleMessage: (@Composable (modifier: Modifier) -> Unit)? = { modifier -> HelpLifecycleMessage(modifier) }

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            helpLifecycleMessage?.invoke(Modifier)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            helpLifecycleMessage?.invoke(Modifier)
        }
    }
}

package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    title: String,
    confirmText: String,
    dismissText: String,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ConfirmationScaffold(
        title,
        modifier,
        startButton = {
            OutlinedButton(
                { onDismissRequest() },
                Modifier.testTag("geoShareConfirmationDialogDismissButton"),
            ) {
                Text(dismissText)
            }
        },
        endButton = {
            Button(
                { onConfirmation() },
                Modifier.testTag("geoShareConfirmationDialogConfirmButton"),
            ) {
                Text(confirmText)
            }
        },
        fill = false,
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            ConfirmationDialog(
                title = "My title",
                confirmText = "Confirm",
                dismissText = "Dismiss",
                onConfirmation = {},
                onDismissRequest = {},
            ) {
                Text(AnnotatedString.fromHtml("My text <i>in italics</i>."))
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            ConfirmationDialog(
                title = "My title",
                confirmText = "Confirm",
                dismissText = "Dismiss",
                onConfirmation = {},
                onDismissRequest = {},
            ) {
                Text(AnnotatedString.fromHtml("My text <i>in italics</i>."))
            }
        }
    }
}

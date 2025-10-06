package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme

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
    AlertDialog(
        title = { Text(text = title) },
        text = { content() },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                { onConfirmation() },
                Modifier.testTag("geoShareConfirmationDialogConfirmButton"),
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                { onDismissRequest() },
                Modifier.testTag("geoShareConfirmationDialogDismissButton"),
            ) {
                Text(dismissText)
            }
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        ConfirmationScaffold(
            lastInputShown = true,
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
        ) {
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
        ConfirmationScaffold(
            lastInputShown = true,
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
        ) {
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

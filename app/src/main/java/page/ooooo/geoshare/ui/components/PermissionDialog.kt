package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun PermissionDialog(
    title: String,
    confirmText: String,
    dismissText: String,
    onConfirmation: (doNotAsk: Boolean) -> Unit,
    onDismissRequest: (doNotAsk: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var doNotAsk by remember { mutableStateOf(false) }

    ConfirmationDialog(
        title = title,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirmation = { onConfirmation(doNotAsk) },
        onDismissRequest = { onDismissRequest(doNotAsk) },
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
            content()
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.permission_do_not_ask),
                    style = MaterialTheme.typography.labelLarge,
                )
                Switch(
                    checked = doNotAsk,
                    onCheckedChange = { doNotAsk = it },
                    modifier = Modifier.testTag("geoShareConfirmationDialogDoNotAskSwitch"),
                )
            }
        }
    }
}

// Preview

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Scaffold {
            PermissionDialog(
                title = stringResource(R.string.converter_google_maps_permission_title),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = {},
                onDismissRequest = {},
            ) {
                val appName = stringResource(R.string.app_name)
                val uriString =
                    "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg?g_ep=CAISDTYuMTE5LjEuNjYwNTAYASC33wEqbCw5NDIyNDgxOSw5NDIyNzI0NSw5NDIyNzI0Niw0NzA3MTcwNCw5NDIwNjE2Niw0NzA2OTUwOCw5NDIxNDE3Miw5NDIxODY0MSw5NDIwMzAxOSw0NzA4NDMwNCw5NDIwODQ1OCw5NDIwODQ0N0ICREU%3D&g_st=isi"
                Text(
                    AnnotatedString.fromHtml(
                        stringResource(
                            R.string.conversion_permission_common_text,
                            uriString,
                            appName,
                        )
                    ),
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Scaffold {
            PermissionDialog(
                title = stringResource(R.string.converter_google_maps_permission_title),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = {},
                onDismissRequest = {},
            ) {
                val appName = stringResource(R.string.app_name)
                val uriString =
                    "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg?g_ep=CAISDTYuMTE5LjEuNjYwNTAYASC33wEqbCw5NDIyNDgxOSw5NDIyNzI0NSw5NDIyNzI0Niw0NzA3MTcwNCw5NDIwNjE2Niw0NzA2OTUwOCw5NDIxNDE3Miw5NDIxODY0MSw5NDIwMzAxOSw0NzA4NDMwNCw5NDIwODQ1OCw5NDIwODQ0N0ICREU%3D&g_st=isi"
                Text(
                    AnnotatedString.fromHtml(
                        stringResource(
                            R.string.conversion_permission_common_text,
                            uriString,
                            appName,
                        )
                    ),
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParseHtmlPermissionPreview() {
    AppTheme {
        Scaffold {
            PermissionDialog(
                title = stringResource(R.string.converter_google_maps_permission_title),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = {},
                onDismissRequest = {},
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .testTag("geoShareParseHtmlPermissionDialog"),
            ) {
                val appName = stringResource(R.string.app_name)
                val uriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                Text(
                    AnnotatedString.fromHtml(
                        stringResource(
                            R.string.conversion_permission_common_text,
                            uriString,
                            appName,
                        )
                    ),
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkParseHtmlPermissionPreview() {
    AppTheme {
        Scaffold {
            PermissionDialog(
                title = stringResource(R.string.converter_google_maps_permission_title),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = {},
                onDismissRequest = {},
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .testTag("geoShareParseHtmlPermissionDialog"),
            ) {
                val appName = stringResource(R.string.app_name)
                val uriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                Text(
                    AnnotatedString.fromHtml(
                        stringResource(
                            R.string.conversion_permission_common_text,
                            uriString,
                            appName,
                        )
                    ),
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }
        }
    }
}

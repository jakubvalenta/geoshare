package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun InputsHeader(text: String, onShowOpenByDefaultSettings: () -> Unit) {
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)

    Column(
        Modifier.padding(start = spacing.windowPadding, top = spacing.tiny, end = spacing.windowPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        ParagraphText(text)
        Button(
            onClick = onShowOpenByDefaultSettings,
            modifier = Modifier.widthIn(max = 400.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) {
            Box(
                Modifier
                    .padding(end = 12.dp)
                    .background(Color.White, shape = CircleShape)
                    .padding(5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Settings,
                    null,
                    tint = Color.Black,
                )
            }
            Column {
                Text(
                    stringResource(R.string.open_by_default),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(R.string.inputs_settings, appName),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val appName = stringResource(R.string.app_name)
            InputsHeader(
                text = stringResource(R.string.inputs_list_text, appName),
                onShowOpenByDefaultSettings = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val appName = stringResource(R.string.app_name)
            InputsHeader(
                text = stringResource(R.string.inputs_list_text, appName),
                onShowOpenByDefaultSettings = {},
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        Surface {
            val appName = stringResource(R.string.app_name)
            InputsHeader(
                text = stringResource(R.string.inputs_list_text, appName),
                onShowOpenByDefaultSettings = {},
            )
        }
    }
}

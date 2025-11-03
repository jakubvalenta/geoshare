package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.outputs.Outputs
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ResultSuccessSheetItem(
    label: @Composable () -> String,
    supportingText: String? = null,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label()) },
        modifier = Modifier.clickable(onClick = onClick),
        supportingContent = supportingText?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                Point.example.let { point ->
                    Outputs.getPointAllTexts(point).forEach { (value, label) ->
                        ResultSuccessSheetItem(label, supportingText = value, onClick = {})
                    }
                    HorizontalDivider()
                    Outputs.getPointUriStrings(point).forEach { (_, label) ->
                        ResultSuccessSheetItem(label, onClick = {})
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
            Column {
                Point.example.let { point ->
                    Outputs.getPointAllTexts(point).forEach { (value, label) ->
                        ResultSuccessSheetItem(label, supportingText = value, onClick = {})
                    }
                    HorizontalDivider()
                    Outputs.getPointUriStrings(point).forEach { (_, label) ->
                        ResultSuccessSheetItem(label, onClick = {})
                    }
                }
            }
        }
    }
}

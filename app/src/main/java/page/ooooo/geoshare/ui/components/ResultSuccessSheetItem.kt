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
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.outputs.allOutputGroups
import page.ooooo.geoshare.lib.outputs.getActionOutputs
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ResultSuccessSheetItem(
    label: String,
    supportingText: String? = null,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
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
                val position = Position.example
                val (copyActionsAndLabels, otherActionsAndLabel) = allOutputGroups
                    .getActionOutputs()
                    .map { it.getAction(position) to it.label() }
                    .partition { (action) -> action is Action.Copy }
                copyActionsAndLabels.forEach { (action, label) ->
                    ResultSuccessSheetItem(label, supportingText = (action as? Action.Copy)?.text) {}
                }
                if (copyActionsAndLabels.isNotEmpty() && otherActionsAndLabel.isNotEmpty()) {
                    HorizontalDivider()
                }
                otherActionsAndLabel.forEach { (_, label) ->
                    ResultSuccessSheetItem(label) {}
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
                val position = Position.example
                val (copyActionsAndLabels, otherActionsAndLabel) = allOutputGroups
                    .getActionOutputs()
                    .map { it.getAction(position) to it.label() }
                    .partition { (action) -> action is Action.Copy }
                copyActionsAndLabels.forEach { (action, label) ->
                    ResultSuccessSheetItem(label, supportingText = (action as? Action.Copy)?.text) {}
                }
                if (copyActionsAndLabels.isNotEmpty() && otherActionsAndLabel.isNotEmpty()) {
                    HorizontalDivider()
                }
                otherActionsAndLabel.forEach { (_, label) ->
                    ResultSuccessSheetItem(label) {}
                }
            }
        }
    }
}

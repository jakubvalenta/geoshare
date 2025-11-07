package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.allOutputGroups
import page.ooooo.geoshare.lib.outputs.getActionOutputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultSuccessSheetContent(
    copyActionsAndLabels: List<Pair<Action, String>>,
    otherActionsAndLabels: List<Pair<Action, String>>,
    headline: String? = null,
    onHide: () -> Unit,
    onRun: (action: Action) -> Unit,
) {
    headline?.let { headline ->
        val spacing = LocalSpacing.current
        Text(
            headline,
            Modifier.padding(start = 16.dp, end = 16.dp, bottom = spacing.medium),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
    copyActionsAndLabels.forEach { (action, label) ->
        ResultSuccessSheetItem(label, supportingText = (action as? Action.Copy)?.text) {
            onRun(action)
            onHide()
        }
    }
    if (copyActionsAndLabels.isNotEmpty() && otherActionsAndLabels.isNotEmpty()) {
        HorizontalDivider()
    }
    otherActionsAndLabels.forEach { (action, label) ->
        ResultSuccessSheetItem(label) {
            onRun(action)
            onHide()
        }
    }
}

@Composable
private fun ResultSuccessSheetItem(
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
                val (copyActionsAndLabels, otherActionsAndLabels) = allOutputGroups.getActionOutputs()
                    .map { it.getAction(position) to it.label(position) }
                    .partition { (action) -> action is Action.Copy }
                ResultSuccessSheetContent(
                    copyActionsAndLabels = copyActionsAndLabels,
                    otherActionsAndLabels = otherActionsAndLabels,
                    headline = stringResource(R.string.conversion_succeeded_point_number, 3),
                    onHide = {},
                    onRun = {},
                )
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
                val (copyActionsAndLabels, otherActionsAndLabels) = allOutputGroups.getActionOutputs()
                    .map { it.getAction(position) to it.label(position) }
                    .partition { (action) -> action is Action.Copy }
                ResultSuccessSheetContent(
                    copyActionsAndLabels = copyActionsAndLabels,
                    otherActionsAndLabels = otherActionsAndLabels,
                    headline = stringResource(R.string.conversion_succeeded_point_number, 3),
                    onHide = {},
                    onRun = {},
                )
            }
        }
    }
}

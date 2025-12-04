package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.CopyAction
import page.ooooo.geoshare.lib.outputs.allOutputs
import page.ooooo.geoshare.lib.outputs.getPositionActions
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultSuccessSheetContent(
    position: Position,
    i: Int?,
    copyActions: List<Action>,
    otherActions: List<Action>,
    headline: String? = null,
    onHide: () -> Unit,
    onRun: (action: Action, i: Int?) -> Unit,
) {
    headline?.let { headline ->
        val spacing = LocalSpacing.current
        Text(
            headline,
            Modifier.padding(start = 16.dp, end = 16.dp, bottom = spacing.medium),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
    copyActions.mapNotNull { it as? CopyAction }.forEach { action ->
        ResultSuccessSheetItem({ action.Label() }, description = action.getText(position, i)) {
            onRun(action, i)
            onHide()
        }
    }
    if (copyActions.isNotEmpty() && otherActions.isNotEmpty()) {
        HorizontalDivider()
    }
    otherActions.forEach { action ->
        ResultSuccessSheetItem({ action.Label() }) {
            onRun(action, i)
            onHide()
        }
    }
}

@Composable
private fun ResultSuccessSheetItem(
    label: @Composable () -> Unit,
    description: String? = null,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = label,
        modifier = Modifier.clickable(onClick = onClick),
        supportingContent = description?.let { text ->
            {
                Text(
                    text,
                    Modifier.testTag("geoShareConversionSuccessSheetItemDescription"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
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
                val (copyActions, otherActions) = allOutputs
                    .getPositionActions()
                    .filter { it.isEnabled(position, null) }
                    .partition { it is CopyAction }
                ResultSuccessSheetContent(
                    position = position,
                    i = null,
                    copyActions = copyActions,
                    otherActions = otherActions,
                    headline = stringResource(R.string.conversion_succeeded_point_number, 3),
                    onHide = {},
                    onRun = { _, _ -> },
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
                val (copyActions, otherActions) = allOutputs
                    .getPositionActions()
                    .filter { it.isEnabled(position, null) }
                    .partition { it is CopyAction }
                ResultSuccessSheetContent(
                    position = position,
                    i = null,
                    copyActions = copyActions,
                    otherActions = otherActions,
                    headline = stringResource(R.string.conversion_succeeded_point_number, 3),
                    onHide = {},
                    onRun = { _, _ -> },
                )
            }
        }
    }
}

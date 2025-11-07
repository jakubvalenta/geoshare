package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessPoint(
    i: Int,
    point: Point,
    pointCount: Int,
    textPointOutput: Output.Text<Point>?,
    labelTextPointOutput: Output.PointLabel<Point>?,
    menuPointOutputs: List<Output.Action<Point, Action>>,
    onRun: (action: Action) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (sheetVisible, setSheetVisible) = remember { mutableStateOf(false) }
    val label = labelTextPointOutput?.getText(point, i, pointCount)

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        label?.let { text ->
            Text(
                text,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        SelectionContainer(Modifier.weight(1f)) {
            textPointOutput?.getText(point)?.let { text ->
                Text(text, style = MaterialTheme.typography.bodySmall)
            }
        }
        Box {
            IconButton({ setSheetVisible(true) }, Modifier.size(16.dp)) {
                Icon(
                    painterResource(R.drawable.more_horiz_24px),
                    contentDescription = stringResource(R.string.nav_menu_content_description),
                )
            }
        }
    }
    ResultSuccessSheet(
        sheetVisible = sheetVisible,
        onSetSheetVisible = setSheetVisible,
    ) { onHide ->
        val (copyActionsAndLabels, otherActionsAndLabels) = menuPointOutputs
            .map { it.getAction(point) to it.label(point) }
            .partition { (action) -> action is Action.Copy }
        ResultSuccessSheetContent(
            copyActionsAndLabels = copyActionsAndLabels,
            otherActionsAndLabels = otherActionsAndLabels,
            headline = label,
            onHide = onHide,
            onRun = onRun,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            ResultSuccessPoint(
                i = 3,
                point = Point.example,
                pointCount = 5,
                textPointOutput = allPointOutputGroups.getTextOutput(),
                labelTextPointOutput = allPointOutputGroups.getLabelTextOutput(),
                menuPointOutputs = allPointOutputGroups.getActionOutputs(),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            ResultSuccessPoint(
                i = 3,
                point = Point.example,
                pointCount = 5,
                textPointOutput = allPointOutputGroups.getTextOutput(),
                labelTextPointOutput = allPointOutputGroups.getLabelTextOutput(),
                menuPointOutputs = allPointOutputGroups.getActionOutputs(),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnePointPreview() {
    AppTheme {
        Surface {
            ResultSuccessPoint(
                i = 3,
                point = Point.example,
                pointCount = 1,
                textPointOutput = allPointOutputGroups.getTextOutput(),
                labelTextPointOutput = allPointOutputGroups.getLabelTextOutput(),
                menuPointOutputs = allPointOutputGroups.getActionOutputs(),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkOnePointPreview() {
    AppTheme {
        Surface {
            ResultSuccessPoint(
                i = 0,
                point = Point.example,
                pointCount = 1,
                textPointOutput = allPointOutputGroups.getTextOutput(),
                labelTextPointOutput = allPointOutputGroups.getLabelTextOutput(),
                menuPointOutputs = allPointOutputGroups.getActionOutputs(),
                onRun = {},
            )
        }
    }
}

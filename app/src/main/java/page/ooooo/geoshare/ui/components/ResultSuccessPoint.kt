package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
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
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private val iconSize = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessPoint(
    i: Int,
    point: Point,
    pointCount: Int,
    textPointOutput: Output.Text<Point>?,
    namePointOutput: Output.PointLabel<Point>?,
    menuPointOutputs: List<Output.Action<Point, Action>>,
    onRun: (action: Action) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (sheetVisible, setSheetVisible) = remember { mutableStateOf(false) }
    val name = namePointOutput?.getText(point, i, pointCount)

    Box {
        FlowRow(
            Modifier
                .fillMaxWidth()
                .padding(end = iconSize + spacing.tiny),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            name?.let { text ->
                Text(
                    text,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            SelectionContainer {
                textPointOutput?.getText(point)?.let { text ->
                    Text(text, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Box(Modifier.align(Alignment.TopEnd)) {
            IconButton({ setSheetVisible(true) }, Modifier.size(iconSize)) {
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
            .filter { it.isEnabled(point) }
            .map { it.getAction(point) to it.label() }
            .partition { (action) -> action is Action.Copy }
        ResultSuccessSheetContent(
            copyActionsAndLabels = copyActionsAndLabels,
            otherActionsAndLabels = otherActionsAndLabels,
            headline = name,
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
                namePointOutput = allPointOutputGroups.getNameOutput(),
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
                namePointOutput = allPointOutputGroups.getNameOutput(),
                menuPointOutputs = allPointOutputGroups.getActionOutputs(),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LongNamePreview() {
    AppTheme {
        Surface {
            @Suppress("SpellCheckingInspection")
            ResultSuccessPoint(
                i = 3,
                point = Point.example.copy(name = "Reuterstraße 1, Berlin-Neukölln, Germany"),
                pointCount = 1,
                textPointOutput = allPointOutputGroups.getTextOutput(),
                namePointOutput = allPointOutputGroups.getNameOutput(),
                menuPointOutputs = allPointOutputGroups.getActionOutputs(),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLongNamePreview() {
    AppTheme {
        Surface {
            @Suppress("SpellCheckingInspection")
            ResultSuccessPoint(
                i = 3,
                point = Point.example.copy(name = "Reuterstraße 1, Berlin-Neukölln, Germany"),
                pointCount = 1,
                textPointOutput = allPointOutputGroups.getTextOutput(),
                namePointOutput = allPointOutputGroups.getNameOutput(),
                menuPointOutputs = allPointOutputGroups.getActionOutputs(),
                onRun = {},
            )
        }
    }
}

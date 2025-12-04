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
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.collections.filter

private val iconSize = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessPoint(
    position: Position,
    i: Int,
    onRun: (action: Action, i: Int?) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (sheetVisible, setSheetVisible) = remember { mutableStateOf(false) }
    val name = allOutputs.getName(position, i)

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
                allOutputs.getText(position, i)?.let { text ->
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
        val (copyActions, otherActions) = allOutputs.getPointActions()
            .filter { it.isEnabled(position, i) }
            .partition { it is CopyAction }
        ResultSuccessSheetContent(
            position = position,
            i = i,
            copyActions = copyActions,
            otherActions = otherActions,
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
                position = Position(
                    points = persistentListOf(
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                    ),
                ),
                i = 2,
                onRun = { _, _ -> },
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
                position = Position(
                    points = persistentListOf(
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                    ),
                ),
                i = 2,
                onRun = { _, _ -> },
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
                position = Position(
                    points = persistentListOf(
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(name = "Reuterstraße 1, Berlin-Neukölln, Germany"),
                        Point.genRandomPoint(),
                    ),
                ),
                i = 2,
                onRun = { _, _ -> },
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
                position = Position(
                    points = persistentListOf(
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(name = "Reuterstraße 1, Berlin-Neukölln, Germany"),
                        Point.genRandomPoint(),
                    ),
                ),
                i = 2,
                onRun = { _, _ -> },
            )
        }
    }
}

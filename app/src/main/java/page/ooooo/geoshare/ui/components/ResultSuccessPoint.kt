package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.getOrNull
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private val iconSize = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessPoint(
    points: ImmutableList<Point>,
    i: Int,
    onSelect: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Box {
        FlowRow(
            Modifier
                .fillMaxWidth()
                .padding(end = iconSize + spacing.tiny),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                points.getOrNull(i)?.cleanName
                    ?: stringResource(R.string.conversion_succeeded_point_number, i + 1),
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall,
            )
            SelectionContainer {
                allOutputs.getText(points, i)?.let { text ->
                    Text(text, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Box(Modifier.align(Alignment.TopEnd)) {
            IconButton(onSelect, Modifier.size(iconSize)) {
                Icon(
                    painterResource(R.drawable.content_copy_24px),
                    contentDescription = stringResource(R.string.nav_menu_content_description),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            ResultSuccessPoint(
                points = persistentListOf(
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                ),
                i = 2,
                onSelect = {},
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
                points = persistentListOf(
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                ),
                i = 2,
                onSelect = {},
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
                points = persistentListOf(
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(name = "Reuterstraße 1, Berlin-Neukölln, Germany"),
                    Point.genRandomPoint(),
                ),
                i = 2,
                onSelect = {},
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
                points = persistentListOf(
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(name = "Reuterstraße 1, Berlin-Neukölln, Germany"),
                    Point.genRandomPoint(),
                ),
                i = 2,
                onSelect = {},
            )
        }
    }
}

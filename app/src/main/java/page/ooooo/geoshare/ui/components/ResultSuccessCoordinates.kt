package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.allOutputs
import page.ooooo.geoshare.lib.outputs.getAllPointsChipActions
import page.ooooo.geoshare.lib.outputs.getLastPointChipActions
import page.ooooo.geoshare.lib.outputs.getText
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point
import page.ooooo.geoshare.lib.point.getOrNull
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessCoordinates(
    points: ImmutableList<Point>,
    initialExpanded: Boolean = false,
    onRun: (action: Action, i: Int?) -> Unit,
    onSelect: (points: ImmutableList<Point>, i: Int?) -> Unit,
) {
    val spacing = LocalSpacing.current
    var expanded by remember { mutableStateOf(initialExpanded) }

    Column {
        Headline(
            points.getOrNull(null)?.cleanName
                ?: if (points.size > 1) {
                    stringResource(R.string.conversion_succeeded_point_last)
                } else {
                    stringResource(R.string.conversion_succeeded_title)
                },
            Modifier
                .testTag("geoShareResultSuccessLastPointName")
                .padding(top = 4.dp), // Align with the "Open with..." headline on wide screen
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = spacing.windowPadding, end = spacing.windowPadding - 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            allOutputs.getText(points, null)?.let { text ->
                SelectionContainer {
                    Text(
                        text,
                        Modifier.testTag("geoShareResultSuccessLastPointCoordinates"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } ?: Text(
                stringResource(R.string.conversion_succeeded_description_q_only),
                Modifier
                    .testTag("geoShareResultSuccessLastPointDescription")
                    .fillMaxWidth(),
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineBreak = LineBreak.Paragraph,
                    hyphens = Hyphens.Auto,
                ),
            )
            IconButton(
                { onSelect(points, null) },
                Modifier.testTag("geoShareResultSuccessLastPointMenu")
            ) {
                Icon(
                    painterResource(R.drawable.content_copy_24px),
                    contentDescription = stringResource(R.string.nav_menu_content_description),
                )
            }
        }
        ResultChips {
            allOutputs.getLastPointChipActions()
                .filter { it.isEnabled(points, null) }
                .forEach { action ->
                    ResultChip({ action.Label() }, icon = action.getIcon()) { onRun(action, null) }
                }
            if (points.size <= 1) {
                allOutputs.getAllPointsChipActions()
                    .filter { it.isEnabled(points, null) }
                    .forEach { action ->
                        ResultChip({ action.Label() }, icon = action.getIcon()) { onRun(action, null) }
                    }
            }
        }
        points.takeIf { it.size > 1 }?.let { points ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(
                    Modifier.padding(top = spacing.mediumAdaptive),
                    verticalArrangement = Arrangement.spacedBy(spacing.tinyAdaptive),
                ) {
                    ExpandablePane(
                        expanded = expanded,
                        headline = stringResource(R.string.conversion_succeeded_point_all, points.size),
                        onSetExpanded = { expanded = it },
                        modifier = Modifier.padding(
                            start = spacing.windowPadding,
                            end = spacing.windowPadding - 8.dp, // Align with last point menu
                        ),
                    ) {
                        Column(
                            Modifier.padding(
                                top = spacing.mediumAdaptive,
                                end = 10.dp, // Align with expand/collapse icon
                            ),
                            verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)
                        ) {
                            points.indices.forEach { i ->
                                ResultSuccessPoint(
                                    points = points,
                                    i = i,
                                    onSelect = { onSelect(points, i) },
                                )
                            }
                        }
                    }
                    allOutputs.getAllPointsChipActions()
                        .filter { it.isEnabled(points, null) }
                        .takeIf { it.isNotEmpty() }
                        ?.let { actions ->
                            ResultChips {
                                actions.forEach { action ->
                                    ResultChip({ action.Label() }, icon = action.getIcon()) { onRun(action, null) }
                                }
                            }
                        }
                }
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(Point.example),
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(Point.example),
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DescriptionPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(WGS84Point(name = "Berlin, Germany", z = 13.0)),
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDescriptionPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(WGS84Point(name = "Berlin, Germany", z = 13.0)),
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LabelPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(
                    Point.example,
                    WGS84Point(50.123456, 11.123456, name = "My point"),
                ),
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLabelPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(
                    Point.example,
                    WGS84Point(50.123456, 11.123456, name = "My point"),
                ),
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                ),
                initialExpanded = true,
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                ),
                initialExpanded = true,
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsWithNamePreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(name = "Berlin, Germany", z = 13.0),
                ),
                initialExpanded = true,
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsWithNamePreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                points = persistentListOf(
                    Point.genRandomPoint(),
                    Point.genRandomPoint(),
                    Point.genRandomPoint(name = "Berlin, Germany", z = 13.0),
                ),
                initialExpanded = true,
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

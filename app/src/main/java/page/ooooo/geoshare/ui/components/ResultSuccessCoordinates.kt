package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessCoordinates(
    points: ImmutableList<Point>,
    onRun: (action: Action, i: Int?) -> Unit,
    onSelect: (points: ImmutableList<Point>, i: Int?) -> Unit,
) {
    val spacing = LocalSpacing.current

    Column {
        Headline(
            allOutputs.getName(points, null) ?: stringResource(R.string.conversion_succeeded_title),
            Modifier
                .testTag("geoShareConversionSuccessPositionName")
                .padding(top = 4.dp), // Align with the "Open with..." headline on wide screen
        )
        ResultCard(
            main = {
                allOutputs.getText(points, null)?.let { text ->
                    SelectionContainer {
                        Text(
                            text,
                            Modifier.testTag("geoShareConversionSuccessPositionCoordinates"),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } ?: Text(
                    stringResource(R.string.conversion_succeeded_description_q_only),
                    Modifier
                        .testTag("geoShareConversionSuccessPositionDescription")
                        .fillMaxWidth(),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineBreak = LineBreak.Paragraph,
                        hyphens = Hyphens.Auto,
                    ),
                )
            },
            end = {
                IconButton(
                    { onSelect(points, null) },
                    Modifier.testTag("geoShareConversionSuccessPositionMenuButton")
                ) {
                    Icon(
                        painterResource(R.drawable.content_copy_24px),
                        contentDescription = stringResource(R.string.conversion_succeeded_copy_content_description)
                    )
                }
            },
            bottom = points.takeIf { it.size > 1 }?.let { points ->
                {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.tinyAdaptive)) {
                        points.indices.forEach { i ->
                            ResultSuccessPoint(
                                points = points,
                                i = i,
                                onSelect = { onSelect(points, i) },
                            )
                        }
                    }
                }
            },
            chips = {
                allOutputs.getChipActions()
                    .filter { it.isEnabled(points, null) }
                    .forEach { action ->
                        ResultCardChip({ action.Label() }, icon = action.getIcon()) { onRun(action, null) }
                    }
            },
        )
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
                points = persistentListOf(WGS84Point(50.123456, 11.123456, name = "My point")),
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
                points = persistentListOf(WGS84Point(50.123456, 11.123456, name = "My point")),
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
                ),
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
                ),
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsAndDescriptionPreview() {
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
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsAndDescriptionPreview() {
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
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

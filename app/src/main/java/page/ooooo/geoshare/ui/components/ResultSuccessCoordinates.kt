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
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessCoordinates(
    position: Position,
    onRun: (action: Action, i: Int?) -> Unit,
    onSelect: (position: Position, i: Int?) -> Unit,
) {
    val spacing = LocalSpacing.current

    Column {
        (allOutputs.getName(position, null)
            ?: stringResource(R.string.conversion_succeeded_title)).let { text ->
            Headline(text, Modifier.testTag("geoShareConversionSuccessPositionName"))
        }
        ResultCard(
            main = {
                allOutputs.getText(position, null)?.let { text ->
                    SelectionContainer {
                        Text(
                            text,
                            Modifier.testTag("geoShareConversionSuccessPositionCoordinates"),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                allOutputs.getDescription(position)?.takeIf { it.isNotEmpty() }?.let { text ->
                    SelectionContainer {
                        Text(
                            text,
                            Modifier
                                .testTag("geoShareConversionSuccessPositionDescription")
                                .fillMaxWidth()
                                .padding(top = spacing.tinyAdaptive, bottom = spacing.smallAdaptive),
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            end = {
                IconButton(
                    { onSelect(position, null) },
                    Modifier.testTag("geoShareConversionSuccessPositionMenuButton")
                ) {
                    Icon(
                        painterResource(R.drawable.content_copy_24px),
                        contentDescription = stringResource(R.string.conversion_succeeded_copy_content_description)
                    )
                }
            },
            bottom = position.points?.takeIf { it.size > 1 }?.let { points ->
                {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.tinyAdaptive)) {
                        points.indices.forEach { i ->
                            ResultSuccessPoint(
                                position = position,
                                i = i,
                                onSelect = { onSelect(position, i) },
                            )
                        }
                    }
                }
            },
            chips = {
                allOutputs.getChipActions()
                    .filter { it.isEnabled(position, null) }
                    .forEach { action ->
                        ResultCardChip({ action.Label() }) { onRun(action, null) }
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
                position = Position.example,
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
                position = Position.example,
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
                position = Position.example.copy(q = "Berlin, Germany", z = 13.0),
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
                position = Position.example.copy(q = "Berlin, Germany", z = 13.0),
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
                position = Position(Srs.WGS84, 50.123456, 11.123456, name = "My point"),
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
                position = Position(Srs.WGS84, 50.123456, 11.123456, name = "My point"),
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
                position = Position(
                    points = persistentListOf(
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                    ),
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
                position = Position(
                    points = persistentListOf(
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                    ),
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
                position = Position(
                    points = persistentListOf(
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                    ),
                    q = "Berlin, Germany",
                    z = 13.0,
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
                position = Position(
                    points = persistentListOf(
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                        Point.genRandomPoint(),
                    ),
                    q = "Berlin, Germany",
                    z = 13.0,
                ),
                onRun = { _, _ -> },
                onSelect = { _, _ -> },
            )
        }
    }
}

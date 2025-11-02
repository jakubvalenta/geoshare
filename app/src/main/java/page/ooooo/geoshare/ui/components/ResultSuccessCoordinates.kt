package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.outputs.GoogleMapsOutput
import page.ooooo.geoshare.lib.outputs.Outputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultSuccessCoordinates(
    position: Position,
    onCopy: (text: String) -> Unit,
    onOpenChooser: (uriString: String) -> Unit,
    onSave: () -> Boolean,
) {
    val spacing = LocalSpacing.current
    var menuExpanded by remember { mutableStateOf(false) }

    ResultCard(
        main = {
            SelectionContainer {
                Text(
                    Outputs.default.getPositionText(position).value,
                    Modifier
                        .testTag("geoShareConversionSuccessPositionCoordinates")
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            position.toParamsString("\t\t").takeIf { it.isNotEmpty() }?.let {
                SelectionContainer {
                    Text(
                        it,
                        Modifier
                            .testTag("geoShareConversionSuccessPositionParams")
                            .fillMaxWidth(),
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        after = {
            Box {
                IconButton({ menuExpanded = true }) {
                    Icon(
                        painterResource(R.drawable.content_copy_24px),
                        contentDescription = stringResource(R.string.conversion_succeeded_copy_content_description)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    Outputs.getPositionAllTexts(position).map { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label()) },
                            onClick = {
                                menuExpanded = false
                                onCopy(value)
                            },
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.content_copy_24px),
                                    stringResource(R.string.conversion_succeeded_copy_content_description),
                                )
                            },
                        )
                    }
                }
            }
        },
        bottom = position.points?.takeIf { it.size > 1 }?.let { points ->
            {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                    points.forEachIndexed { i, point ->
                        ResultSuccessPoint(i, point, onCopy, onOpenChooser)
                    }
                }
            }
        },
        chips = { lastPaddingEnd ->
            ResultCardChip(stringResource(R.string.conversion_succeeded_copy_geo)) {
                onCopy(position.toGeoUriString())
            }
            ResultCardChip(stringResource(R.string.conversion_succeeded_copy_google_maps)) {
                onCopy(GoogleMapsOutput.getPositionText(position).value)
            }
            ResultCardChip(
                stringResource(R.string.conversion_succeeded_save_gpx),
                Modifier.padding(end = lastPaddingEnd),
            ) {
                onSave()
            }
        },
    )
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
                onCopy = {},
                onOpenChooser = {},
                onSave = { true },
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
                onCopy = {},
                onOpenChooser = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OneAppPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                position = Position.example,
                onCopy = {},
                onOpenChooser = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkOneAppPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                position = Position.example,
                onCopy = {},
                onOpenChooser = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParamsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                position = Position.example.copy(q = "Berlin, Germany", z = "13"),
                onCopy = {},
                onOpenChooser = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkParamsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                position = Position.example.copy(q = "Berlin, Germany", z = "13"),
                onCopy = {},
                onOpenChooser = {},
                onSave = { true },
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
                onCopy = {},
                onOpenChooser = {},
                onSave = { true },
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
                onCopy = {},
                onOpenChooser = {},
                onSave = { true },
            )
        }
    }
}

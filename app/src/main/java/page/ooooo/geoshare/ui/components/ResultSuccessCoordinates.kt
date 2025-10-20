package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultSuccessCoordinates(
    position: Position,
    onCopy: (text: String) -> Unit,
    onSave: () -> Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ResultCard(
        main = {
            SelectionContainer {
                Text(
                    position.toNorthSouthWestEastDecCoordsString(),
                    Modifier.testTag("geoShareConversionSuccessPositionCoordinates"),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            position.toParamsString().takeIf { it.isNotEmpty() }?.let {
                SelectionContainer {
                    Text(
                        it,
                        Modifier.testTag("geoShareConversionSuccessPositionParams"),
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            position.points?.takeIf { it.size > 1 }?.mapIndexed { i, (lat, lon) ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                    Text(
                        stringResource(R.string.conversion_succeeded_point_number, i + 1),
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    SelectionContainer {
                        Text(
                            Position(lat, lon).toCoordsDecString(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
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
                    listOf(
                        position.toCoordsDecString(),
                        position.toNorthSouthWestEastDecCoordsString(),
                        position.toGeoUriString(),
                        GoogleMapsUrlConverter.formatUriString(position),
                        AppleMapsUrlConverter.formatUriString(position),
                        MagicEarthUrlConverter.formatUriString(position),
                    ).map { text ->
                        DropdownMenuItem(
                            text = { Text(text) },
                            onClick = {
                                menuExpanded = false
                                onCopy(text)
                            },
                        )
                    }
                }
            }
        },
        chips = {
            ResultCardChip(stringResource(R.string.conversion_succeeded_copy_geo)) {
                onCopy(position.toGeoUriString())
            }
            ResultCardChip(stringResource(R.string.conversion_succeeded_copy_google_maps)) {
                onCopy(GoogleMapsUrlConverter.formatUriString(position))
            }
            ResultCardChip(
                stringResource(R.string.conversion_succeeded_save_gpx),
                Modifier.padding(end = Spacing.small),
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
        Surface {
            ResultSuccessCoordinates(
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            ResultSuccessCoordinates(
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OneAppPreview() {
    AppTheme {
        Surface {
            ResultSuccessCoordinates(
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkOneAppPreview() {
    AppTheme {
        Surface {
            ResultSuccessCoordinates(
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParamsPreview() {
    AppTheme {
        Surface {
            ResultSuccessCoordinates(
                position = Position("50.123456", "11.123456", q = "Berlin, Germany", z = "13"),
                onCopy = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkParamsPreview() {
    AppTheme {
        Surface {
            ResultSuccessCoordinates(
                position = Position("50.123456", "11.123456", q = "Berlin, Germany", z = "13"),
                onCopy = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsPreview() {
    AppTheme {
        Surface {
            ResultSuccessCoordinates(
                position = Position(
                    points = listOf(
                        "59.1293656" to "11.4585672",
                        "59.4154007" to "11.659710599999999",
                        "59.147731699999994" to "11.550661199999999",
                    ),
                ),
                onCopy = {},
                onSave = { true },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsPreview() {
    AppTheme {
        Surface {
            ResultSuccessCoordinates(
                position = Position(
                    points = listOf(
                        "59.1293656" to "11.4585672",
                        "59.4154007" to "11.659710599999999",
                        "59.147731699999994" to "11.550661199999999",
                    ),
                ),
                onCopy = {},
                onSave = { true },
            )
        }
    }
}

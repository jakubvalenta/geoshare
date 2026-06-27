package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ResultPoint(
    point: Point,
    index: Int,
    coordinateFormat: CoordinateFormat,
    coordinateConverter: CoordinateConverter,
    onSelect: () -> Unit,
) {
    ListItem(
        headlineContent = {
            SelectionContainer {
                Text(
                    point.cleanName ?: stringResource(R.string.conversion_succeeded_point_number, index + 1)
                )
            }
        },
        supportingContent = if (point.hasCoordinates()) {
            {
                SelectionContainer {
                    Text(
                        when (coordinateFormat) {
                            CoordinateFormat.DEC -> CoordinateFormatter.formatDecCoords(
                                coordinateConverter.toWGS84(point)
                            )

                            CoordinateFormat.DEG_MIN_SEC -> CoordinateFormatter.formatDegMinSecCoords(
                                coordinateConverter.toWGS84(point)
                            )
                        }
                    )
                }
            }
        } else {
            null
        },
        trailingContent = {
            IconButton(
                onSelect,
                Modifier.offset(x = 10.dp), // Align with expand/collapse icon
            ) {
                Icon(
                    painterResource(R.drawable.content_copy_24px),
                    contentDescription = stringResource(R.string.nav_menu_content_description),
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            ResultPoint(
                point = WGS84Point(NaivePoint.example),
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
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
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            ResultPoint(
                point = WGS84Point(NaivePoint.example),
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
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
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            ResultPoint(
                point = WGS84Point(
                    NaivePoint.genRandomPoint(
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Reuterstraße 1, Berlin-Neukölln, Germany"
                    )
                ),
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
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
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            ResultPoint(
                point = WGS84Point(
                    NaivePoint.genRandomPoint(
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Reuterstraße 1, Berlin-Neukölln, Germany"
                    )
                ),
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NameOnlyPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            ResultPoint(
                point = WGS84Point(
                    name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                    "Reuterstraße 1, Berlin-Neukölln, Germany",
                    source = Source.GENERATED
                ),
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNameOnlyPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            ResultPoint(
                point = WGS84Point(
                    name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                    "Reuterstraße 1, Berlin-Neukölln, Germany",
                    source = Source.GENERATED
                ),
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
                onSelect = {},
            )
        }
    }
}

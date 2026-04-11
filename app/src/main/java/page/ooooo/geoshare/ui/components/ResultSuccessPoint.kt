package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.point.CoordinateConverter
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private val iconSize = 16.dp

@Composable
fun ResultSuccessPoint(
    point: Point,
    index: Int,
    coordinateFormat: CoordinateFormat,
    coordinateFormatter: CoordinateFormatter,
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
                point.cleanName ?: stringResource(R.string.conversion_succeeded_point_number, index + 1),
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall,
            )
            SelectionContainer {
                Text(
                    when (coordinateFormat) {
                        CoordinateFormat.DEC -> coordinateFormatter.formatDecCoords(point)
                        CoordinateFormat.DEG_MIN_SEC -> coordinateFormatter.formatDegMinSecCoords(point)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
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
            val context = LocalContext.current
            val chinaGeometry = ChinaGeometry(context)
            val coordinateConverter = CoordinateConverter(chinaGeometry)
            val coordinateFormatter = CoordinateFormatter(coordinateConverter)
            ResultSuccessPoint(
                point = Point.example,
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateFormatter = coordinateFormatter,
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
            val chinaGeometry = ChinaGeometry(context)
            val coordinateConverter = CoordinateConverter(chinaGeometry)
            val coordinateFormatter = CoordinateFormatter(coordinateConverter)
            ResultSuccessPoint(
                point = Point.example,
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateFormatter = coordinateFormatter,
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
            val chinaGeometry = ChinaGeometry(context)
            val coordinateConverter = CoordinateConverter(chinaGeometry)
            val coordinateFormatter = CoordinateFormatter(coordinateConverter)
            ResultSuccessPoint(
                point = Point.genRandomPoint(name = @Suppress("SpellCheckingInspection") "Reuterstraße 1, Berlin-Neukölln, Germany"),
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateFormatter = coordinateFormatter,
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
            val chinaGeometry = ChinaGeometry(context)
            val coordinateConverter = CoordinateConverter(chinaGeometry)
            val coordinateFormatter = CoordinateFormatter(coordinateConverter)
            ResultSuccessPoint(
                point = Point.genRandomPoint(name = @Suppress("SpellCheckingInspection") "Reuterstraße 1, Berlin-Neukölln, Germany"),
                index = 2,
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateFormatter = coordinateFormatter,
                onSelect = {},
            )
        }
    }
}

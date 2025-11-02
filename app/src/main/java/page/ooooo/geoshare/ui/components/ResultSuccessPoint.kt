package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.outputs.Outputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultSuccessPoint(
    i: Int,
    point: Point,
    onCopy: (text: String) -> Unit,
    onOpenChooser: (uriString: String) -> Unit,
) {
    val spacing = LocalSpacing.current
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.conversion_succeeded_point_number, i + 1),
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.bodySmall,
        )
        SelectionContainer(Modifier.weight(1f)) {
            Text(
                Outputs.default.getPointText(point).value,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Box {
            IconButton({ menuExpanded = true }, Modifier.size(16.dp)) {
                Icon(
                    painterResource(R.drawable.more_horiz_24px),
                    contentDescription = stringResource(R.string.nav_menu_content_description),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                Outputs.getPointAllTexts(point).forEach { (value, label) ->
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
                HorizontalDivider()
                Outputs.getPointUriStrings(point).forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label()) },
                        onClick = {
                            menuExpanded = false
                            onOpenChooser(value)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Share,
                                stringResource(R.string.conversion_succeeded_share),
                            )
                        },
                    )
                }
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
                i = 3,
                point = Point.example,
                onCopy = {},
                onOpenChooser = {},
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
                onCopy = {},
                onOpenChooser = {},
            )
        }
    }
}

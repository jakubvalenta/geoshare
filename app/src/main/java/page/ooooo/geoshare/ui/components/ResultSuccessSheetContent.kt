package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.CopyAction
import page.ooooo.geoshare.lib.outputs.allOutputs
import page.ooooo.geoshare.lib.outputs.getPointActions
import page.ooooo.geoshare.lib.outputs.getPositionActions
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultSuccessSheetContent(
    position: Position,
    i: Int?,
    headline: String? = null,
    onHide: () -> Unit,
    onRun: (action: Action, i: Int?) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (copyActions, otherActions) = allOutputs
        .run {
            if (i == null) {
                getPositionActions()
            } else {
                getPointActions()
            }
        }
        .filter { it.isEnabled(position, i) }
        .partition { it is CopyAction }
    LazyColumn {
        headline?.let { headline ->
            item {
                Text(
                    headline,
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = spacing.mediumAdaptive),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        copyActions.mapNotNull { it as? CopyAction }.forEach { action ->
            item {
                ResultSuccessSheetItem(
                    label = { action.Label() },
                    icon = action.getIcon(),
                    description = action.getText(position, i),
                ) {
                    onHide()
                    onRun(action, i)
                }
            }
        }
        if (copyActions.isNotEmpty() && otherActions.isNotEmpty()) {
            item {
                HorizontalDivider()
            }
        }
        otherActions.forEach { action ->
            item {
                ResultSuccessSheetItem(
                    label = { action.Label() },
                    icon = action.getIcon(),
                ) {
                    onHide()
                    onRun(action, i)
                }
            }
        }
    }
}

@Composable
private fun ResultSuccessSheetItem(
    label: @Composable () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    description: String? = null,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = label,
        modifier = Modifier.clickable(onClick = onClick),
        supportingContent = description?.let { text ->
            {
                Text(
                    text,
                    Modifier.testTag("geoShareConversionSuccessSheetItemDescription"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        leadingContent = icon ?: {
            PlaceholderIcon()
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Preview(showBackground = true, device = "spec:width=1080px,height=2800px,dpi=440")
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            ResultSuccessSheetContent(
                position = Position.example,
                i = null,
                headline = stringResource(R.string.conversion_succeeded_point_number, 3),
                onHide = {},
                onRun = { _, _ -> },
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=2800px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            ResultSuccessSheetContent(
                position = Position.example,
                i = null,
                headline = stringResource(R.string.conversion_succeeded_point_number, 3),
                onHide = {},
                onRun = { _, _ -> },
            )
        }
    }
}

package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> SegmentedList(
    values: List<T>,
    modifier: Modifier = Modifier,
    itemHeadline: @Composable (value: T) -> String,
    itemIsSelected: (value: T) -> Boolean = { false },
    itemOnClick: (value: T) -> Unit = {},
    itemEnabled: ((value: T) -> Boolean)? = null,
    itemLeadingContent: ((value: T) -> (@Composable () -> Unit)?)? = null,
    itemSupportingContent: ((value: T) -> (@Composable () -> Unit)?)? = null,
    itemTrailingContent: ((value: T) -> (@Composable () -> Unit)?)? = null,
    itemTestTag: ((value: T) -> String)? = null,
    sort: Boolean = false,
) {
    val colors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        values
            .map { value -> value to itemHeadline(value) }
            .run {
                if (sort) {
                    sortedBy { (_, headline) -> headline }
                } else {
                    this
                }
            }
            .forEachIndexed { i, (value, headline) ->
                SegmentedListItem(
                    selected = itemIsSelected(value),
                    onClick = { itemOnClick(value) },
                    shapes = ListItemDefaults.segmentedShapes(index = i, count = values.size),
                    modifier = Modifier.run {
                        itemTestTag?.invoke(value)?.let {
                            testTag(it)
                        } ?: this
                    },
                    enabled = itemEnabled?.invoke(value) ?: true,
                    leadingContent = itemLeadingContent?.invoke(value),
                    trailingContent = itemTrailingContent?.invoke(value),
                    supportingContent = itemSupportingContent?.invoke(value)?.let { supportingContent ->
                        {
                            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                                supportingContent()
                            }
                        }
                    },
                    colors = colors,
                ) {
                    Text(headline, style = MaterialTheme.typography.bodyLarge)
                }
            }
    }
}

@Composable
fun SegmentedListLabel(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    val spacing = LocalSpacing.current
    LabelLarge(
        text,
        Modifier.padding(top = spacing.largeAdaptive, bottom = spacing.smallAdaptive),
        color = color,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                SegmentedListLabel("My list")
                SegmentedList(
                    values = listOf("Apples", "Oranges", "Bananas"),
                    itemHeadline = { it },
                    itemIsSelected = { it == "Oranges" },
                    itemOnClick = {},
                    sort = true,
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column {
                SegmentedListLabel("My list")
                SegmentedList(
                    values = listOf("Apples", "Oranges", "Bananas"),
                    itemHeadline = { it },
                    itemIsSelected = { it == "Oranges" },
                    itemOnClick = {},
                    sort = true,
                )
            }
        }
    }
}

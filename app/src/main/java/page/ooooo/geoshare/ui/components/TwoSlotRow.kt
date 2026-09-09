package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.AppTheme

/**
 * A row that contains two composables: [firstContent] at the start and [secondContent] at the end. Both are optional.
 *
 * When [paddingValues] is set, it is passed to [firstContent] and [secondContent], so that [firstContent] gets the
 * start padding value, and [secondContent] gets the end padding value. If there is only [firstContent] or only
 * [secondContent], they get both the start and end padding values. Vertical padding values are ignored.
 */
@Composable
fun TwoSlotRow(
    modifier: Modifier = Modifier,
    firstContent: (@Composable RowScope.(paddingValues: PaddingValues) -> Unit)? = null,
    secondContent: (@Composable RowScope.(paddingValues: PaddingValues) -> Unit)? = null,
    gap: Dp = 0.dp,
    paddingValues: PaddingValues = PaddingValues.Zero,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
) {
    val layoutDirection = LocalLayoutDirection.current

    if (firstContent != null || secondContent != null) {
        Row(modifier, verticalAlignment = verticalAlignment) {
            if (firstContent != null) {
                firstContent(
                    PaddingValues(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = if (secondContent == null) {
                            paddingValues.calculateEndPadding(layoutDirection)
                        } else {
                            gap / 2
                        },
                    )
                )
            }
            if (secondContent != null) {
                secondContent(
                    PaddingValues(
                        start = if (firstContent == null) {
                            paddingValues.calculateStartPadding(layoutDirection)
                        } else {
                            gap / 2
                        },
                        end = paddingValues.calculateEndPadding(layoutDirection),
                    )
                )
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TwoSlotRow(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    firstContent = { paddingValues ->
                        Text(
                            "Start padding 5",
                            Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(paddingValues)
                        )
                    },
                    secondContent = { paddingValues ->
                        Text(
                            "End padding 10", Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(paddingValues)
                        )
                    },
                    gap = 2.dp,
                    paddingValues = PaddingValues(start = 5.dp, end = 10.dp),
                )
                TwoSlotRow(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    firstContent = { paddingValues ->
                        Text(
                            "Start padding 5 & end padding 10",
                            Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(paddingValues)
                        )
                    },
                    gap = 2.dp,
                    paddingValues = PaddingValues(start = 5.dp, end = 10.dp),
                )
                TwoSlotRow(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    secondContent = { paddingValues ->
                        Text(
                            "Start padding 5 & end padding 10", Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(paddingValues)
                        )
                    },
                    gap = 2.dp,
                    paddingValues = PaddingValues(start = 5.dp, end = 10.dp),
                )
                TwoSlotRow(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    gap = 2.dp,
                    paddingValues = PaddingValues(start = 5.dp, end = 10.dp),
                )
            }
        }
    }
}

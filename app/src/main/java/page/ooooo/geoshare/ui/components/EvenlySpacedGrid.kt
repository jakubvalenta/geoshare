package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun EvenlySpacedGrid(
    minItemWidth: Dp,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 0.dp,
    content: @Composable FlowRowScope.(itemWidth: Dp) -> Unit,
) {
    BoxWithConstraints(modifier) {
        with(LocalDensity.current) {
            // Calculate item width in px to make sure items fit in row even after conversion and rounding of dp to px
            val maxWidthPx = maxWidth.toPx()
            val minItemWidthPx = minItemWidth.toPx()
            val horizontalSpacingPx = horizontalSpacing.toPx().toInt()
            val columns = maxOf(
                1,
                ((maxWidthPx + horizontalSpacingPx) / (minItemWidthPx + horizontalSpacingPx)).toInt(),
            )
            val itemWidthPx = ((maxWidthPx - horizontalSpacingPx * (columns - 1)) / columns).toInt()

            FlowRow(horizontalArrangement = Arrangement.spacedBy(horizontalSpacingPx.toDp())) {
                content(itemWidthPx.toDp())
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
                listOf(127.dp, 150.dp, 200.dp).forEach { containerWidth ->
                    EvenlySpacedGrid(
                        minItemWidth = 30.dp,
                        modifier = Modifier
                            .width(containerWidth)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        horizontalSpacing = 5.dp,
                    ) { itemWidth ->
                        repeat(11) { i ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(itemWidth)
                                    .height(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(5.dp)
                                    )
                            ) {
                                Text(
                                    (i + 1).toString(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

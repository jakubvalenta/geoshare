package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultGrid(
    horizontalPadding: Dp = LocalSpacing.current.windowPadding - 10.dp,
    horizontalSpacing: Dp = LocalSpacing.current.tiny,
    minItemWidth: Dp = 78.dp,
    content: @Composable FlowRowScope.(itemWidth: Dp) -> Unit,
) {
    EvenlySpacedGrid(
        minItemWidth,
        modifier = Modifier.padding(horizontal = horizontalPadding),
        horizontalSpacing = horizontalSpacing,
        content = content,
    )
}

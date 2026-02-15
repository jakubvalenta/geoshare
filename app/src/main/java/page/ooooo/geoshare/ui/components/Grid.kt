package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import page.ooooo.geoshare.ui.theme.LocalSpacing

class GridScope {
    val items: MutableList<@Composable RowScope.() -> Unit> = mutableListOf()

    fun item(content: @Composable RowScope.() -> Unit) {
        items.add(content)
    }
}

@Composable
fun Grid(modifier: Modifier = Modifier, content: GridScope.() -> Unit) {
    val spacing = LocalSpacing.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val columnCount = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        5
    } else {
        4
    }

    val scope = GridScope()
    scope.content()

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive),
    ) {
        scope.items.chunked(columnCount).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)) {
                row.forEach { item ->
                    item()
                }
                if (row.size < columnCount) {
                    repeat(columnCount - row.size) {
                        Box(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

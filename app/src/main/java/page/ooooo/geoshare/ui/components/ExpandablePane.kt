package page.ooooo.geoshare.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import page.ooooo.geoshare.ui.theme.LocalSpacing

/**
 * Column with [title] and [content]. Clicking the title toggles the content with animation.
 *
 * [modifier] and [color] are applied to the title row, not to the content.
 */
@Composable
fun ExpandablePane(
    expanded: Boolean,
    onSetExpanded: (expanded: Boolean) -> Unit,
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier.toggleable(value = expanded, enabled = enabled, onValueChange = onSetExpanded),
            horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalContentColor provides color) {
                title()
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                )
            }
        }
        AnimatedVisibility(expanded) {
            content()
        }
    }
}

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import page.ooooo.geoshare.ui.theme.LocalSpacing

/**
 * Column with [title] and [content]. Clicking the title toggles the content with animation.
 *
 * [modifier] is applied to the title row.
 */
@Composable
fun ExpandablePane(
    expanded: Boolean,
    onSetExpanded: (expanded: Boolean) -> Unit,
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
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
            title()
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
            )
        }
        AnimatedVisibility(expanded) {
            content()
        }
    }
}

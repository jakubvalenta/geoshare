package page.ooooo.geoshare.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ExpandablePane(
    expanded: Boolean,
    headline: String,
    onSetExpanded: (expanded: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = spacing.tinyAdaptive)
                .clickable(
                    onClickLabel = stringResource(if (expanded) R.string.faq_item_collapse else R.string.faq_item_expand),
                    onClick = { onSetExpanded(!expanded) },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                headline,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(9f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.weight(1f),
            )
        }
        AnimatedVisibility(expanded) {
            content()
        }
    }
}

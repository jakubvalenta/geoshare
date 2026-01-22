package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ScaffoldAction(
    text: String,
    onClick: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    bottomCorners: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable () -> Unit = {},
) {
    val spacing = LocalSpacing.current

    ElevatedCard(
        shape = MaterialTheme.shapes.large.run {
            if (bottomCorners) {
                this
            } else {
                this.copy(
                    bottomStart = ZeroCornerSize,
                    bottomEnd = ZeroCornerSize,
                )
            }
        },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 20.dp),
    ) {
        Column(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(vertical = spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            content()
            LargeButton(
                text,
                modifier,
                enabled = enabled,
                containerColor = containerColor,
                contentColor = contentColor,
                onClick = onClick,
            )
        }
    }
}

package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    after: @Composable () -> Unit = {},
    before: @Composable () -> Unit = {},
) {
    val spacing = LocalSpacing.current

    Card(
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(vertical = spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            before()
            LargeButton(
                text,
                modifier,
                enabled = enabled,
                containerColor = containerColor,
                contentColor = contentColor,
                onClick = onClick,
            )
            after()
        }
    }
}

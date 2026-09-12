package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun SheetSection(
    modifier: Modifier = Modifier,
    first: Boolean = true,
    title: String? = null,
    items: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(modifier) {
        if (title != null) {
            LabelLarge(
                title,
                Modifier
                    .padding(horizontal = 16.dp)
                    .run {
                        if (first) {
                            this
                        } else {
                            padding(top = spacing.small)
                        }
                    }
                    .padding(bottom = spacing.tiny),
            )
        }
        items()
    }
}

package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultHeadline(text: String, extra: (@Composable RowScope.() -> Unit)? = null) {
    val spacing = LocalSpacing.current

    Row(
        Modifier
            .padding(top = spacing.tiny)
            .padding(horizontal = spacing.windowPadding)
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
        )
        if (extra != null) {
            extra()
        }
    }
}

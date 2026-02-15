package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ResultSuccessSheetItem(
    headlineText: String,
    onClick: (() -> Unit)? = null,
    supportingText: String? = null,
    icon: IconDescriptor? = null,
) {
    ListItem(
        headlineContent = {
            Text(headlineText, Modifier.testTag("geoShareResultSuccessSheetItemHeadline"))
        },
        modifier = Modifier.run {
            if (onClick != null) {
                clickable(onClick = onClick)
            } else {
                this
            }
        },
        supportingContent = supportingText?.let { text ->
            {
                Text(
                    text,
                    Modifier.testTag("geoShareResultSuccessSheetItemDescription"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        leadingContent = icon?.let { { IconFromDescriptor(it, contentDescription = null) } }
            ?: { Spacer(Modifier.size(24.dp)) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

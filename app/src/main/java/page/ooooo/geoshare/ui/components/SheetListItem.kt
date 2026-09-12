package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SheetListItem(
    headlineText: String,
    onClick: (() -> Unit)? = null,
    supportingText: String? = null,
    icon: IconDescriptor? = null,
    prevIcon: IconDescriptor? = null,
) {
    ListItem(
        modifier = Modifier.run {
            if (onClick != null) {
                clickable(onClick = onClick)
            } else {
                this
            }
        },
        supportingContent = supportingText?.let { text ->
            {
                Text(text, overflow = TextOverflow.Ellipsis, maxLines = 1)
            }
        },
        leadingContent = {
            IconFromDescriptor(
                icon?.takeIf { it != prevIcon } ?: SpacerIconDescriptor,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(headlineText, Modifier.testTag("geoShareSheetListItemHeadline"))
    }
}

@Composable
fun OneLineSheetListItem(
    headlineText: String,
    onClick: (() -> Unit)? = null,
    icon: IconDescriptor? = null,
    prevIcon: IconDescriptor? = null,
) {
    ListItem(
        modifier = Modifier.run {
            if (onClick != null) {
                clickable(onClick = onClick)
            } else {
                this
            }
        },
        leadingContent = {
            IconFromDescriptor(
                icon?.takeIf { it != prevIcon } ?: SpacerIconDescriptor,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(
            headlineText,
            Modifier.testTag("geoShareSheetListItemHeadline"),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

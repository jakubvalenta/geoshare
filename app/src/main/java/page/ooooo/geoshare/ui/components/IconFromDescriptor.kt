package page.ooooo.geoshare.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter

sealed interface IconDescriptor

data class CharacterIconDescriptor(val text: String?) : IconDescriptor

data class DrawableIconDescriptor(val drawable: Drawable) : IconDescriptor

data class ImageVectorIconDescriptor(val imageVector: ImageVector) : IconDescriptor

object PlaceholderIconDescriptor : IconDescriptor

data class ResourceIconDescriptor(val id: Int) : IconDescriptor

object SpacerIconDescriptor : IconDescriptor

@Composable
fun IconFromDescriptor(
    descriptor: IconDescriptor,
    @Suppress("SameParameterValue") contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    inverseContentColor: Color = MaterialTheme.colorScheme.surface,
    placeholderContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    when (descriptor) {
        is DrawableIconDescriptor -> Image(
            rememberDrawablePainter(descriptor.drawable),
            contentDescription,
            modifier.requiredSize(size), // Possible stretches the icon, but better showing one tiny icon in a grid
        )

        is CharacterIconDescriptor -> CharIcon(
            descriptor.text,
            contentDescription,
            modifier.requiredSize(size),
            inverseContentColor,
        )

        is ImageVectorIconDescriptor -> Icon(
            descriptor.imageVector,
            contentDescription,
            modifier.requiredSize(size),
        )

        is PlaceholderIconDescriptor -> Box(
            Modifier
                .requiredSize(size)
                .background(placeholderContainerColor, CircleShape),
        )

        is ResourceIconDescriptor -> Icon(
            painterResource(descriptor.id),
            contentDescription,
            modifier.requiredSize(size),
        )

        is SpacerIconDescriptor -> Spacer(Modifier.size(size))
    }
}

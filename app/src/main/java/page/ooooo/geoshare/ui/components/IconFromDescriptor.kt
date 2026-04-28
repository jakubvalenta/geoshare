package page.ooooo.geoshare.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
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

data class DrawableIconDescriptor(val drawable: Drawable) : IconDescriptor

data class ResourceIconDescriptor(val id: Int) : IconDescriptor

data class ImageVectorIconDescriptor(val imageVector: ImageVector) : IconDescriptor

data class CharacterIconDescriptor(val text: String?) : IconDescriptor

@Composable
fun IconFromDescriptor(
    descriptor: IconDescriptor,
    @Suppress("SameParameterValue") contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    inverseContentColor: Color = MaterialTheme.colorScheme.surface,
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

        is ResourceIconDescriptor -> Icon(
            painterResource(descriptor.id),
            contentDescription,
            modifier.requiredSize(size),
        )
    }
}

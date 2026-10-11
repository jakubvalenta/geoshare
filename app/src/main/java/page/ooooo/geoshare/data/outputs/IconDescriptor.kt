package page.ooooo.geoshare.data.outputs

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface IconDescriptor

data class CharacterIconDescriptor(val text: String?) : IconDescriptor

data class DrawableIconDescriptor(val drawable: Drawable) : IconDescriptor

data class ImageVectorIconDescriptor(val imageVector: ImageVector) : IconDescriptor

object PlaceholderIconDescriptor : IconDescriptor

data class ResourceIconDescriptor(val id: Int) : IconDescriptor

object SpacerIconDescriptor : IconDescriptor

package page.ooooo.geoshare.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

fun Modifier.scaleAndCrop(scale: Float) = this
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val scaledWidth = (placeable.width * scale).roundToInt()
        val scaledHeight = (placeable.height * scale).roundToInt()
        layout(scaledWidth, scaledHeight) {
            val offsetX = (placeable.width - scaledWidth) / 2
            val offsetY = (placeable.height - scaledHeight) / 2
            placeable.placeWithLayer(x = -offsetX, y = -offsetY) {
                scaleX = scale
                scaleY = scale
            }
        }
    }

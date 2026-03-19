package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.graphics.BitmapShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import org.intellij.lang.annotations.Language
import page.ooooo.geoshare.ui.theme.AppTheme
import kotlin.math.floor
import kotlin.math.roundToInt

@Suppress("SpellCheckingInspection")
@Language("AGSL")
private val CHECKERED_SHADER = """
    uniform float squareSize;
    layout(color) uniform half4 color1;
    layout(color) uniform half4 color2;

    half4 main(float2 fragCoord) {
        float col = floor(fragCoord.x / squareSize);
        float row = floor(fragCoord.y / squareSize);
        bool isEven = mod(col + row, 2.0) == 0.0;
        return isEven ? color1 : color2;
    }
""".trimIndent()

/**
 * Draw a checkered background using [android.graphics.RuntimeShader] on Android versions that support it, or using a
 * 2x2 tile bitmap on Android versions that don't.
 */
fun Modifier.checkeredBackground(
    squarePx: Float,
    color1: Color = Color.White,
    color2: Color = Color.Black,
): Modifier = drawWithCache {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = android.graphics.RuntimeShader(CHECKERED_SHADER)
        val brush = ShaderBrush(shader)
        shader.setFloatUniform("squareSize", squarePx)

        onDrawBehind {
            shader.setColorUniform(
                "color1",
                android.graphics.Color.valueOf(color1.red, color1.green, color1.blue, color1.alpha)
            )
            shader.setColorUniform(
                "color2",
                android.graphics.Color.valueOf(color2.red, color2.green, color2.blue, color2.alpha)
            )
            drawRect(brush)
        }
    } else {
        val squareWholePx = squarePx.roundToInt()
        val tileBitmap = ImageBitmap(squareWholePx * 2, squareWholePx * 2)
        val tileCanvas = Canvas(tileBitmap)
        val paint = Paint()
        paint.color = color1
        tileCanvas.drawRect(Rect(0f, 0f, squareWholePx.toFloat(), squareWholePx.toFloat()), paint)
        paint.color = color2
        tileCanvas.drawRect(Rect(squareWholePx.toFloat(), 0f, squareWholePx * 2f, squareWholePx.toFloat()), paint)
        paint.color = color2
        tileCanvas.drawRect(Rect(0f, squareWholePx.toFloat(), squareWholePx.toFloat(), squareWholePx * 2f), paint)
        paint.color = color1
        tileCanvas.drawRect(
            Rect(
                squareWholePx.toFloat(),
                squareWholePx.toFloat(),
                squareWholePx * 2f,
                squareWholePx * 2f
            ), paint
        )

        val shader = BitmapShader(
            tileBitmap.asAndroidBitmap(),
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT,
        )
        val brush = ShaderBrush(shader)

        onDrawBehind {
            drawRect(brush)
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun CheckeredBackgroundPreview() {
    AppTheme {
        Surface {
            BoxWithConstraints {
                val wholeSquaresCount = floor(maxWidth.value / 30)
                val squareSizePx = with(LocalDensity.current) { (maxWidth / wholeSquaresCount).toPx() }
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.1f }
                        .checkeredBackground(
                            squareSizePx,
                            Color.Yellow,
                            Color.Blue,
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkCheckeredBackgroundPreview() {
    AppTheme {
        Surface {
            BoxWithConstraints {
                val wholeSquaresCount = floor(maxWidth.value / 30)
                val squareSizePx = with(LocalDensity.current) { (maxWidth / wholeSquaresCount).toPx() }
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.1f }
                        .checkeredBackground(
                            squareSizePx,
                            Color.Yellow,
                            Color.Blue,
                        )
                )
            }
        }
    }
}

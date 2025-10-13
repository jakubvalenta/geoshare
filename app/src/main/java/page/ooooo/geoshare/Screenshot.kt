package page.ooooo.geoshare

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import page.ooooo.geoshare.ui.theme.ScreenshotTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun Screenshot(
    drawableId: Int,
    contentDescription: String,
    origSizePx: IntSize,
    content: @Composable (scale: Float) -> Unit,
) {
    var currentSizePx by remember { mutableStateOf(IntSize.Zero) }
    Box(
        Modifier
            .padding(horizontal = Spacing.large)
            .clip(MaterialTheme.shapes.large),
    ) {
        Image(
            painter = painterResource(drawableId),
            contentDescription = contentDescription,
            modifier = Modifier.onGloballyPositioned { currentSizePx = it.size },
            contentScale = ContentScale.Inside,
        )
        val scale = currentSizePx.width.toFloat() / origSizePx.width
        content(scale)
    }
}

@Composable
fun ScreenshotColumn(
    scale: Float,
    width: Int?,
    x: Int = 0,
    y: Int = 0,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalSpacing: Int = 0,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val density = LocalDensity.current
    Column(
        Modifier.offset { IntOffset(x, y) * scale }
            .let { if (width != null) it.width(with(density) { (width - 2 * x).toDp() * scale }) else it },
        verticalArrangement = Arrangement.spacedBy(with(density) { verticalSpacing.toDp() * scale }),
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun ScreenshotRow(
    scale: Float,
    width: Int,
    x: Int = 0,
    y: Int = 0,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier
            .offset { IntOffset(x, y) * scale }
            .width(with(LocalDensity.current) { (width - 2 * x).toDp() * scale }),
        horizontalArrangement = horizontalArrangement,
        content = content,
    )
}

@Composable
fun ScreenshotText(
    text: String,
    scale: Float,
    modifier: Modifier = Modifier,
    color: Color = ScreenshotTheme.colors.textColor,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign? = null,
    style: TextStyle = ScreenshotTheme.typography.textMedium,
) {
    with(LocalDensity.current) {
        Text(
            text,
            modifier,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            style = style.copy(
                fontSize = style.fontSize * 2.75 / this.density * scale,
                lineHeight = style.lineHeight * 2.75 / this.density * scale,
            ),
        )
    }
}

package page.ooooo.geoshare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val AppTypography = Typography()

@Immutable
data class ScreenshotTypography(
    val textExtraExtraExtraLarge: TextStyle,
    val textExtraExtraLarge: TextStyle,
    val textExtraLarge: TextStyle,
    val textLarge: TextStyle,
    val textMedium: TextStyle,
    val textSmall: TextStyle,
)

val screenshotTypography = ScreenshotTypography(
    textExtraExtraExtraLarge = TextStyle(
        fontSize = 34.sp,
        letterSpacing = 0.sp,
        lineHeight = 34.sp,
    ),
    textExtraExtraLarge = TextStyle(
        fontSize = 19.5.sp,
        letterSpacing = 0.sp,
        lineHeight = 19.5.sp,
    ),
    textExtraLarge = TextStyle(
        fontSize = 17.sp,
        letterSpacing = 0.sp,
        lineHeight = 17.sp,
    ),
    textLarge = TextStyle(
        fontSize = 15.5.sp,
        letterSpacing = 0.sp,
        lineHeight = 15.5.sp,
    ),
    textMedium = TextStyle(
        fontSize = 13.5.sp,
        letterSpacing = 0.sp,
        lineHeight = 13.5.sp,
    ),
    textSmall = TextStyle(
        fontSize = 12.sp,
        letterSpacing = 0.sp,
        lineHeight = 12.sp,
    )
)

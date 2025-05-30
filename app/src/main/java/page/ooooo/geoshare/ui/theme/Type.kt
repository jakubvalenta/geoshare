package page.ooooo.geoshare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val AppTypography = Typography()

val Typography.screenshotTextLarge: TextStyle
    get() = TextStyle(
        fontSize = 17.sp,
        letterSpacing = 0.sp,
        lineHeight = 17.sp,
    )

val Typography.screenshotTextMedium: TextStyle
    get() = TextStyle(
        fontSize = 13.5.sp,
        letterSpacing = 0.sp,
        lineHeight = 13.5.sp,
    )

val Typography.screenshotTextSmall: TextStyle
    get() = TextStyle(
        fontSize = 12.sp,
        letterSpacing = 0.sp,
        lineHeight = 12.sp,
    )

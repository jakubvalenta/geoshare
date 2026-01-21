package page.ooooo.geoshare.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val tiny: Dp = 8.dp,
    val small: Dp = 15.dp,
    val medium: Dp = 30.dp,
    val large: Dp = 45.dp,

    val tinyAdaptive: Dp = tiny,
    val smallAdaptive: Dp = small,
    val mediumAdaptive: Dp = medium,
    val largeAdaptive: Dp = large,

    val builtInTopBarPaddingEnd: Dp = 8.dp,
    val builtInTopBarHeight: Dp = 64.dp,
    val windowPadding: Dp = 16.dp,
)

val defaultSpacing = Spacing()

val smallWindowSpacing = Spacing(
    tinyAdaptive = 4.dp,
    smallAdaptive = 8.dp,
    mediumAdaptive = 16.dp,
    largeAdaptive = 24.dp,
)

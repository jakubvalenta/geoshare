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

    val builtInTopBarPaddingEnd: Dp = 8.dp,
    val windowPadding: Dp = 16.dp,
)

val defaultSpacing = Spacing()

val smallWindowSpacing = defaultSpacing.run {
    copy(
        tiny = 4.dp,
        small = 8.dp,
        medium = 16.dp,
        large = 24.dp,
    )
}

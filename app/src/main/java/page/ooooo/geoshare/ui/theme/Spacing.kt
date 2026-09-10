package page.ooooo.geoshare.ui.theme

import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val extraTiny: Dp = 4.dp,
    val tiny: Dp = 8.dp,
    val small: Dp = 16.dp,
    val medium: Dp = 24.dp,
    val large: Dp = 32.dp,

    val largeButtonHorizontalPadding: Dp = 9.dp,
    val largeButtonMaxWidth: Dp = 400.dp,
    val largeTopAppBarCollapsedHeight: Dp = TopAppBarDefaults.LargeAppBarCollapsedHeight,
    val largeTopAppBarExpandedHeight: Dp = TopAppBarDefaults.LargeAppBarExpandedHeight,
    val windowPadding: Dp = 16.dp,
)

val defaultSpacing = Spacing()

val smallWindowSpacing = Spacing(
    largeTopAppBarExpandedHeight = 128.dp,
)

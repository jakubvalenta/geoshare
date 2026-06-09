package page.ooooo.geoshare.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class TwoPaneScaffoldColors(
    val containerColor: Color,
    val contentColor: Color,
    val mainContainerColor: Color,
    val mainContentColor: Color,
    val wideMainContainerColor: Color,
    val wideMainContentColor: Color,
)

object TwoPaneScaffoldDefaults {
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        mainContainerColor: Color = Color.Transparent,
        mainContentColor: Color = contentColor,
        wideMainContainerColor: Color = mainContainerColor,
        wideMainContentColor: Color = mainContentColor,
    ) = TwoPaneScaffoldColors(
        containerColor = containerColor,
        contentColor = contentColor,
        mainContainerColor = mainContainerColor,
        mainContentColor = mainContentColor,
        wideMainContainerColor = wideMainContainerColor,
        wideMainContentColor = wideMainContentColor,
    )
}

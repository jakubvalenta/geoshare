package page.ooooo.geoshare.lib.inputs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

sealed interface InputChangelogItem {
    val addedInVersionCode: Int

    data class Text(override val addedInVersionCode: Int, val text: @Composable () -> String) : InputChangelogItem

    @Immutable
    data class Url(override val addedInVersionCode: Int, val urlString: String) : InputChangelogItem
}

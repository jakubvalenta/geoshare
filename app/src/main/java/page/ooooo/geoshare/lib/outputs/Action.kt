package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Immutable
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Position

sealed interface Action {
    @Immutable
    data class Copy(val text: String) : Action

    @Immutable
    data class OpenApp(val packageName: String, val uriString: String) : Action

    @Immutable
    data class OpenChooser(val uriString: String) : Action

    @Immutable
    data class SaveGpx(val position: Position, val uriQuote: UriQuote = DefaultUriQuote()) : Action
}

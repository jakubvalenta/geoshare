package page.ooooo.geoshare.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlin.time.Duration

sealed interface Automation {

    @Immutable
    enum class Type {
        COPY_APPLE_MAPS_URI,
        COPY_COORDS_DEC,
        COPY_COORDS_NSWE_DEC,
        COPY_GEO_URI,
        COPY_GOOGLE_MAPS_URI,
        COPY_MAGIC_EARTH_URI,
        NOOP,
        OPEN_APP,
        SAVE_GPX,
        SHARE,
    }

    val type: Type
    val packageName: String?
    val testTag: String?

    fun getAction(position: Position, uriQuote: UriQuote = DefaultUriQuote()): Action?

    @Composable
    fun Label()

    interface Noop : Automation

    interface HasDelay : Automation {
        val delay: Duration

        @Composable
        fun waitingText(counterSec: Int): String
    }

    interface HasSuccessMessage : Automation {
        @Composable
        fun successText(): String
    }

    interface HasErrorMessage : Automation {
        @Composable
        fun errorText(): String
    }
}

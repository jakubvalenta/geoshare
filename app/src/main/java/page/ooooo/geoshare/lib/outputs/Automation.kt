package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
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
    val packageName: String
    val testTag: String?

    fun getAction(position: Position, uriQuote: UriQuote = DefaultUriQuote()): Action?

    @Composable
    fun Label()

    object Noop : Automation {
        override val type = Type.NOOP
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) = null

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_nothing))
        }
    }

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

package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlin.time.Duration

sealed interface Automation : Action {
    @Suppress("SpellCheckingInspection")
    @Immutable
    enum class Type {
        COPY_APPLE_MAPS_URI,
        COPY_COORDS_DEC,
        COPY_COORDS_NSWE_DEC,
        COPY_GEO_URI,
        COPY_GOOGLE_MAPS_NAVIGATE_TO_URI,
        COPY_GOOGLE_MAPS_STREET_VIEW_URI,
        COPY_GOOGLE_MAPS_URI,
        COPY_MAGIC_EARTH_NAVIGATE_TO_URI,
        COPY_MAGIC_EARTH_URI,
        NOOP,
        OPEN_APP,
        OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO,
        OPEN_APP_GOOGLE_MAPS_STREET_VIEW,
        OPEN_APP_GPX_ROUTE,
        OPEN_APP_MAGIC_EARTH_NAVIGATE_TO,
        SAVE_GPX,
        SHARE,
        SHARE_GPX_ROUTE,
    }

    val type: Type
    val packageName: String
    val testTag: String?

    interface HasDelay {
        val delay: Duration

        @Composable
        fun waitingText(counterSec: Int): String
    }
}

interface BasicAutomation : BasicAction, Automation

interface LocationAutomation : LocationAction, Automation

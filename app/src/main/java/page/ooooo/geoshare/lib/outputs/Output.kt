package page.ooooo.geoshare.lib.outputs

import android.content.res.Resources
import android.net.Uri
import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Points
import page.ooooo.geoshare.ui.components.IconDescriptor

sealed interface Output {
    @Composable
    fun label(appDetails: AppDetails): String

    fun getMenuIcon(appDetails: AppDetails): IconDescriptor?

    fun getIcon(appDetails: AppDetails): IconDescriptor? = getMenuIcon(appDetails)

    @Composable
    fun automationLabel(appDetails: AppDetails): String = label(appDetails)

    fun getAutomationDescription(): (@Composable () -> String)? = null

    interface HasErrorText {
        @Composable
        fun errorText(appDetails: AppDetails): String
    }

    interface HasSuccessText {
        @Composable
        fun successText(appDetails: AppDetails): String
    }

    interface HasAutomationErrorText {
        @Composable
        fun automationErrorText(appDetails: AppDetails): String
    }

    interface HasAutomationSuccessText {
        @Composable
        fun automationSuccessText(appDetails: AppDetails): String
    }

    interface HasAutomationDelay {
        @Composable
        fun automationWaitingText(counterSec: Int, appDetails: AppDetails): String
    }
}

sealed interface PointOutput : Output {
    fun toAction(value: Point): Action<Point>

    fun getDescription(value: Point, uriQuote: UriQuote = DefaultUriQuote): String? = null

    /**
     * Output that takes a single [Point].
     *
     * Example: Copy point coordinates, Open point in an app
     */
    sealed interface WithoutLocation : PointOutput {
        suspend fun execute(value: Point, actionContext: ActionContext): Boolean

        override fun toAction(value: Point) = BasicAction.WithPoint(value, this)
    }

    /**
     * Output that takes a single [Point] and a file writer.
     *
     * Example: Save a route from current location to a point as GPX
     */
    sealed interface WithFile : PointOutput {
        fun getFilename(resources: Resources): String

        val mimeType: String

        @Suppress("SameReturnValue")
        suspend fun execute(uri: Uri, value: Point, actionContext: ActionContext): Boolean

        override fun toAction(value: Point) = FileAction.WithPoint(value, this)
    }

    /**
     * Output that takes a single [Point] and current device location.
     *
     * Example: Share a route from current location to a point
     */
    sealed interface WithLocation : PointOutput {
        suspend fun execute(location: Point?, value: Point, actionContext: ActionContext): Boolean

        override fun toAction(value: Point) = LocationAction.WithPoint(value, this)

        @Composable
        fun permissionText(): String
    }
}

sealed interface PointsOutput : Output {
    fun toAction(value: Points): Action<Points>

    @Suppress("SameReturnValue")
    fun getDescription(value: Points, uriQuote: UriQuote = DefaultUriQuote): String? = null

    /**
     * Output that takes a [Point] list.
     *
     * Example: Open GPX route in an app
     */
    sealed interface WithoutLocation : PointsOutput {
        suspend fun execute(value: Points, actionContext: ActionContext): Boolean

        override fun toAction(value: Points) = BasicAction.WithPoints(value, this)
    }

    /**
     * Output that takes a [Point] list and a file writer.
     *
     * Example: Save points as GPX
     */
    sealed interface WithFile : PointsOutput {
        fun getFilename(resources: Resources): String

        val mimeType: String

        @Suppress("SameReturnValue")
        suspend fun execute(uri: Uri, value: Points, actionContext: ActionContext): Boolean

        override fun toAction(value: Points) = FileAction.WithPoints(value, this)
    }

    /**
     * Output that takes a [Point] list and current device location.
     *
     * Example: Share route from current location to last point
     */
    sealed interface WithLocation : PointsOutput {
        suspend fun execute(location: Point?, value: Points, actionContext: ActionContext): Boolean

        override fun toAction(value: Points) = LocationAction.WithPoints(value, this)

        @Composable
        fun permissionText(): String
    }
}

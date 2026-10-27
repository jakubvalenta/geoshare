package page.ooooo.geoshare.data.outputs

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.compose.runtime.Composable
import page.ooooo.geoshare.data.android.AppActivity
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points

sealed interface Output<T> {
    /**
     * A unique id of the output and its parameters. Used as a LazyList key, testTag, etc.
     */
    val id: String

    fun toAction(value: T): Action<T>

    /**
     * Returns true if the output can be executed for the given [value] and should be visible in the UI.
     */
    @Suppress("SameReturnValue")
    fun isEnabled(value: T): Boolean = true

    @Composable
    fun label(appDetails: AppDetails): String

    fun getDescription(value: T): String? = null

    fun getMenuIcon(appDetails: AppDetails): IconDescriptor?

    fun getIcon(appDetails: AppDetails): IconDescriptor? = getMenuIcon(appDetails)

    @Composable
    fun automationLabel(appDetails: AppDetails): String = label(appDetails)

    @Composable
    fun automationDescription(): String? = null

    interface HasActivity<T : AppActivity> {
        @Suppress("EmptyMethod")
        val activity: T
    }

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

sealed interface PointOutput : Output<Point> {
    /**
     * Output that takes a single [Point].
     *
     * Example: Copy point coordinates, Open point in an app
     */
    sealed interface WithoutLocation : PointOutput {
        suspend fun execute(value: Point, context: Context): ActionResult

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
        suspend fun execute(uri: Uri, value: Point, context: Context): ActionResult

        override fun toAction(value: Point) = FileAction.WithPoint(value, this)
    }

    /**
     * Output that takes a single [Point] and current device location.
     *
     * Example: Share a route from current location to a point
     */
    sealed interface WithLocation : PointOutput {
        suspend fun execute(location: Point?, value: Point, context: Context): ActionResult

        override fun toAction(value: Point) = LocationAction.WithPoint(value, this)

        @Composable
        fun permissionText(): String
    }
}

sealed interface PointsOutput : Output<Points> {
    /**
     * Output that takes a [Point] list.
     *
     * Example: Open GPX route in an app
     */
    sealed interface WithoutLocation : PointsOutput {
        suspend fun execute(value: Points, context: Context): ActionResult

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
        suspend fun execute(uri: Uri, value: Points, context: Context): ActionResult

        override fun toAction(value: Points) = FileAction.WithPoints(value, this)
    }

    /**
     * Output that takes a [Point] list and current device location.
     *
     * Example: Share route from current location to last point
     */
    sealed interface WithLocation : PointsOutput {
        suspend fun execute(location: Point?, value: Points, context: Context): ActionResult

        override fun toAction(value: Points) = LocationAction.WithPoints(value, this)

        @Composable
        fun permissionText(): String
    }
}

sealed interface StringOutput : Output<String> {
    override fun toAction(value: String) = BasicAction.WithString(value, this)

    suspend fun execute(value: String, context: Context): ActionResult
}

fun Output<*>.toAction(point: Point, points: Points, source: String): Action<*> =
    when (this) {
        is PointOutput -> toAction(point)
        is PointsOutput -> toAction(points)
        is StringOutput -> toAction(source)
    }

fun Output<*>.isEnabled(point: Point, points: Points, source: String): Boolean =
    when (this) {
        is PointOutput -> isEnabled(point)
        is PointsOutput -> isEnabled(points)
        is StringOutput -> isEnabled(source)
    }

package page.ooooo.geoshare.lib.outputs

import android.content.res.Resources
import android.net.Uri
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Points
import page.ooooo.geoshare.lib.point.WGS84Point

/**
 * Action is an [output] with the [value] that's needed to execute it.
 */
sealed interface Action<T> {
    val value: T
    val output: Output

    fun getDescription(value: T, uriQuote: UriQuote = DefaultUriQuote): String?
}

sealed interface BasicAction<T> : Action<T> {
    suspend fun execute(actionContext: ActionContext): Boolean

    data class WithPoint(
        override val value: Point,
        override val output: PointOutput.WithoutLocation,
    ) : BasicAction<Point> {
        override suspend fun execute(actionContext: ActionContext) =
            output.execute(value, actionContext)

        override fun getDescription(value: Point, uriQuote: UriQuote) =
            output.getDescription(value)
    }

    data class WithPoints(
        override val value: Points,
        override val output: PointsOutput.WithoutLocation,
    ) : BasicAction<Points> {
        override suspend fun execute(actionContext: ActionContext) =
            output.execute(value, actionContext)

        override fun getDescription(value: Points, uriQuote: UriQuote) =
            output.getDescription(value)
    }
}

sealed interface LocationAction<T> : Action<T> {
    suspend fun execute(location: Point, actionContext: ActionContext): Boolean

    data class WithPoint(
        override val value: Point,
        override val output: PointOutput.WithLocation,
    ) : LocationAction<Point> {
        override suspend fun execute(location: Point, actionContext: ActionContext) =
            output.execute(location, value, actionContext)

        override fun getDescription(value: Point, uriQuote: UriQuote) =
            output.getDescription(value)
    }

    data class WithPoints(
        override val value: Points,
        override val output: PointsOutput.WithLocation,
    ) : LocationAction<Points> {
        override suspend fun execute(location: Point, actionContext: ActionContext) =
            output.execute(location, value, actionContext)

        override fun getDescription(value: Points, uriQuote: UriQuote) =
            output.getDescription(value)
    }
}

sealed interface FileAction<T> : Action<T> {
    fun getFilename(resources: Resources): String

    val mimeType: String

    suspend fun execute(uri: Uri, actionContext: ActionContext): Boolean

    data class WithPoint(
        override val value: Point,
        override val output: PointOutput.WithFile,
    ) : FileAction<Point> {
        override fun getFilename(resources: Resources) = output.getFilename(resources)

        override val mimeType = output.mimeType

        override suspend fun execute(uri: Uri, actionContext: ActionContext) =
            output.execute(uri, value, actionContext)

        override fun getDescription(value: Point, uriQuote: UriQuote) =
            output.getDescription(value)
    }

    data class WithPoints(
        override val value: Points,
        override val output: PointsOutput.WithFile,
    ) : FileAction<Points> {
        override fun getFilename(resources: Resources) = output.getFilename(resources)

        override val mimeType = output.mimeType

        override suspend fun execute(uri: Uri, actionContext: ActionContext) =
            output.execute(uri, value, actionContext)

        override fun getDescription(value: Points, uriQuote: UriQuote) =
            output.getDescription(value)
    }
}

val NoopAction = BasicAction.WithPoint(WGS84Point(), NoopOutput)

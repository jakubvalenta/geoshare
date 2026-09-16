package page.ooooo.geoshare.lib.outputs

import android.content.res.Resources
import android.net.Uri
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points

/**
 * Action is an [output] with the [value] ([Point] or [Points]) that's needed to execute it.
 *
 * For example a [CopyCoordsDecOutput] output with a [Point] value is an [Action], which copies the point to clipboard
 * when executed.
 *
 * There are several implementations of this interface which allow executing the [output] with more data than just a
 * [value]. See [BasicAction], [FileAction], [LocationAction], etc.
 */
sealed interface Action<T> {
    val value: T
    val output: Output
}

/**
 * An [Action] that doesn't require anything other than a [value] ([Point] or [Points]) to be executed.
 */
sealed interface BasicAction<T> : Action<T> {
    suspend fun execute(actionContext: ActionContext): ActionResult

    data class WithPoint(
        override val value: Point,
        override val output: PointOutput.WithoutLocation,
    ) : BasicAction<Point> {
        override suspend fun execute(actionContext: ActionContext) =
            output.execute(value, actionContext)
    }

    data class WithPoints(
        override val value: Points,
        override val output: PointsOutput.WithoutLocation,
    ) : BasicAction<Points> {
        override suspend fun execute(actionContext: ActionContext) =
            output.execute(value, actionContext)
    }

    data class WithString(
        override val value: String,
        override val output: StringOutput,
    ) : BasicAction<String> {
        override suspend fun execute(actionContext: ActionContext) =
            output.execute(value, actionContext)
    }
}

/**
 * An [Action] that requires a [value] ([Point] or [Points]) and a content: [Uri] of a file to be executed.
 */
sealed interface FileAction<T> : Action<T> {
    fun getFilename(resources: Resources): String

    val mimeType: String

    suspend fun execute(uri: Uri, actionContext: ActionContext): ActionResult

    data class WithPoint(
        override val value: Point,
        override val output: PointOutput.WithFile,
    ) : FileAction<Point> {
        override fun getFilename(resources: Resources) = output.getFilename(resources)

        override val mimeType = output.mimeType

        override suspend fun execute(uri: Uri, actionContext: ActionContext) =
            output.execute(uri, value, actionContext)
    }

    data class WithPoints(
        override val value: Points,
        override val output: PointsOutput.WithFile,
    ) : FileAction<Points> {
        override fun getFilename(resources: Resources) = output.getFilename(resources)

        override val mimeType = output.mimeType

        override suspend fun execute(uri: Uri, actionContext: ActionContext) =
            output.execute(uri, value, actionContext)
    }
}

/**
 * An [Action] that requires a [value] ([Point] or [Points]) and current device location [Point] to be executed.
 */
sealed interface LocationAction<T> : Action<T> {
    suspend fun execute(location: Point, actionContext: ActionContext): ActionResult

    data class WithPoint(
        override val value: Point,
        override val output: PointOutput.WithLocation,
    ) : LocationAction<Point> {
        override suspend fun execute(location: Point, actionContext: ActionContext) =
            output.execute(location, value, actionContext)
    }

    data class WithPoints(
        override val value: Points,
        override val output: PointsOutput.WithLocation,
    ) : LocationAction<Points> {
        override suspend fun execute(location: Point, actionContext: ActionContext) =
            output.execute(location, value, actionContext)
    }
}

val NoopAction = NoopOutput.toAction("")

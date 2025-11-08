package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.extensions.toDegMinSec
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs
import kotlin.math.abs

object CoordinatesPointOutputGroup : OutputGroup<Point> {

    object TextOutput : Output.Text<Point> {
        override fun getText(value: Point, uriQuote: UriQuote) = formatDegMinSecString(value)
    }

    object LabelTextOutput : Output.PointLabel<Point> {
        @Composable
        override fun getText(value: Point, i: Int, pointCount: Int, uriQuote: UriQuote) = label(value, i, pointCount)
    }

    object CopyDegMinSecOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatDegMinSecString(value))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_copy_coordinates)
    }

    object CopyDecOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatDecString(value))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_copy_coordinates)
    }

    override fun getTextOutput() = TextOutput

    override fun getLabelTextOutput() = LabelTextOutput

    override fun getSupportingTextOutput() = null

    override fun getActionOutputs() = listOf(
        CopyDegMinSecOutput,
        CopyDecOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Point>>()

    override fun getChipOutputs() = emptyList<Output.Action<Point, Action>>()

    override fun getChooserOutput() = null

    override fun getAutomations(packageNames: List<String>) = emptyList<Automation>()

    override fun findAutomation(type: Automation.Type, packageName: String?) = null

    fun formatDecString(value: Point): String = value.toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
        "$latStr, $lonStr"
    }

    fun formatDegMinSecString(value: Point): String = value.toSrs(Srs.WGS84).run {
        lat.toDegMinSec().let { (deg, min, sec) ->
            "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "S" else "N"}, "
        } + lon.toDegMinSec().let { (deg, min, sec) ->
            "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "W" else "E"}"
        }
    }

    @Composable
    fun label(value: Point, i: Int, pointCount: Int): String? =
        if (value.desc?.isNotEmpty() == true) {
            value.desc
        } else if (pointCount > 1) {
            stringResource(R.string.conversion_succeeded_point_number, i + 1)
        } else {
            null
        }
}

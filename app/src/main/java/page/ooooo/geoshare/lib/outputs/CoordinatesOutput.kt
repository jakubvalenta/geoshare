package page.ooooo.geoshare.lib.outputs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.extensions.toDegMinSec
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.getOrNull
import kotlin.math.abs

object CoordinatesOutput : Output {

    open class CopyDecCoordsAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatDecString(points.getOrNull(i)) ?: "0,0"

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_coordinates))
        }

        override fun getIcon() = @Composable {
            Icon(painterResource(R.drawable.content_copy_24px), null)
        }
    }

    open class CopyDegMinSecCoordsAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatDegMinSecString(points.getOrNull(i)) ?: "0 E, 0 N"

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_coordinates))
        }
    }

    object CopyDecCoordsAutomation : CopyDecCoordsAction(), BasicAutomation {
        override val type = Automation.Type.COPY_COORDS_DEC
        override val packageName = ""
        override val testTag = "geoShareUserPreferenceAutomationCopyCoordsDec"

        @Composable
        override fun Label() {
            Column {
                Text(
                    stringResource(R.string.conversion_succeeded_copy_coordinates)
                )
                Text(
                    formatDecString(Point.example) ?: "0,0",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    object CopyDegMinSecCoordsAutomation : CopyDegMinSecCoordsAction(), BasicAutomation {
        override val type = Automation.Type.COPY_COORDS_NSWE_DEC
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun Label() {
            Column {
                Text(
                    stringResource(R.string.conversion_succeeded_copy_coordinates)
                )
                Text(
                    formatDegMinSecString(Point.example) ?: "0 E, 0 N",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
        points.getOrNull(i)?.let { point -> formatDegMinSecString(point) }

    @Composable
    override fun getName(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) = name(points, i)

    override fun getPositionActions(): List<BasicAction> = listOf(
        CopyDecCoordsAction(),
        CopyDegMinSecCoordsAction(),
    )

    override fun getPointActions(): List<BasicAction> = listOf(
        CopyDecCoordsAction(),
        CopyDegMinSecCoordsAction(),
    )

    override fun getRandomAction() = listOf(CopyDecCoordsAction(), CopyDegMinSecCoordsAction()).randomOrNull()

    override fun getAutomations(apps: List<AndroidTools.App>): List<Automation> = listOf(
        CopyDecCoordsAutomation,
        CopyDegMinSecCoordsAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?): Automation? = when (type) {
        Automation.Type.COPY_COORDS_DEC -> CopyDecCoordsAutomation
        Automation.Type.COPY_COORDS_NSWE_DEC -> CopyDegMinSecCoordsAutomation
        else -> null
    }

    private fun formatDecString(point: Point?): String? = point?.toWGS84()?.run {
        latStr?.let { latStr ->
            lonStr?.let { lonStr ->
                "$latStr, $lonStr"
            }
        }
    }

    fun formatDegMinSecString(point: Point?): String? = point?.toWGS84()?.run {
        lat?.let { lat ->
            lon?.let { lon ->
                lat.toDegMinSec().let { (deg, min, sec) ->
                    "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "S" else "N"}, "
                } + lon.toDegMinSec().let { (deg, min, sec) ->
                    "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "W" else "E"}"
                }
            }
        }
    }

    @Composable
    private fun name(points: ImmutableList<Point>, i: Int?): String? =
        points.getOrNull(i)?.let { point ->
            point.name.takeUnless { it.isNullOrEmpty() }?.replace('+', ' ')
                ?: points.size.takeIf { it > 1 }?.let { size ->
                    stringResource(R.string.conversion_succeeded_point_number, if (i == null) size else i + 1)
                }
        }
}

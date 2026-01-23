package page.ooooo.geoshare.lib.outputs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.toDegMinSec
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import kotlin.math.abs

object CoordinatesOutput : Output {

    open class CopyDecCoordsAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote) =
            formatDecString(position, i)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_coordinates))
        }

        override fun getIcon() = @Composable {
            Icon(painterResource(R.drawable.content_copy_24px), null)
        }
    }

    open class CopyDegMinSecCoordsAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote) =
            formatDegMinSecString(position, i)

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
                    formatDecString(Position.example, null),
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
                    formatDegMinSecString(Position.example, null),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    override fun getText(position: Position, i: Int?, uriQuote: UriQuote) = formatDegMinSecString(position, i)

    @Composable
    override fun getName(position: Position, i: Int?, uriQuote: UriQuote) = name(position, i)

    override fun getDescription(position: Position, uriQuote: UriQuote) = formatDescriptionString(position)

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

    private fun formatDecString(position: Position, i: Int?): String =
        (position.getPoint(i) ?: Point(Srs.WGS84))
            .toStringPair(Srs.WGS84)
            .let { (latStr, lonStr) -> "$latStr, $lonStr" }

    fun formatDegMinSecString(position: Position, i: Int?): String =
        (position.getPoint(i) ?: Point(Srs.WGS84))
            .toSrs(Srs.WGS84).run {
                lat.toDegMinSec().let { (deg, min, sec) ->
                    "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "S" else "N"}, "
                } + lon.toDegMinSec().let { (deg, min, sec) ->
                    "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "W" else "E"}"
                }
            }

    private fun formatDescriptionString(position: Position): String = position.run {
        buildList {
            q.takeUnless { it.isNullOrEmpty() }?.let { q ->
                (points?.lastOrNull() ?: Point(Srs.WGS84)).toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
                    if (q != "$latStr,$lonStr") {
                        add(q.replace('+', ' '))
                    }
                }
            }
            zStr?.let { zStr ->
                add("z$zStr")
            }
        }.joinToString("\t\t")
    }

    @Composable
    private fun name(position: Position, i: Int?): String? =
        position.getPoint(i)?.let { point ->
            point.name.takeUnless { it.isNullOrEmpty() }?.replace('+', ' ')
                ?: position.points?.size?.takeIf { it > 1 }?.let { size ->
                    stringResource(R.string.conversion_succeeded_point_number, if (i == null) size else i + 1)
                }
        }
}

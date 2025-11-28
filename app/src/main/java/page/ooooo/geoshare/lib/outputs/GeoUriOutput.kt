package page.ooooo.geoshare.lib.outputs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.re2j.Pattern
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds

object GeoUriOutput : Output {

    private val GCJ_PACKAGE_NAMES = GoogleMapsOutput.PACKAGE_NAMES

    @Suppress("SpellCheckingInspection")
    private val NAME_DISABLED_PACKAGE_NAME_PATTERN = Pattern.compile("""de\.schildbach\.oeffi""")
    private val ZOOM_DISABLED_PACKAGE_NAME_PATTERN = Pattern.compile("""com\.garmin\..+""")

    open class CopyGeoUriAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote): String =
            formatUriString(position, i, Srs.WGS84, nameDisabled = false, zoomDisabled = false, uriQuote = uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_geo))
        }
    }

    open class ShareGeoUriAction : OpenChooserAction() {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatUriString(position, i, Srs.WGS84, nameDisabled = false, zoomDisabled = false, uriQuote = uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_share))
        }
    }

    open class ShareGeoUriWithAppAction(override val packageName: String) : OpenAppAction(packageName) {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatUriString(
                position,
                i,
                srs = if (packageName in GCJ_PACKAGE_NAMES) Srs.GCJ02 else Srs.WGS84,
                nameDisabled = NAME_DISABLED_PACKAGE_NAME_PATTERN.matches(packageName),
                zoomDisabled = ZOOM_DISABLED_PACKAGE_NAME_PATTERN.matches(packageName),
                uriQuote = uriQuote,
            )

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName
                )
            )
        }
    }

    object CopyGeoUriAutomation : CopyGeoUriAction(), BasicAutomation {
        override val type = Automation.Type.COPY_GEO_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object ShareGeoUriAutomation :
        ShareGeoUriAction(),
        Action.HasSuccessMessage,
        BasicAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.SHARE
        override val packageName = ""
        override val testTag = null
        override val delay = 5.seconds

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_share_succeeded)

        @Composable
        override fun errorText() = stringResource(R.string.conversion_automation_share_failed)

        @Composable
        override fun waitingText(counterSec: Int) =
            stringResource(R.string.conversion_automation_share_waiting, counterSec)
    }

    data class ShareGeoUriWithAppAutomation(override val packageName: String) :
        ShareGeoUriWithAppAction(packageName),
        Action.HasSuccessMessage,
        Action.HasErrorMessage,
        BasicAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP
        override val testTag = "geoShareUserPreferenceAutomationOpenApp_${packageName}"

        override val delay = 5.seconds

        @Composable
        override fun Label() {
            val spacing = LocalSpacing.current
            queryAppDetails()?.let { appDetails ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        rememberDrawablePainter(appDetails.icon),
                        appDetails.label,
                        Modifier.widthIn(max = 24.dp),
                    )
                    Text(
                        stringResource(
                            R.string.conversion_succeeded_open_app,
                            queryAppDetails()?.label ?: packageName
                        )
                    )
                }
            } ?: Text(stringResource(R.string.conversion_succeeded_open_app, packageName))
        }

        @Composable
        override fun successText() = stringResource(
            R.string.conversion_automation_open_app_succeeded,
            queryAppDetails()?.label ?: packageName,
        )

        @Composable
        override fun errorText() = stringResource(
            R.string.conversion_automation_open_app_failed,
            queryAppDetails()?.label ?: packageName,
        )

        @Composable
        override fun waitingText(counterSec: Int) = stringResource(
            R.string.conversion_automation_open_app_waiting,
            queryAppDetails()?.label ?: packageName,
            counterSec,
        )
    }

    override fun getPositionActions() = listOf(
        CopyGeoUriAction(),
        ShareGeoUriAction(),
    )

    override fun getPointActions() = listOf(
        CopyGeoUriAction(),
        ShareGeoUriAction(),
    )

    override fun getAppActions(apps: List<AndroidTools.App>) =
        apps.filter { it.type == AndroidTools.AppType.GEO_URI }
            .map { it.packageName to ShareGeoUriWithAppAction(it.packageName) }

    override fun getChipActions() = listOf(CopyGeoUriAction())

    override fun getChooserAction() = ShareGeoUriAction()

    override fun getRandomAction() = CopyGeoUriAction()

    override fun getAutomations(apps: List<AndroidTools.App>): List<Automation> = buildList {
        add(CopyGeoUriAutomation)
        add(ShareGeoUriAutomation)
        apps.filter { it.type == AndroidTools.AppType.GEO_URI }
            .forEach { add(ShareGeoUriWithAppAutomation(it.packageName)) }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?): Automation? = when (type) {
        Automation.Type.COPY_GEO_URI -> CopyGeoUriAutomation
        Automation.Type.SHARE -> ShareGeoUriAutomation
        Automation.Type.OPEN_APP if packageName != null -> ShareGeoUriWithAppAutomation(packageName)
        else -> null
    }

    fun formatUriString(
        position: Position,
        i: Int?,
        srs: Srs,
        nameDisabled: Boolean,
        zoomDisabled: Boolean,
        uriQuote: UriQuote = DefaultUriQuote(),
    ) = buildString {
        // Use custom string builder instead of Uri.toString(), because we want to allow custom chars in query params
        (position.getPoint(i) ?: Point(Srs.WGS84)).let { point ->
            point.toStringPair(srs)
                .let { (latStr, lonStr) -> "$latStr,$lonStr" }
                .let { coordsStr ->
                    append("geo:")
                    append(Uri.formatPath(coordsStr, uriQuote = uriQuote))
                    buildMap {
                        // It's important that the z parameter comes before q, because some map apps require the name (which is
                        // part of the q parameter) to be at the very end of the URI.
                        if (!zoomDisabled) {
                            position.zStr?.let { zStr ->
                                set("z", zStr)
                            }
                        }
                        if (position.q != null) {
                            set("q", position.q)
                        } else if (point.lat != 0.0 && point.lon != 0.0) {
                            if (!nameDisabled && point.name != null) {
                                set("q", "$coordsStr(${point.name})")
                            } else {
                                set("q", coordsStr)
                            }
                        }
                    }
                        .takeIf { it.isNotEmpty() }
                        ?.let { Uri.formatQueryParams(it.toImmutableMap(), allow = ",()", uriQuote = uriQuote) }
                        ?.let { append("?$it") }
                }
        }
    }
}

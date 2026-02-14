package page.ooooo.geoshare.lib.outputs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.DefaultGeoUriAppType
import page.ooooo.geoshare.lib.android.GeoUriAppType
import page.ooooo.geoshare.lib.android.geoUriAppTypes
import page.ooooo.geoshare.lib.android.getByPackageName
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.getOrNull
import page.ooooo.geoshare.ui.components.AppIcon

object GeoUriOutput : Output {

    open class CopyGeoUriAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote): String =
            formatUriString(points, i, uriQuote = uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_geo))
        }

        override fun getIcon() = @Composable {
            Icon(painterResource(R.drawable.language_24px), null)
        }
    }

    open class ShareGeoUriAction : OpenChooserAction() {
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatUriString(points, i, uriQuote = uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_share))
        }

        override fun getIcon() = @Composable {
            Icon(Icons.Default.Share, null)
        }
    }

    open class ShareGeoUriWithAppAction(override val packageName: String, open val appType: GeoUriAppType) :
        OpenAppAction(packageName) {

        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatUriString(points, i, appType, uriQuote = uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
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

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_share_succeeded)

        @Composable
        override fun errorText() = stringResource(R.string.conversion_automation_share_failed)

        @Composable
        override fun waitingText(counterSec: Int) =
            pluralStringResource(R.plurals.conversion_automation_share_waiting, counterSec, counterSec)
    }

    data class ShareGeoUriWithAppAutomation(override val packageName: String, override val appType: GeoUriAppType) :
        ShareGeoUriWithAppAction(packageName, appType),
        Action.HasSuccessMessage,
        Action.HasErrorMessage,
        BasicAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP
        override val testTag = "geoShareUserPreferenceAutomationOpenApp_${packageName}"

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
                )
            )
        }

        override fun getIcon() = @Composable {
            AppIcon(packageName)
        }

        @Composable
        override fun successText() = stringResource(
            R.string.conversion_automation_open_app_succeeded,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
        )

        @Composable
        override fun errorText() = stringResource(
            R.string.conversion_automation_open_app_failed,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
        )

        @Composable
        override fun waitingText(counterSec: Int) = pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
            counterSec,
        )
    }

    override fun getPointsActions() = listOf(
        CopyGeoUriAction(),
        ShareGeoUriAction(),
    )

    override fun getPointActions() = listOf(
        CopyGeoUriAction(),
        ShareGeoUriAction(),
    )

    override fun getAppActions(apps: List<App>) =
        apps.mapNotNull { app ->
            (app.type as? GeoUriAppType)?.let { appType ->
                app.packageName to ShareGeoUriWithAppAction(app.packageName, appType)
            }
        }

    override fun getLastPointChipActions() = listOf(CopyGeoUriAction())

    override fun getChooserAction() = ShareGeoUriAction()

    override fun getRandomAction() = CopyGeoUriAction()

    override fun getAutomations(apps: List<App>): List<Automation> = buildList {
        add(CopyGeoUriAutomation)
        add(ShareGeoUriAutomation)
        apps.forEach { app ->
            (app.type as? GeoUriAppType)?.let { appType ->
                add(ShareGeoUriWithAppAutomation(app.packageName, appType))
            }
        }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?): Automation? = when (type) {
        Automation.Type.COPY_GEO_URI ->
            CopyGeoUriAutomation

        Automation.Type.SHARE ->
            ShareGeoUriAutomation

        Automation.Type.OPEN_APP if packageName != null ->
            ShareGeoUriWithAppAutomation(packageName, geoUriAppTypes.getByPackageName(packageName))

        else -> null
    }

    fun formatUriString(
        points: ImmutableList<Point>,
        i: Int?,
        appType: GeoUriAppType = DefaultGeoUriAppType,
        uriQuote: UriQuote = DefaultUriQuote(),
    ) =
        points.getOrNull(i)?.run {
            when (appType.srs) {
                GeoUriAppType.Srs.GCJ02 -> toGCJ02()
                GeoUriAppType.Srs.WGS84 -> toWGS84()
            }
        }?.run {
            // Use custom string builder instead of Uri.toString(), because we want to allow custom chars in query params
            buildString {
                append("geo:")
                append(
                    Uri.formatPath(
                        latStr?.let { latStr ->
                            lonStr?.let { lonStr ->
                                "$latStr,$lonStr"
                            }
                        } ?: "0,0",
                        uriQuote = uriQuote,
                    )
                )
                buildMap {
                    // It's important that the z parameter comes before q, because some map apps require the name (which is
                    // part of the q parameter) to be at the very end of the URI.
                    zStr
                        ?.takeIf { appType.params.zoomSupported && (appType.params.zoomAndQSupported || name == null) }
                        ?.let { zStr ->
                            set("z", zStr)
                        }
                    latStr?.takeIf { appType.params.pinSupported }?.let { latStr ->
                        lonStr?.let { lonStr ->
                            name?.takeIf { appType.params.nameSupported }?.let { name ->
                                set("q", "$latStr,$lonStr(${name})")
                            } ?: set("q", "$latStr,$lonStr")
                        }
                    } ?: name?.let { name ->
                        set("q", name)
                    }
                }
                    .takeIf { it.isNotEmpty() }
                    ?.let { Uri.formatQueryParams(it.toImmutableMap(), allow = ",()", uriQuote = uriQuote) }
                    ?.let { append("?$it") }
            }
        } ?: "geo:0,0"
}

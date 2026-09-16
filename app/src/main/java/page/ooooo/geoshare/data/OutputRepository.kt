@file:Suppress("DEPRECATION")

package page.ooooo.geoshare.data

import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDegMinSecAutomation
import page.ooooo.geoshare.data.local.preferences.CopyGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkDisplayAppleMapsUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkDisplayGoogleMapsUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkDisplayMagicEarthUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkNavigationAppleMapsUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkNavigationMagicEarthUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyNameAutomation
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayCartesIGNUrlAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayMagicEarthUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenNavigationMagicEarthUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenPointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.OpenRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.OpenRouteOnePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.OpenStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointToContactAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SaveRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SendPointAutomation
import page.ooooo.geoshare.data.local.preferences.ShareDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.SharePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareStreetViewGoogleUriAutomation
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.android.AppActivity
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.android.FileType
import page.ooooo.geoshare.lib.android.TextActivity
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.UriScheme
import page.ooooo.geoshare.lib.android.sorted
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.CopyCoordsDegMinSecOutput
import page.ooooo.geoshare.lib.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.lib.outputs.CopyLinkUriOutput
import page.ooooo.geoshare.lib.outputs.CopyNameOutput
import page.ooooo.geoshare.lib.outputs.CopyStringOutput
import page.ooooo.geoshare.lib.outputs.NoopOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayCartesIGNUrlOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenPointsGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenStreetViewGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.OpenUnknownUriOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.lib.outputs.SavePointGpxOutput
import page.ooooo.geoshare.lib.outputs.SavePointToContactOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.SaveRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.SendPointOutput
import page.ooooo.geoshare.lib.outputs.SendStringOutput
import page.ooooo.geoshare.lib.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.lib.outputs.ShareNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareStreetViewGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.StringOutput
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutputRepository @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
    private val log: Log,
) {
    fun getOutputsForPoint(links: List<Link>): List<PointOutput> =
        listOf(
            CopyCoordsDecOutput(coordinateConverter),
            CopyCoordsDegMinSecOutput(coordinateConverter),
            CopyNameOutput(),
            CopyGeoUriOutput(coordinateConverter),
            *links
                .filter { it.sheetEnabled }
                .groupBy { it.groupOrName }
                .toSortedMap()
                .values
                .flatten()
                .map { CopyLinkUriOutput(it, coordinateConverter) }
                .toTypedArray(),
            ShareDisplayGeoUriOutput(coordinateConverter),
            ShareNavigationGoogleUriOutput(coordinateConverter),
            ShareStreetViewGoogleUriOutput(coordinateConverter),
            SavePointGpxOutput(coordinateConverter),
            SavePointToContactOutput(coordinateConverter),
        )

    fun getOutputsForPoints(): List<PointsOutput> =
        listOf(
            ShareRouteGpxOutput(coordinateConverter),
            SharePointsGpxOutput(coordinateConverter),
            SaveRouteGpxOutput(coordinateConverter),
            SavePointsGpxOutput(coordinateConverter),
        )

    fun getOutputsForApps(activities: List<AppActivity>, hiddenApps: Set<String>?): Map<String, List<Output>> =
        activities
            .groupBy { activity -> activity.packageName }
            .filterKeys { packageName -> hiddenApps?.contains(packageName) != true }
            .mapValues { (_, activities) ->
                activities
                    .sorted()
                    .flatMap { activity -> activity.toOutputs() }
            }

    fun getOutputsForLinks(links: List<Link>): Map<String, List<Output>> =
        links
            .filter { it.appEnabled }
            .groupBy { it.groupOrName }
            .toSortedMap()
            .mapValues { (_, links) ->
                listOf(
                    *links.map { ShareLinkUriOutput(it, coordinateConverter) }.toTypedArray(),
                    *links.map { CopyLinkUriOutput(it, coordinateConverter) }.toTypedArray(),
                )
            }

    fun getOutputsForSharing(): List<Output> =
        listOf(
            ShareDisplayGeoUriOutput(coordinateConverter),
            ShareNavigationGoogleUriOutput(coordinateConverter),
            ShareStreetViewGoogleUriOutput(coordinateConverter),
            ShareRouteGpxOutput(coordinateConverter),
            SharePointsGpxOutput(coordinateConverter),
            SavePointToContactOutput(coordinateConverter),
        )

    fun getOutputsForPointChips(links: List<Link>): List<PointOutput> =
        listOf(
            CopyGeoUriOutput(coordinateConverter),
            *links
                .filter { it.chipEnabled }
                .sortedBy { it.name }
                .map { CopyLinkUriOutput(it, coordinateConverter) }
                .toTypedArray(),
        )

    fun getOutputsForPointsChips(): List<PointsOutput> =
        listOf(
            ShareRouteGpxOutput(coordinateConverter),
            SaveRouteGpxOutput(coordinateConverter),
            SavePointsGpxOutput(coordinateConverter),
        )

    // TODO Test
    fun getOutputsForUri(activities: List<AppActivity>): List<StringOutput> =
        listOf(
            CopyStringOutput,
            *activities
                .mapNotNull { activity ->
                    when (activity) {
                        // Don't show an item for a file activity, because we don't know how to create a file from the
                        // source, which is a URI or a text
                        is FileActivity -> null
                        is TextActivity -> SendStringOutput(activity)
                        is UriActivity -> OpenUnknownUriOutput(activity)
                    }
                }
                .toTypedArray(),
        )

    fun getAutomationOutput(automation: Automation, getLinkByUUID: (linkUUID: UUID) -> Link?): Output? =
        automation.toOutput(coordinateConverter, log, getLinkByUUID)

    private fun AppActivity.toOutputs(): List<Output> =
        when (this) {
            is FileActivity ->
                when (fileType) {
                    FileType.GPX -> listOf(
                        OpenRouteGpxOutput(this, coordinateConverter, log),
                        OpenPointsGpxOutput(this, coordinateConverter, log),
                    )

                    FileType.GPX_ONE_POINT -> listOf(
                        OpenRouteOnePointGpxOutput(this, coordinateConverter, log),
                    )
                }

            is TextActivity -> listOf(
                SendPointOutput(this, coordinateConverter),
            )

            is UriActivity ->
                when (uriScheme) {
                    UriScheme.CARTES_IGN -> listOf(
                        OpenDisplayCartesIGNUrlOutput(this, coordinateConverter),
                    )

                    UriScheme.GEO -> listOf(
                        OpenDisplayGeoUriOutput(this, coordinateConverter),
                    )

                    UriScheme.GOOGLE_NAVIGATION -> listOf(
                        OpenNavigationGoogleUriOutput(this, coordinateConverter),
                    )

                    UriScheme.GOOGLE_STREET_VIEW -> listOf(
                        OpenStreetViewGoogleUriOutput(this, coordinateConverter),
                    )

                    UriScheme.MAGIC_EARTH -> listOf(
                        OpenDisplayMagicEarthUriOutput(this, coordinateConverter),
                        OpenNavigationMagicEarthUriOutput(this, coordinateConverter),
                    )

                    UriScheme.UNKNOWN -> listOf(
                        OpenUnknownUriOutput(this),
                    )
                }
        }
}

fun Automation.toOutput(
    coordinateConverter: CoordinateConverter,
    log: Log = DefaultLog,
    getLinkByUUID: (linkUUID: UUID) -> Link?,
): Output? =
    when (this) {
        is CopyCoordsDecAutomation ->
            CopyCoordsDecOutput(coordinateConverter)

        is CopyCoordsDegMinSecAutomation ->
            CopyCoordsDegMinSecOutput(coordinateConverter)

        is CopyGeoUriAutomation ->
            CopyGeoUriOutput(coordinateConverter)

        is CopyLinkUriAutomation ->
            getLinkByUUID(linkUUID)?.let { link ->
                CopyLinkUriOutput(link, coordinateConverter)
            }

        is CopyLinkDisplayAppleMapsUriAutomation ->
            getLinkByUUID(UUID.fromString(InitialLinks.APPLE_MAPS_DISPLAY_UUID))?.let { link ->
                CopyLinkUriOutput(link, coordinateConverter)
            }

        is CopyLinkDisplayGoogleMapsUriAutomation ->
            getLinkByUUID(UUID.fromString(InitialLinks.GOOGLE_MAPS_DISPLAY_UUID))?.let { link ->
                CopyLinkUriOutput(link, coordinateConverter)
            }

        is CopyLinkDisplayMagicEarthUriAutomation ->
            getLinkByUUID(UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07"))?.let { link ->
                CopyLinkUriOutput(link, coordinateConverter)
            }

        is CopyLinkNavigationAppleMapsUriAutomation ->
            getLinkByUUID(UUID.fromString(InitialLinks.APPLE_MAPS_NAVIGATION_UUID))?.let { link ->
                CopyLinkUriOutput(link, coordinateConverter)
            }

        is CopyLinkNavigationGoogleUriAutomation ->
            getLinkByUUID(UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a"))?.let { link ->
                CopyLinkUriOutput(link, coordinateConverter)
            }

        is CopyLinkNavigationMagicEarthUriAutomation ->
            getLinkByUUID(UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7"))?.let { link ->
                CopyLinkUriOutput(link, coordinateConverter)
            }

        is CopyLinkStreetViewGoogleUriAutomation ->
            getLinkByUUID(UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a"))?.let { link ->
                CopyLinkUriOutput(link, coordinateConverter)
            }

        is CopyNameAutomation ->
            CopyNameOutput()

        is NoopAutomation ->
            NoopOutput()

        is OpenDisplayGeoUriAutomation ->
            packageName?.let { packageName ->
                OpenDisplayGeoUriOutput(UriActivity(packageName, UriScheme.GEO), coordinateConverter)
            }

        is OpenDisplayCartesIGNUrlAutomation ->
            packageName?.let { packageName ->
                OpenDisplayCartesIGNUrlOutput(UriActivity(packageName, UriScheme.CARTES_IGN), coordinateConverter)
            }

        is OpenDisplayMagicEarthUriAutomation ->
            packageName?.let { packageName ->
                OpenDisplayMagicEarthUriOutput(UriActivity(packageName, UriScheme.MAGIC_EARTH), coordinateConverter)
            }

        is OpenNavigationGoogleUriAutomation ->
            packageName?.let { packageName ->
                OpenNavigationGoogleUriOutput(
                    UriActivity(packageName, UriScheme.GOOGLE_NAVIGATION),
                    coordinateConverter
                )
            }

        is OpenNavigationMagicEarthUriAutomation ->
            packageName?.let { packageName ->
                OpenNavigationMagicEarthUriOutput(
                    UriActivity(packageName, UriScheme.MAGIC_EARTH),
                    coordinateConverter
                )
            }

        is OpenStreetViewGoogleUriAutomation ->
            packageName?.let { packageName ->
                OpenStreetViewGoogleUriOutput(
                    UriActivity(packageName, UriScheme.GOOGLE_STREET_VIEW),
                    coordinateConverter
                )
            }

        is OpenPointsGpxAutomation ->
            packageName?.let { packageName ->
                OpenPointsGpxOutput(FileActivity(packageName, FileType.GPX), coordinateConverter, log)
            }

        is OpenRouteGpxAutomation ->
            packageName?.let { packageName ->
                OpenRouteGpxOutput(FileActivity(packageName, FileType.GPX), coordinateConverter, log)
            }

        is OpenRouteOnePointGpxAutomation ->
            packageName?.let { packageName ->
                OpenRouteOnePointGpxOutput(
                    FileActivity(packageName, FileType.GPX_ONE_POINT),
                    coordinateConverter,
                    log,
                )
            }

        is SavePointGpxAutomation ->
            SavePointGpxOutput(coordinateConverter)

        is SavePointsGpxAutomation ->
            SavePointsGpxOutput(coordinateConverter)

        is SaveRouteGpxAutomation ->
            SaveRouteGpxOutput(coordinateConverter)

        is SavePointToContactAutomation ->
            SavePointToContactOutput(coordinateConverter)

        is SendPointAutomation ->
            packageName?.let { packageName ->
                SendPointOutput(TextActivity(packageName, "text/plain"), coordinateConverter)
            }

        is ShareDisplayGeoUriAutomation ->
            ShareDisplayGeoUriOutput(coordinateConverter)

        is ShareLinkUriAutomation ->
            getLinkByUUID(linkUUID)?.let { link ->
                ShareLinkUriOutput(link, coordinateConverter)
            }

        is ShareRouteGpxAutomation ->
            ShareRouteGpxOutput(coordinateConverter)

        is SharePointsGpxAutomation ->
            SharePointsGpxOutput(coordinateConverter)

        is ShareNavigationGoogleUriAutomation ->
            ShareNavigationGoogleUriOutput(coordinateConverter)

        is ShareStreetViewGoogleUriAutomation ->
            ShareStreetViewGoogleUriOutput(coordinateConverter)
    }

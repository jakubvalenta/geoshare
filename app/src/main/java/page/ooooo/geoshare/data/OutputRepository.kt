@file:Suppress("DEPRECATION")

package page.ooooo.geoshare.data

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
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayMagicEarthUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenNavigationMagicEarthUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenPointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.OpenRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.OpenRouteOnePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.OpenStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SaveRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.SharePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareStreetViewGoogleUriAutomation
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.DataTypes
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.formatters.MagicEarthUriFormatter
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.CopyCoordsDegMinSecOutput
import page.ooooo.geoshare.lib.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.lib.outputs.CopyLinkUriOutput
import page.ooooo.geoshare.lib.outputs.NoopOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenPointsGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenStreetViewGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.lib.outputs.SavePointGpxOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.SaveRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.lib.outputs.ShareNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareStreetViewGoogleUriOutput
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutputRepository @Inject constructor(
    private val coordinateFormatter: CoordinateFormatter,
    private val geoUriFormatter: GeoUriFormatter,
    private val googleMapsUriFormatter: GoogleMapsUriFormatter,
    private val gpxFormatter: GpxFormatter,
    private val magicEarthUriFormatter: MagicEarthUriFormatter,
    private val uriFormatter: UriFormatter,
) {
    fun getOutputsForPoint(links: List<Link>): List<PointOutput> =
        listOf(
            CopyCoordsDecOutput(coordinateFormatter),
            CopyCoordsDegMinSecOutput(coordinateFormatter),
            CopyGeoUriOutput(geoUriFormatter),
            *links
                .filter { it.sheetEnabled }
                .groupBy { it.groupOrName }
                .toSortedMap()
                .values
                .flatten()
                .map { CopyLinkUriOutput(it, uriFormatter) }
                .toTypedArray(),
            ShareDisplayGeoUriOutput(geoUriFormatter),
            ShareNavigationGoogleUriOutput(googleMapsUriFormatter),
            ShareStreetViewGoogleUriOutput(googleMapsUriFormatter),
            SavePointGpxOutput(gpxFormatter),
        )

    fun getOutputsForPoints(): List<PointsOutput> =
        listOf(
            ShareRouteGpxOutput(gpxFormatter),
            SharePointsGpxOutput(gpxFormatter),
            SaveRouteGpxOutput(gpxFormatter),
            SavePointsGpxOutput(gpxFormatter),
        )

    fun getOutputsForApps(apps: DataTypes, hiddenApps: Set<String>?): Map<String, List<Output>> =
        apps.filterKeys { hiddenApps?.contains(it) != true }.mapValues { (packageName, dataTypes) ->
            buildList {
                if (DataType.GEO_URI in dataTypes) {
                    add(OpenDisplayGeoUriOutput(packageName, geoUriFormatter))
                }
                if (DataType.MAGIC_EARTH_URI in dataTypes) {
                    add(OpenDisplayMagicEarthUriOutput(packageName, magicEarthUriFormatter))
                    add(OpenNavigationMagicEarthUriOutput(packageName, magicEarthUriFormatter))
                }
                if (DataType.GOOGLE_NAVIGATION_URI in dataTypes) {
                    add(OpenNavigationGoogleUriOutput(packageName, googleMapsUriFormatter))
                }
                if (DataType.GOOGLE_STREET_VIEW_URI in dataTypes) {
                    add(OpenStreetViewGoogleUriOutput(packageName, googleMapsUriFormatter))
                }
                if (DataType.GPX_DATA in dataTypes) {
                    add(OpenRouteGpxOutput(packageName, gpxFormatter))
                    add(OpenPointsGpxOutput(packageName, gpxFormatter))
                }
                if (DataType.GPX_ONE_POINT_DATA in dataTypes) {
                    add(OpenRouteOnePointGpxOutput(packageName, gpxFormatter))
                }
            }
        }

    fun getOutputsForLinks(links: List<Link>): Map<String?, List<Output>> =
        links
            .filter { it.appEnabled }
            .groupBy { it.groupOrName }
            .toSortedMap()
            .mapValues { (_, links) ->
                listOf(
                    *links.map { ShareLinkUriOutput(it, uriFormatter) }.toTypedArray(),
                    *links.map { CopyLinkUriOutput(it, uriFormatter) }.toTypedArray(),
                )
            }

    fun getOutputsForSharing(): List<Output> =
        listOf(
            ShareDisplayGeoUriOutput(geoUriFormatter),
            ShareNavigationGoogleUriOutput(googleMapsUriFormatter),
            ShareStreetViewGoogleUriOutput(googleMapsUriFormatter),
            ShareRouteGpxOutput(gpxFormatter),
            SharePointsGpxOutput(gpxFormatter),
        )

    fun getOutputsForPointChips(links: List<Link>): List<PointOutput> =
        listOf(
            CopyGeoUriOutput(geoUriFormatter),
            *links.filter { it.chipEnabled }.sortedBy { it.name }.map { CopyLinkUriOutput(it, uriFormatter) }
                .toTypedArray(),
        )

    fun getOutputsForPointsChips(): List<PointsOutput> =
        listOf(
            ShareRouteGpxOutput(gpxFormatter),
            SaveRouteGpxOutput(gpxFormatter),
            SavePointsGpxOutput(gpxFormatter),
        )

    suspend fun getAutomationOutput(automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?): Output? =
        when (automation) {
            is CopyCoordsDecAutomation ->
                CopyCoordsDecOutput(coordinateFormatter)

            is CopyCoordsDegMinSecAutomation ->
                CopyCoordsDegMinSecOutput(coordinateFormatter)

            is CopyGeoUriAutomation ->
                CopyGeoUriOutput(geoUriFormatter)

            is CopyLinkUriAutomation ->
                getLinkByUUID(automation.linkUUID)
                    ?.let { link -> CopyLinkUriOutput(link, uriFormatter) }

            is CopyLinkDisplayAppleMapsUriAutomation ->
                getLinkByUUID(UUID.fromString("ce900ea1-2c5d-4641-82f3-a5429a68d603"))
                    ?.let { link -> CopyLinkUriOutput(link, uriFormatter) }

            is CopyLinkDisplayGoogleMapsUriAutomation ->
                getLinkByUUID(UUID.fromString("7bd96da4-beba-4a30-9dbd-b437a49a1dc0"))
                    ?.let { link -> CopyLinkUriOutput(link, uriFormatter) }

            is CopyLinkDisplayMagicEarthUriAutomation ->
                getLinkByUUID(UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07"))
                    ?.let { link -> CopyLinkUriOutput(link, uriFormatter) }

            is CopyLinkNavigationAppleMapsUriAutomation ->
                getLinkByUUID(UUID.fromString("a5092c63-cf5c-4225-9059-e888ae12e215"))
                    ?.let { link -> CopyLinkUriOutput(link, uriFormatter) }

            is CopyLinkNavigationGoogleUriAutomation ->
                getLinkByUUID(UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a"))
                    ?.let { link -> CopyLinkUriOutput(link, uriFormatter) }

            is CopyLinkNavigationMagicEarthUriAutomation ->
                getLinkByUUID(UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7"))
                    ?.let { link -> CopyLinkUriOutput(link, uriFormatter) }

            is CopyLinkStreetViewGoogleUriAutomation ->
                getLinkByUUID(UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a"))
                    ?.let { link -> CopyLinkUriOutput(link, uriFormatter) }

            is NoopAutomation ->
                NoopOutput()

            is OpenDisplayGeoUriAutomation ->
                automation.packageName
                    ?.let { packageName -> OpenDisplayGeoUriOutput(packageName, geoUriFormatter) }

            is OpenDisplayMagicEarthUriAutomation ->
                automation.packageName
                    ?.let { packageName -> OpenDisplayMagicEarthUriOutput(packageName, magicEarthUriFormatter) }

            is OpenNavigationGoogleUriAutomation ->
                automation.packageName
                    ?.let { packageName -> OpenNavigationGoogleUriOutput(packageName, googleMapsUriFormatter) }

            is OpenNavigationMagicEarthUriAutomation ->
                automation.packageName
                    ?.let { packageName -> OpenNavigationMagicEarthUriOutput(packageName, magicEarthUriFormatter) }

            is OpenStreetViewGoogleUriAutomation ->
                automation.packageName
                    ?.let { packageName -> OpenStreetViewGoogleUriOutput(packageName, googleMapsUriFormatter) }

            is OpenPointsGpxAutomation ->
                automation.packageName
                    ?.let { packageName -> OpenPointsGpxOutput(packageName, gpxFormatter) }

            is OpenRouteGpxAutomation ->
                automation.packageName
                    ?.let { packageName -> OpenRouteGpxOutput(packageName, gpxFormatter) }

            is OpenRouteOnePointGpxAutomation ->
                automation.packageName
                    ?.let { packageName -> OpenRouteOnePointGpxOutput(packageName, gpxFormatter) }

            is SavePointGpxAutomation ->
                SavePointGpxOutput(gpxFormatter)

            is SavePointsGpxAutomation ->
                SavePointsGpxOutput(gpxFormatter)

            is SaveRouteGpxAutomation ->
                SaveRouteGpxOutput(gpxFormatter)

            is ShareDisplayGeoUriAutomation ->
                ShareDisplayGeoUriOutput(geoUriFormatter)

            is ShareLinkUriAutomation ->
                getLinkByUUID(automation.linkUUID)
                    ?.let { link -> ShareLinkUriOutput(link, uriFormatter) }

            is ShareRouteGpxAutomation ->
                ShareRouteGpxOutput(gpxFormatter)

            is SharePointsGpxAutomation ->
                SharePointsGpxOutput(gpxFormatter)

            is ShareNavigationGoogleUriAutomation ->
                ShareNavigationGoogleUriOutput(googleMapsUriFormatter)

            is ShareStreetViewGoogleUriAutomation ->
                ShareStreetViewGoogleUriOutput(googleMapsUriFormatter)
        }
}

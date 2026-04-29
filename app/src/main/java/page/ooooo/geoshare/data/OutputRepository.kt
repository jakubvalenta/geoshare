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
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.DataTypes
import page.ooooo.geoshare.lib.geo.CoordinateConverter
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
import page.ooooo.geoshare.lib.outputs.SavePointToContactOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.SaveRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.SendPointOutput
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
    private val coordinateConverter: CoordinateConverter,
) {
    fun getOutputsForPoint(links: List<Link>): List<PointOutput> =
        listOf(
            CopyCoordsDecOutput(coordinateConverter),
            CopyCoordsDegMinSecOutput(coordinateConverter),
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

    fun getOutputsForApps(apps: DataTypes, hiddenApps: Set<String>?): Map<String, List<Output>> =
        apps.filterKeys { hiddenApps?.contains(it) != true }.mapValues { (packageName, dataTypes) ->
            buildList {
                if (DataType.GEO_URI in dataTypes) {
                    add(OpenDisplayGeoUriOutput(packageName, coordinateConverter))
                }
                if (DataType.MAGIC_EARTH_URI in dataTypes) {
                    add(OpenDisplayMagicEarthUriOutput(packageName, coordinateConverter))
                    add(OpenNavigationMagicEarthUriOutput(packageName, coordinateConverter))
                }
                if (DataType.GOOGLE_NAVIGATION_URI in dataTypes) {
                    add(OpenNavigationGoogleUriOutput(packageName, coordinateConverter))
                }
                if (DataType.GOOGLE_STREET_VIEW_URI in dataTypes) {
                    add(OpenStreetViewGoogleUriOutput(packageName, coordinateConverter))
                }
                if (DataType.GPX_DATA in dataTypes) {
                    add(OpenRouteGpxOutput(packageName, coordinateConverter))
                    add(OpenPointsGpxOutput(packageName, coordinateConverter))
                }
                if (DataType.GPX_ONE_POINT_DATA in dataTypes) {
                    add(OpenRouteOnePointGpxOutput(packageName, coordinateConverter))
                }
                if (DataType.SEND_PLAIN_TEXT in dataTypes) {
                    add(SendPointOutput(packageName, coordinateConverter))
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
            *links.filter { it.chipEnabled }.sortedBy { it.name }
                .map { CopyLinkUriOutput(it, coordinateConverter) }
                .toTypedArray(),
        )

    fun getOutputsForPointsChips(): List<PointsOutput> =
        listOf(
            ShareRouteGpxOutput(coordinateConverter),
            SaveRouteGpxOutput(coordinateConverter),
            SavePointsGpxOutput(coordinateConverter),
        )

    suspend fun getAutomationOutput(automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?): Output? =
        when (automation) {
            is CopyCoordsDecAutomation ->
                CopyCoordsDecOutput(coordinateConverter)

            is CopyCoordsDegMinSecAutomation ->
                CopyCoordsDegMinSecOutput(coordinateConverter)

            is CopyGeoUriAutomation ->
                CopyGeoUriOutput(coordinateConverter)

            is CopyLinkUriAutomation ->
                getLinkByUUID(automation.linkUUID)?.let { link ->
                    CopyLinkUriOutput(link, coordinateConverter)
                }

            is CopyLinkDisplayAppleMapsUriAutomation ->
                getLinkByUUID(UUID.fromString("ce900ea1-2c5d-4641-82f3-a5429a68d603"))?.let { link ->
                    CopyLinkUriOutput(link, coordinateConverter)
                }

            is CopyLinkDisplayGoogleMapsUriAutomation ->
                getLinkByUUID(UUID.fromString("7bd96da4-beba-4a30-9dbd-b437a49a1dc0"))?.let { link ->
                    CopyLinkUriOutput(link, coordinateConverter)
                }

            is CopyLinkDisplayMagicEarthUriAutomation ->
                getLinkByUUID(UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07"))?.let { link ->
                    CopyLinkUriOutput(link, coordinateConverter)
                }

            is CopyLinkNavigationAppleMapsUriAutomation ->
                getLinkByUUID(UUID.fromString("a5092c63-cf5c-4225-9059-e888ae12e215"))?.let { link ->
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

            is NoopAutomation ->
                NoopOutput()

            is OpenDisplayGeoUriAutomation ->
                automation.packageName?.let { packageName ->
                    OpenDisplayGeoUriOutput(packageName, coordinateConverter)
                }

            is OpenDisplayMagicEarthUriAutomation ->
                automation.packageName?.let { packageName ->
                    OpenDisplayMagicEarthUriOutput(packageName, coordinateConverter)
                }

            is OpenNavigationGoogleUriAutomation ->
                automation.packageName?.let { packageName ->
                    OpenNavigationGoogleUriOutput(packageName, coordinateConverter)
                }

            is OpenNavigationMagicEarthUriAutomation ->
                automation.packageName?.let { packageName ->
                    OpenNavigationMagicEarthUriOutput(packageName, coordinateConverter)
                }

            is OpenStreetViewGoogleUriAutomation ->
                automation.packageName?.let { packageName ->
                    OpenStreetViewGoogleUriOutput(packageName, coordinateConverter)
                }

            is OpenPointsGpxAutomation ->
                automation.packageName?.let { packageName ->
                    OpenPointsGpxOutput(packageName, coordinateConverter)
                }

            is OpenRouteGpxAutomation ->
                automation.packageName?.let { packageName ->
                    OpenRouteGpxOutput(packageName, coordinateConverter)
                }

            is OpenRouteOnePointGpxAutomation ->
                automation.packageName?.let { packageName ->
                    OpenRouteOnePointGpxOutput(packageName, coordinateConverter)
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
                automation.packageName?.let { packageName ->
                    SendPointOutput(packageName, coordinateConverter)
                }

            is ShareDisplayGeoUriAutomation ->
                ShareDisplayGeoUriOutput(coordinateConverter)

            is ShareLinkUriAutomation ->
                getLinkByUUID(automation.linkUUID)?.let { link ->
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
}

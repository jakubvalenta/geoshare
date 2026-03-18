package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.DataTypes

fun getOutputsForPoint(links: List<Link>): List<PointOutput> =
    listOf(
        CopyCoordsDecOutput,
        CopyCoordsDegMinSecOutput,
        CopyGeoUriOutput,
        *links
            .filter { it.sheetEnabled }
            .groupBy { it.groupOrName }
            .toSortedMap()
            .values
            .flatten()
            .map { CopyLinkUriOutput(it) }
            .toTypedArray(),
        ShareDisplayGeoUriOutput,
        ShareNavigationGoogleUriOutput,
        ShareStreetViewGoogleUriOutput,
        SavePointGpxOutput,
    )

fun getOutputsForPoints(): List<PointsOutput> =
    listOf(
        ShareRouteGpxOutput,
        SharePointsGpxOutput,
        SaveRouteGpxOutput,
        SavePointsGpxOutput,
    )

fun getOutputsForApps(apps: DataTypes, hiddenApps: Set<String>?): Map<String, List<Output>> =
    apps.filterKeys { hiddenApps?.contains(it) != true }.mapValues { (packageName, dataTypes) ->
        buildList {
            if (DataType.GEO_URI in dataTypes) {
                add(OpenDisplayGeoUriOutput(packageName))
            }
            if (DataType.MAGIC_EARTH_URI in dataTypes) {
                add(OpenDisplayMagicEarthUriOutput(packageName))
                add(OpenNavigationMagicEarthUriOutput(packageName))
            }
            if (DataType.GOOGLE_NAVIGATION_URI in dataTypes) {
                add(OpenNavigationGoogleUriOutput(packageName))
            }
            if (DataType.GOOGLE_STREET_VIEW_URI in dataTypes) {
                add(OpenStreetViewGoogleUriOutput(packageName))
            }
            if (DataType.GPX_DATA in dataTypes) {
                add(OpenRouteGpxOutput(packageName))
                add(OpenPointsGpxOutput(packageName))
            }
            if (DataType.GPX_ONE_POINT_DATA in dataTypes) {
                add(OpenRouteOnePointGpxOutput(packageName))
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
                *links.map { ShareLinkUriOutput(it) }.toTypedArray(),
                *links.map { CopyLinkUriOutput(it) }.toTypedArray(),
            )
        }

fun getOutputsForSharing(): List<Output> =
    listOf(
        ShareDisplayGeoUriOutput,
        ShareNavigationGoogleUriOutput,
        ShareStreetViewGoogleUriOutput,
        ShareRouteGpxOutput,
        SharePointsGpxOutput,
    )

fun getOutputsForPointChips(links: List<Link>): List<PointOutput> =
    listOf(
        CopyGeoUriOutput,
        *links.filter { it.chipEnabled }.sortedBy { it.name }.map { CopyLinkUriOutput(it) }.toTypedArray(),
    )

fun getOutputsForPointsChips(): List<PointsOutput> =
    listOf(
        ShareRouteGpxOutput,
        SaveRouteGpxOutput,
        SavePointsGpxOutput,
    )

package page.ooooo.geoshare.lib.formatters

import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AMAP_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.BAIDU_MAP_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.COMAPS_PACKAGE_NAME_PREFIX
import page.ooooo.geoshare.lib.android.GARMIN_PACKAGE_NAME_PREFIX
import page.ooooo.geoshare.lib.android.GMAPS_WV_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.HERE_WEGO_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.KOMOOT_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.LOCUS_MAP_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.MAPS_ME_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.MAPY_COM_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.OEFFI_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.ORGANIC_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.OSMAND_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.OSMAND_PLUS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.SYGIC_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.VESPUCCI_PACKAGE_NAME
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Srs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoUriFormatter @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) {
    data class Flavor(
        val srs: Srs = Srs.WGS84,
        val pin: PinFlavor,
        val zoom: ZoomFlavor,
    ) {
        enum class PinFlavor {
            /**
             * Pin coords are in the 'q' param with name in parentheses, e.g. 'geo:50.123,-11.123?q=50.123,-11.123(foo%20bar)'
             */
            COORDS_AND_NAME_IN_Q,

            /**
             * Pin coords are in the 'q' param, pin name is not supported, e.g. 'geo:50.123,-11.123?q=50.123,-11.123'
             */
            COORDS_ONLY_IN_Q,

            /**
             * Pin name is in the 'q' param, pin coords are supported, e.g. 'geo:50.123,-11.123?q=foo%20bar'
             */
            NAME_ONLY_IN_Q,

            NOT_AVAILABLE,
        }

        enum class ZoomFlavor {
            /**
             * The 'z' param is supported but not when other params are set, e.g. 'geo:50.123,-11.123?z=3.14'
             */
            ALONE_ONLY,

            /**
             * The 'z' param is supported and other params can be set too, e.g. 'geo:50.123,-11.123?z=3.14&q=foo%20bar'
             */
            ANY,

            NOT_AVAILABLE,
        }

        companion object {
            val Safe = Flavor(pin = PinFlavor.NOT_AVAILABLE, zoom = ZoomFlavor.NOT_AVAILABLE)
            val Best = Flavor(pin = PinFlavor.COORDS_AND_NAME_IN_Q, zoom = ZoomFlavor.ANY)
        }
    }

    private fun getFlavor(packageName: String?): Flavor = when {
        packageName == HERE_WEGO_PACKAGE_NAME ||
            packageName == MAPS_ME_PACKAGE_NAME ||
            packageName == MAPY_COM_PACKAGE_NAME ||
            packageName == ORGANIC_MAPS_PACKAGE_NAME ||
            packageName == OSMAND_PACKAGE_NAME ||
            packageName == OSMAND_PLUS_PACKAGE_NAME ||
            packageName == SYGIC_PACKAGE_NAME ||
            packageName == VESPUCCI_PACKAGE_NAME ||
            packageName?.startsWith(COMAPS_PACKAGE_NAME_PREFIX) == true ->
            Flavor.Best

        packageName == AMAP_PACKAGE_NAME ->
            Flavor.Best.copy(srs = Srs.GCJ02_GREATER_CHINA_AND_TAIWAN)

        packageName == GOOGLE_MAPS_PACKAGE_NAME ||
            packageName == GMAPS_WV_PACKAGE_NAME ->
            Flavor.Best.copy(srs = Srs.GCJ02_MAINLAND_CHINA)

        packageName == BAIDU_MAP_PACKAGE_NAME ->
            // Notice that Baidu Map uses WGS 84 geo: URIs, although all its other links are in BD09MC
            Flavor(pin = Flavor.PinFlavor.NOT_AVAILABLE, zoom = Flavor.ZoomFlavor.ALONE_ONLY)

        packageName?.startsWith(GARMIN_PACKAGE_NAME_PREFIX) == true ->
            Flavor(pin = Flavor.PinFlavor.COORDS_AND_NAME_IN_Q, zoom = Flavor.ZoomFlavor.NOT_AVAILABLE)

        packageName == KOMOOT_PACKAGE_NAME ->
            Flavor(pin = Flavor.PinFlavor.COORDS_ONLY_IN_Q, zoom = Flavor.ZoomFlavor.ANY)

        packageName == LOCUS_MAP_PACKAGE_NAME ->
            Flavor(pin = Flavor.PinFlavor.NAME_ONLY_IN_Q, zoom = Flavor.ZoomFlavor.ANY)

        packageName == OEFFI_PACKAGE_NAME ->
            Flavor(pin = Flavor.PinFlavor.COORDS_ONLY_IN_Q, zoom = Flavor.ZoomFlavor.ANY)

        else ->
            Flavor.Safe
    }

    fun formatGeoUriString(point: Point, flavor: Flavor = Flavor.Safe, uriQuote: UriQuote = DefaultUriQuote) =
        coordinateConverter.toSrs(point, flavor.srs).run {
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
                    val z = zStr
                    val q = latStr?.let { latStr ->
                        lonStr?.let { lonStr ->
                            when (flavor.pin) {
                                Flavor.PinFlavor.COORDS_AND_NAME_IN_Q -> "$latStr,$lonStr${name?.let { "($it)" } ?: ""}"
                                Flavor.PinFlavor.COORDS_ONLY_IN_Q -> "$latStr,$lonStr"
                                Flavor.PinFlavor.NAME_ONLY_IN_Q -> name
                                Flavor.PinFlavor.NOT_AVAILABLE -> null
                            }
                        }
                    }
                        ?: if (latStr == null || lonStr == null) {
                            name
                        } else {
                            null
                        }
                    // It's important that the 'z' param comes before 'q', because some map apps require the name (which
                    // can be part of 'q') to be at the very end of the URI.
                    if (z != null) {
                        when (flavor.zoom) {
                            Flavor.ZoomFlavor.ALONE_ONLY -> if (q == null) set("z", z)
                            Flavor.ZoomFlavor.ANY -> set("z", z)
                            Flavor.ZoomFlavor.NOT_AVAILABLE -> {}
                        }
                    }
                    if (q != null) {
                        set("q", q)
                    }
                }
                    .takeIf { it.isNotEmpty() }
                    ?.let { Uri.formatQueryParams(it.toImmutableMap(), allow = ",()", uriQuote = uriQuote) }
                    ?.let { append("?$it") }
            }
        }

    fun formatGeoUriString(point: Point, packageName: String?, uriQuote: UriQuote) =
        formatGeoUriString(point, getFlavor(packageName), uriQuote)
}

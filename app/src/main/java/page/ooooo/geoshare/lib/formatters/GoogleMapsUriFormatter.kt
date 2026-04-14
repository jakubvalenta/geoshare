package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AMAP_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.GMAPS_WV_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Srs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsUriFormatter @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) {
    // TODO Check Google Maps and Amap navigation
    fun formatNavigationUriString(point: Point, srs: Srs = Srs.WGS84, uriQuote: UriQuote) =
        coordinateConverter.toSrs(point, srs).run {
            Uri(
                scheme = "google.navigation",
                path = (latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        "$latStr,$lonStr"
                    }
                } ?: name ?: "0,0").let { "q=$it" },
                uriQuote = uriQuote,
            ).toString()
        }

    fun formatNavigationUriString(point: Point, packageName: String?, uriQuote: UriQuote) =
        formatNavigationUriString(point, getSrs(packageName), uriQuote)

    // TODO Check Google Maps and Amap Street View
    fun formatStreetViewUriString(point: Point, srs: Srs = Srs.WGS84, uriQuote: UriQuote) =
        coordinateConverter.toSrs(point, srs).run {
            Uri(
                scheme = "google.streetview",
                path = (latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        "$latStr,$lonStr"
                    }
                } ?: "0,0").let { @Suppress("SpellCheckingInspection") "cbll=$it" },
                uriQuote = uriQuote,
            ).toString()
        }

    fun formatStreetViewUriString(point: Point, packageName: String?, uriQuote: UriQuote) =
        formatStreetViewUriString(point, getSrs(packageName), uriQuote)

    // TODO Merge with [GeoUriFormatter.getFlavor] if all apps use the same SRS for geo, google.navigation, and google.streetview links
    private fun getSrs(packageName: String?): Srs =
        when (packageName) {
            AMAP_PACKAGE_NAME -> Srs.GCJ02_GREATER_CHINA_AND_TAIWAN
            GOOGLE_MAPS_PACKAGE_NAME, GMAPS_WV_PACKAGE_NAME -> Srs.GCJ02_MAINLAND_CHINA
            else -> Srs.WGS84
        }
}

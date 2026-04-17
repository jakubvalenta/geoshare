package page.ooooo.geoshare.lib.formatters

import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Point

object GeoUriFormatter {
    fun formatGeoUriString(
        point: Point,
        flavor: GeoUriFlavor = GeoUriFlavor.Safe,
        uriQuote: UriQuote = DefaultUriQuote,
    ) =
        point.run {
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
                                GeoUriFlavor.PinFlavor.COORDS_AND_NAME_IN_Q -> "$latStr,$lonStr${name?.let { "($it)" } ?: ""}"
                                GeoUriFlavor.PinFlavor.COORDS_ONLY_IN_Q -> "$latStr,$lonStr"
                                GeoUriFlavor.PinFlavor.NAME_ONLY_IN_Q -> name
                                GeoUriFlavor.PinFlavor.NOT_AVAILABLE -> null
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
                            GeoUriFlavor.ZoomFlavor.ALONE_ONLY -> if (q == null) set("z", z)
                            GeoUriFlavor.ZoomFlavor.ANY -> set("z", z)
                            GeoUriFlavor.ZoomFlavor.NOT_AVAILABLE -> {}
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
}

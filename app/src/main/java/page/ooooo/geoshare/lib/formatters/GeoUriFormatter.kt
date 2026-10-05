package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.uri.DefaultUriQuote
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import page.ooooo.geoshare.lib.uri.format
import page.ooooo.geoshare.lib.uri.toQueryParams

object GeoUriFormatter {
    fun formatGeoUriString(
        point: Point,
        flavor: GeoUriFlavor = GeoUriFlavor.Safe,
        uriQuote: UriQuote = DefaultUriQuote,
    ): String =
        point.run {
            // Use custom string builder instead of Uri.toString(), because we want to allow custom chars in query params
            buildString {
                append("geo:")
                append(
                    Uri.formatPathPart(
                        latStr?.let { latStr ->
                            lonStr?.let { lonStr ->
                                "$latStr,$lonStr"
                            }
                        } ?: "0,0",
                        uriQuote = uriQuote,
                    )
                )
                buildList {
                    val z = zStr
                    val q = latStr?.let { latStr ->
                        lonStr?.let { lonStr ->
                            when (flavor.pin) {
                                GeoUriFlavor.PinFlavor.COORDS_AND_NAME_IN_Q ->
                                    "$latStr,$lonStr${name?.let { "($it)" }.orEmpty()}"

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
                            GeoUriFlavor.ZoomFlavor.ALONE_ONLY -> if (q == null) add("z" to z)
                            GeoUriFlavor.ZoomFlavor.ANY -> add("z" to z)
                            GeoUriFlavor.ZoomFlavor.NOT_AVAILABLE -> {}
                        }
                    }
                    if (q != null) {
                        add("q" to q)
                    }
                }
                    .takeIf { it.isNotEmpty() }
                    ?.toQueryParams()
                    ?.format(allow = ",()", uriQuote = uriQuote)
                    ?.let { append("?$it") }
            }
        }
}

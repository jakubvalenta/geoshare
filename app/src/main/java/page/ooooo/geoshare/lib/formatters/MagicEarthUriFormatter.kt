package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import page.ooooo.geoshare.lib.uri.toQueryParams

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/, although it's outdated:
 * - drive_via doesn't work
 * - navigate_to doesn't work; use get_directions
 * - navigate_via doesn't work; it was an undocumented parameter that used to work for a while
 * - search_around seems to do the same as open_search
 */
object MagicEarthUriFormatter {
    fun formatDisplayUriString(point: WGS84Point, uriQuote: UriQuote) = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = buildList {
            point.run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        add("show_on_map" to "")
                        add("lat" to latStr)
                        add("lon" to lonStr)
                        name?.let { name ->
                            add("name" to name)
                        }
                        Unit
                    }
                } ?: name?.let { name ->
                    add("open_search" to "")
                    add("q" to name)
                } ?: run {
                    add("show_on_map" to "")
                    add("lat" to "0")
                    add("lon" to "0")
                }
            }
        }.toQueryParams(),
        uriQuote = uriQuote,
    ).toString()

    fun formatNavigationUriString(point: WGS84Point, uriQuote: UriQuote) = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = buildList {
            add("get_directions" to "")
            point.run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        add("lat" to latStr)
                        add("lon" to lonStr)
                    }
                } ?: name?.let { name ->
                    add("q" to name)
                } ?: run {
                    add("lat" to "0")
                    add("lon" to "0")
                }
            }
        }.toQueryParams(),
        uriQuote = uriQuote,
    ).toString()
}

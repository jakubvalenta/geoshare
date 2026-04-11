package page.ooooo.geoshare.lib.formatters

import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import javax.inject.Inject
import javax.inject.Singleton

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/, although it's outdated:
 * - drive_via doesn't work
 * - navigate_to doesn't work; use get_directions
 * - navigate_via doesn't work; it was an undocumented parameter that used to work for a while
 * - search_around seems to do the same as open_search
 */
@Singleton
class MagicEarthUriFormatter @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) {
    fun formatDisplayUriString(point: Point, uriQuote: UriQuote) = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = buildMap {
            coordinateConverter.toWGS84(point).run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        set("show_on_map", "")
                        set("lat", latStr)
                        set("lon", lonStr)
                        name?.let { name ->
                            set("name", name)
                        }
                        Unit
                    }
                } ?: name?.let { name ->
                    set("open_search", "")
                    set("q", name)
                } ?: run {
                    set("show_on_map", "")
                    set("lat", "0")
                    set("lon", "0")
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    fun formatNavigationUriString(point: Point, uriQuote: UriQuote) = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = buildMap {
            set("get_directions", "")
            coordinateConverter.toWGS84(point).run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        set("lat", latStr)
                        set("lon", lonStr)
                    }
                } ?: name?.let { name ->
                    set("q", name)
                } ?: run {
                    set("lat", "0")
                    set("lon", "0")
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()
}

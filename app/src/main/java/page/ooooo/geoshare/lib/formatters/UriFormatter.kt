package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.extensions.toTrimmedString
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Srs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UriFormatter @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) {
    fun formatUriString(
        point: Point,
        coordsUriTemplate: String,
        nameUriTemplate: String = "",
        srs: Srs = Srs.WGS84,
        defaultZ: Double = 16.0,
        uriQuote: UriQuote = DefaultUriQuote,
    ): String? = point.run {
        when (srs) {
            Srs.WGS84 -> coordinateConverter.toWGS84(point)
            Srs.GCJ02 -> coordinateConverter.toGCJ02(point) // FIXME
        }
            .run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        val zOrDefaultStr = (z ?: defaultZ).toScale(7).toTrimmedString()
                        coordsUriTemplate
                            .replace("{lat}", uriQuote.encode(latStr))
                            .replace("{lon}", uriQuote.encode(lonStr))
                            .replace("{z}", uriQuote.encode(zOrDefaultStr))
                            .replace("{name}", uriQuote.encode(cleanName.orEmpty()))
                            .takeIf { it.isNotEmpty() }
                    }
                } ?: cleanName?.let { cleanName ->
                    nameUriTemplate
                        .replace("{q}", uriQuote.encode(cleanName))
                        .takeIf { it.isNotEmpty() }
                }
            }
    }
}

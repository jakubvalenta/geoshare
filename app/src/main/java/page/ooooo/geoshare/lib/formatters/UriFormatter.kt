package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.extensions.toTrimmedString
import page.ooooo.geoshare.lib.geo.Point

object UriFormatter {
    fun formatUriString(
        point: Point,
        coordsUriTemplate: String,
        nameUriTemplate: String = "",
        defaultZ: Double = 16.0,
        uriQuote: UriQuote = DefaultUriQuote,
    ): String? = point.run {
        latStr?.let { latStr ->
            lonStr?.let { lonStr ->
                val zOrDefaultStr = (z ?: defaultZ).toScale(7).toTrimmedString()
                coordsUriTemplate
                    .replace("{lat}", uriQuote.encode(latStr))
                    .replace("{lon}", uriQuote.encode(lonStr))
                    .replace("{z}", uriQuote.encode(zOrDefaultStr))
                    .replace("{name}", uriQuote.encode(cleanName.orEmpty()))
                    .replace(
                        "{plus_code}",
                        if ("{plus_code}" in coordsUriTemplate) {
                            PlusCodeFormatter.formatPlusCode(point)?.let { uriQuote.encode(it) } ?: ""
                        } else {
                            ""
                        },
                    )
                    .takeIf { it.isNotEmpty() }
            }
        } ?: cleanName?.let { cleanName ->
            nameUriTemplate
                .replace("{q}", uriQuote.encode(cleanName))
                .takeIf { it.isNotEmpty() }
        }
    }
}

package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.encodeURI
import page.ooooo.geoshare.lib.geo.Point

object UriFormatter {
    fun formatUriString(
        point: Point,
        coordsUriTemplate: String,
        nameUriTemplate: String = "",
        uriQuote: UriQuote = DefaultUriQuote,
    ): String? = point.run {
        val template = when {
            coordsUriTemplate.isNotEmpty() && hasCoordinates() -> coordsUriTemplate
            nameUriTemplate.isNotEmpty() && hasName() -> nameUriTemplate
            else -> return null
        }
        val q by lazy {
            when {
                cleanName != null -> cleanName
                latStr != null && lonStr != null -> "$latStr,$lonStr"
                else -> {
                    // This branch can't be reached, because we make sure the point has coordinates or name when
                    // selecting the template, but let's keep the branch anyway, in case the template selection
                    // changes in the future.
                    null
                }
            }
        }
        val plusCode by lazy { PlusCodeFormatter.formatPlusCode(this) }
        return template
            .replace("{lat}", latStr?.encodeURI(uriQuote).orEmpty())
            .replace("{lon}", lonStr?.encodeURI(uriQuote).orEmpty())
            .replace("{z}", zOrDefaultStr.encodeURI(uriQuote))
            .replace("{name}", cleanName?.encodeURI(uriQuote).orEmpty())
            .replace("{plus_code}", plusCode?.encodeURI(uriQuote).orEmpty())
            .replace("{q}", q?.encodeURI(uriQuote).orEmpty())
    }
}

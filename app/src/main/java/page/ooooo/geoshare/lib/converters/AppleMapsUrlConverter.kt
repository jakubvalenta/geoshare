package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.htmlPattern
import page.ooooo.geoshare.lib.matcherIfFind
import page.ooooo.geoshare.lib.matcherIfMatches
import page.ooooo.geoshare.lib.uriPattern

class AppleMapsUrlConverter() : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {
    companion object {
        const val NAME = "Apple Maps"

        /**
         * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
         */
        fun formatUriString(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String = Uri(
            scheme = "https",
            host = "maps.apple.com",
            path = "/",
            queryParams = buildMap {
                position.apply {
                    mainPoint?.let { (lat, lon) ->
                        set("ll", "$lat,$lon")
                    } ?: q?.let { q ->
                        set("q", q)
                    }
                    z?.let { z ->
                        set("z", z)
                    }
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
    }

    /**
     * Sets points to zero, so that we avoid parsing HTML for this URI. Because parsing HTML for this URI doesn't work.
     */
    class DoNotParseHtmlPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
        override val points = persistentListOf(Point())
    }

    class DoNotParseHtmlPositionRegex(regex: String) : PositionRegex(regex) {
        override fun matches(input: String) = pattern.matcherIfMatches(input)?.let { DoNotParseHtmlPositionMatch(it) }
        override fun find(input: String) = pattern.matcherIfFind(input)?.let { DoNotParseHtmlPositionMatch(it) }
    }

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?maps\.apple(\.com)?[/?#]\S+""")

    override val documentation = Documentation(
        nameResId = R.string.converter_apple_maps_name,
        inputs = listOf(
            DocumentationInput.Url(18, "https://maps.apple"),
            DocumentationInput.Url(18, "https://maps.apple.com"),
        ),
    )

    override val conversionUriPattern = uriPattern {
        all {
            optional {
                query("z", PositionRegex(Z))
            }
            first {
                all {
                    host(PositionRegex("maps.apple"))
                    path(PositionRegex("/p/.+"))
                }
                query("ll", PositionRegex("$LAT,$LON"))
                query("coordinate", PositionRegex("$LAT,$LON"))
                query("q", PositionRegex("$LAT,$LON"))
                query("address", PositionRegex(Q_PARAM))
                query("name", PositionRegex(Q_PARAM))
                @Suppress("SpellCheckingInspection")
                query("auid", PositionRegex(".+"))
                query("place-id", PositionRegex(".+"))
                all {
                    query("q", PositionRegex(Q_PARAM))
                    query("sll", PositionRegex("$LAT,$LON"))
                }
                query("sll", PositionRegex("$LAT,$LON"))
                query("center", PositionRegex("$LAT,$LON"))
                query("q", DoNotParseHtmlPositionRegex(Q_PARAM))
            }
        }
    }

    override val conversionHtmlPattern = htmlPattern {
        all {
            content(PositionRegex("""<meta property="place:location:latitude" content="$LAT""""))
            content(PositionRegex("""<meta property="place:location:longitude" content="$LON""""))
        }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title
}

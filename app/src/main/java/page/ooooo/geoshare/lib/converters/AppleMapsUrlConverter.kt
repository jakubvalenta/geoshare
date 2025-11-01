package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z

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

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?maps\.apple(\.com)?[/?#]\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_apple_maps_name,
        inputs = listOf(
            DocumentationInput.Url(18, "https://maps.apple"),
            DocumentationInput.Url(18, "https://maps.apple.com"),
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch(it) }
            }
            first {
                on { if (host == "maps.apple") path matches "/p/.+" else null } doReturn { PositionMatch(it) }
                on { queryParams["ll"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                on { queryParams["coordinate"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                on { queryParams["q"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                on { queryParams["address"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
                on { queryParams["name"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
                @Suppress("SpellCheckingInspection")
                on { queryParams["auid"]?.let { it matches ".+" } } doReturn { PositionMatch(it) }
                on { queryParams["place-id"]?.let { it matches ".+" } } doReturn { PositionMatch(it) }
                all {
                    on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
                    on { queryParams["sll"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                }
                on { queryParams["sll"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                on { queryParams["center"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { DoNotParseHtmlPositionMatch(it) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<String, PositionMatch> {
        all {
            on { this find """<meta property="place:location:latitude" content="$LAT"""" } doReturn { PositionMatch(it) }
            on { this find """<meta property="place:location:longitude" content="$LON"""" } doReturn { PositionMatch(it) }
        }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title

    /**
     * Sets points to zero, so that we avoid parsing HTML for this URI. Because parsing HTML for this URI doesn't work.
     */
    private class DoNotParseHtmlPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
        override val points = persistentListOf(Point())
    }
}

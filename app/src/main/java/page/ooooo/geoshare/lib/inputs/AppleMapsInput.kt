package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs

object AppleMapsInput : Input.HasUri, Input.HasHtml {
    const val NAME = "Apple Maps"

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?maps\.apple(\.com)?[/?#]\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_apple_maps_name,
        inputs = listOf(
            Input.DocumentationInput.Url(18, "https://maps.apple"),
            Input.DocumentationInput.Url(18, "https://maps.apple.com"),
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch(it, srs) }
            }
            first {
                on { if (host == "maps.apple") path matches "/p/.+" else null } doReturn { PositionMatch(it, srs) }
                on { queryParams["ll"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["coordinate"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["q"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["address"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                on { queryParams["name"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                @Suppress("SpellCheckingInspection")
                on { queryParams["auid"]?.let { it matches ".+" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["place-id"]?.let { it matches ".+" } } doReturn { PositionMatch(it, srs) }
                all {
                    on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["sll"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                }
                on { queryParams["sll"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["center"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { DoNotParseHtmlPositionMatch(it, srs) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<String, PositionMatch> {
        all {
            on { this find """<meta property="place:location:latitude" content="$LAT"""" } doReturn {
                PositionMatch(
                    it,
                    srs
                )
            }
            on { this find """<meta property="place:location:longitude" content="$LON"""" } doReturn {
                PositionMatch(
                    it,
                    srs
                )
            }
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
    private class DoNotParseHtmlPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch(matcher, srs) {
        override val points = persistentListOf(Point(srs = srs))
    }
}

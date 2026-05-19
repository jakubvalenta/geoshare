package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenStreetMapApiInput @Inject constructor() : BodyAsTextInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title

    override suspend fun parse(
        data: String,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        // Use a simple regex instead of JSON parsing, because it works fine
        val pattern = Regex(""""lat":$LAT,"lon":$LON""")

        val mutablePoints = mutableListOf<WGS84Point>()
        val name = prevResult?.points?.lastOrNull()?.name

        mutablePoints.addAll(
            pattern.findAll(data)
                .mapNotNull { m -> m.toLatLonPoint(Source.API)?.let { WGS84Point(it) } }
        )

        if (name != null) {
            mutablePoints.removeLastOrNull()?.let { lastPoint ->
                mutablePoints.add(lastPoint.copy(name = name))
            }
        }

        points = mutablePoints.toImmutableList()
    }

    override fun toString() = "OpenStreetMapApiInput"
}

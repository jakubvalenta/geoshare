package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.extensions.doubleGroupOrNull
import page.ooooo.geoshare.data.extensions.groupOrNull
import page.ooooo.geoshare.data.extensions.matchEntire
import page.ooooo.geoshare.data.extensions.toLatLonNamePoint
import page.ooooo.geoshare.data.extensions.toLatLonPoint
import page.ooooo.geoshare.data.formatters.GeoUriFormatter
import page.ooooo.geoshare.data.formatters.UriFormatter
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.uri.Uri
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoUriInput @Inject constructor(
    private val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomSource {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = GeoUriInputGroup
    override val changelog = persistentListOf(
        InputChangelogItem.Text(3) {
            stringResource(
                R.string.example,
                GeoUriFormatter.formatGeoUriString(WGS84Point(NaivePoint.example))
            )
        },
    )

    override val pattern = Regex("""(geo:$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                val z = Z_PATTERN.matchEntire(queryParams["z"]?.firstOrNull())?.doubleGroupOrNull()

                // Name in separate query param
                // ?q=...&({name})
                val name = queryParams
                    .filter { (key, value) -> key != "q" && key != "z" && value.firstOrNull().isNullOrEmpty() }
                    .firstNotNullOfOrNull { (key) -> Regex(NAME_REGEX).matchEntire(key)?.groupOrNull() }

                // Pin without name
                // ?q={lat},{lon}
                // Pin with name
                // ?q={lat},{lon}({name})
                Regex("""$LAT$COORD_SEP$LON\s?(?:$NAME_REGEX)?.*""").matchEntire(queryParams["q"]?.firstOrNull())
                    ?.toLatLonNamePoint(Source.URI)?.let {
                        points = persistentListOf(WGS84Point(it, z, name))
                        return@parseResult
                    }

                // Query unless it contained coordinates
                // ?q={name}
                val query = Q_PARAM_PATTERN.matchEntire(queryParams["q"]?.firstOrNull())?.groupOrNull()

                // Coordinates
                // geo:{lat},{lon}
                Regex("""$LAT_LON_PATTERN.*""").matchEntire(pathParts.firstOrNull())?.toLatLonPoint(Source.URI)?.let {
                    points = persistentListOf(WGS84Point(it, z, name ?: query))
                    return@parseResult
                }

                if (name != null || query != null) {
                    points = persistentListOf(WGS84Point(z = z, name = name ?: query, source = Source.URI))
                }
            }
        }

    override fun genRandomSource(point: Point) =
        UriFormatter.formatUriString(point, "geo:{lat},{lon}?z={z}&q={lat},{lon}({name})")

    override fun toString() = "GeoUriInput"

    private companion object {
        private const val NAME_REGEX = """\((.+)\)"""
    }
}

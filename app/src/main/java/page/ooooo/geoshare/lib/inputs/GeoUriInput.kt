package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.matchLatLonZ
import page.ooooo.geoshare.lib.extensions.matchQ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.extensions.toLatLon
import page.ooooo.geoshare.lib.outputs.GeoUriOutput
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

object GeoUriInput : Input {
    private const val NAME_REGEX = """(\((?P<name>.+)\))"""

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""geo:\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_geo_name, inputs = listOf(
            Input.DocumentationInput.Text(3) {
                stringResource(
                    R.string.example,
                    GeoUriOutput.formatUriString(
                        Position.example,
                        null,
                        Srs.WGS84,
                        nameDisabled = false,
                        zoomDisabled = false,
                    ),
                )
            },
        )
    )

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            ("""$LAT,$LON$NAME_REGEX?""" match queryParams["q"])?.let { m ->
                setPointIfNull { m.toLatLon()?.let { (lat, lon) -> LatLonZ(lat, lon, null) } }
                setQOrNameIfEmpty { m.groupOrNull("name") }
            }
            setQOrNameIfEmpty {
                queryParams.firstNotNullOfOrNull { (key, value) ->
                    if (key != "q" && key != "z" && value.isEmpty()) {
                        (NAME_REGEX match key)?.groupOrNull("name")
                    } else {
                        null
                    }
                }
            }
            setQIfNull { Q_PARAM_PATTERN matchQ queryParams["q"] }
            setPointIfNull { LAT_LON_PATTERN matchLatLonZ path }
            setZIfNull { Z_PATTERN matchZ queryParams["z"] }
        }.toPair()
    }
}

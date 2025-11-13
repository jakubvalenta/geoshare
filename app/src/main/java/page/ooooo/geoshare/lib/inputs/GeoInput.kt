package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.outputs.GeoUriOutputGroup
import page.ooooo.geoshare.lib.position.*

object GeoInput : Input {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""geo:\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_geo_name, inputs = listOf(
            Input.DocumentationInput.Text(3) {
                stringResource(R.string.example, GeoUriOutputGroup.formatUriString(Position.example, Srs.WGS84))
            },
        )
    )

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setPointFromMatcher { LAT_LON_PATTERN match path }
            setQueryFromMatcher { Q_PARAM_PATTERN match queryParams["q"] }
            setZoomFromMatcher { Z_PATTERN match queryParams["z"] }
        }
    }
}

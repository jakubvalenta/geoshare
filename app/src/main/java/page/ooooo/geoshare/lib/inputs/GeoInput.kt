package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matchLatLonZ
import page.ooooo.geoshare.lib.extensions.matchQ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.outputs.GeoUriOutputGroup
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

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
            setQIfEmpty { Q_PARAM_PATTERN matchQ queryParams["q"] }
            setPointIfEmpty { LAT_LON_PATTERN matchLatLonZ path }
            setZIfEmpty { Z_PATTERN matchZ queryParams["z"] }
        }.toPair()
    }
}

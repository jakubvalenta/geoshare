package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.geo.decodeGe0Hash
import page.ooooo.geoshare.lib.extensions.matchHash
import page.ooooo.geoshare.lib.extensions.matchQ
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

object MapsMeInput : Input {
    private const val HASH = """(?P<hash>[A-Za-z0-9\-_]{2,})"""

    private val srs = Srs.WGS84

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""((https?://)?(comaps\.at|ge0\.me|omaps\.app)|ge0:/)/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_ge0_name,
        inputs = listOf(
            Input.DocumentationInput.Url(25, "http://ge0.me/"),
            Input.DocumentationInput.Url(25, "https://omaps.app/"),
            Input.DocumentationInput.Url(25, "https://comaps.at/"),
        ),
    )

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setPointIfNull {
                (HASH matchHash if (scheme == "ge0") host else pathParts.getOrNull(1))
                    ?.let { hash -> decodeGe0Hash(hash) }
                    ?.let { (lat, lon, z) -> LatLonZ(lat.toScale(7), lon.toScale(7), z) }

            }
            setQOrNameIfEmpty {
                (Q_PATH_PATTERN matchQ if (scheme == "ge0") pathParts.getOrNull(1) else pathParts.getOrNull(2))
                    ?.replace('_', ' ')
            }
        }.toPair()
    }
}

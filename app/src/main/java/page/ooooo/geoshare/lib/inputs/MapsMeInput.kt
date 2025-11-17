package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.decodeGe0Hash
import page.ooooo.geoshare.lib.extensions.matchHash
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
                // TODO Add support for http://ge0.me/AbCMCNp0LO/Madagascar
                (if (scheme == "ge0") HASH matchHash host else """/$HASH\S*""" matchHash path)
                    ?.let { hash -> decodeGe0Hash(hash) }
                    ?.let { (lat, lon, z) -> LatLonZ(lat.toScale(7), lon.toScale(7), z) }
            }
        }.toPair()
    }
}

package page.ooooo.geoshare.lib.inputs

import android.R.attr.path
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.position.Srs

object Ge0Input : Input.HasUri {
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

    override val conversionUriPattern = conversionPattern<Uri, IncompletePosition> {
        on {
            (if (scheme == "ge0") host matches HASH else path matches """/$HASH\S*""")?.groupOrNull("hash")
                ?.let { hash ->
                    decodeGe0Hash(hash).let { (lat, lon, z) ->
                        IncompletePosition(
                            srs,
                            lat = lat.toScale(7),
                            lon = lon.toScale(7),
                            z = z,
                        )
                    }
                }
        }
    }
}

package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.*
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findLatLonZ
import page.ooooo.geoshare.lib.extensions.matchLatLonZ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

object UrbiInput : Input.HasHtml {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern =
        Pattern.compile("""(https?://)?(www\.)?((go|maps)\.)?(2gis|urbi|urbi-[a-z]{2})(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_urbi_name,
        inputs = listOf(
            Input.DocumentationInput.Url(27, "https://2gis.ae/"),
            Input.DocumentationInput.Url(27, "https://2gis.am/"),
            Input.DocumentationInput.Url(27, "https://2gis.az/"),
            Input.DocumentationInput.Url(27, "https://2gis.cl/"),
            Input.DocumentationInput.Url(27, "https://2gis.com.cy/"),
            Input.DocumentationInput.Url(27, "https://2gis.com/"),
            Input.DocumentationInput.Url(27, "https://2gis.cz/"),
            Input.DocumentationInput.Url(27, "https://2gis.it/"),
            Input.DocumentationInput.Url(27, "https://2gis.kg/"),
            Input.DocumentationInput.Url(27, "https://2gis.kz/"),
            Input.DocumentationInput.Url(27, "https://2gis.ru/"),
            Input.DocumentationInput.Url(27, "https://2gis.uz/"),
            Input.DocumentationInput.Url(27, "https://go.2gis.com/"),
            Input.DocumentationInput.Url(27, "https://go.urbi.ae/"),
            Input.DocumentationInput.Url(27, "https://maps.urbi.ae/"),
            Input.DocumentationInput.Url(27, "https://urbi-bh.com/"),
            Input.DocumentationInput.Url(27, "https://urbi-eg.com/"),
            Input.DocumentationInput.Url(27, "https://urbi-kw.com/"),
            Input.DocumentationInput.Url(27, "https://urbi-om.com/"),
            Input.DocumentationInput.Url(27, "https://urbi-qa.com/"),
            Input.DocumentationInput.Url(27, "https://urbi-sa.com/"),
            Input.DocumentationInput.Url(27, "https://urbi.bh/"),
            Input.DocumentationInput.Url(27, "https://urbi.qa/"),
        ),
    )

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setPointIfNull { """$LON,$LAT/$Z""" matchLatLonZ queryParams["m"] }
            setPointIfNull { """.*/$LON,$LAT/?$""" matchLatLonZ path }
            setPointIfNull { LON_LAT_PATTERN matchLatLonZ queryParams["center"] }
            setZIfNull { Z_PATTERN matchZ queryParams["zoom"] }
            setUriStringIfNull { uri.toString() }
        }.toPair()
    }

    override suspend fun parseHtml(channel: ByteReadChannel) =
        PositionBuilder(srs).apply {
            val pattern = Pattern.compile("""zoom=$Z&amp;center=$LON%2C$LAT""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                (pattern findLatLonZ line)?.let { (lat, lon, z) ->
                    setPointIfNull { LatLonZ(lat, lon, z) }
                    break
                }
            }
        }.toPair()

    @StringRes
    override val permissionTitleResId = R.string.converter_urbi_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_urbi_loading_indicator_title
}

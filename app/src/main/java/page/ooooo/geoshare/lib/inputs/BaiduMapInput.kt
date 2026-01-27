package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.github._46319943.bd09convertor.BD09Convertor
import com.github.wandergis.coordtransform.CoordTransform
import com.google.re2j.Pattern
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findLatLonZ
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.buildPosition

object BaiduMapInput : Input.HasHtml {
    private val srs = Srs.GCJ02

    override val uriPattern: Pattern = Pattern.compile("""https://j.map.baidu.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.BAIDU_MAP,
        nameResId = R.string.converter_baidu_map_name,
        items = listOf(
            InputDocumentationItem.Url(33, "https://j.map.baidu.com"),
        ),
    )

    override suspend fun parseUri(uri: Uri) = ParseUriResult.from(Position(srs), htmlUriString = uri.toString())

    override suspend fun parseHtml(channel: ByteReadChannel, positionFromUri: Position, log: ILog): ParseHtmlResult? {
        val positionFromHtml = buildPosition(srs) {
            val pattern = Pattern.compile("""\\"geo\\":\\"1\|(?P<lon>\d+\.\d+),(?P<lat>\d+\.\d+)""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                if (
                    setPointIfNull {
                        (pattern findLatLonZ line)
                            ?.let { (y, x) -> BD09Convertor.convertMC2LL(y, x) }
                            ?.let { (bd09Lat, bd09Lon) -> CoordTransform.bd09toGCJ02(bd09Lat, bd09Lon) }
                            ?.let { (lat, lon) -> LatLonZ(lat, lon, null) }
                    }
                ) {
                    break
                }
            }
        }
        return ParseHtmlResult.from(positionFromUri, positionFromHtml)
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_baidu_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_baidu_map_loading_indicator_title
}

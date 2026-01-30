package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findAllNaivePoint
import page.ooooo.geoshare.lib.extensions.findNaivePoint
import page.ooooo.geoshare.lib.extensions.matchNaivePoint
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asBD09MC
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseUriResult

object BaiduMapInput : Input {
    private const val X = """(?P<lon>\d+(\.\d+)?)"""
    private const val Y = """(?P<lat>\d+(\.\d+)?)"""
    private const val CENTER = """@$X,$Y,${Z}z.*"""
    private const val WAYPOINT = """1\$\$\$\$$X,$Y\$\$(?P<name>[^\$]+)"""

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?map\.baidu\.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.BAIDU_MAP,
        nameResId = R.string.converter_baidu_map_name,
        items = listOf(
            InputDocumentationItem.Url(33, "https://map.baidu.com"),
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                val parts = uri.pathParts.drop(1)
                val firstPart = parts.firstOrNull() ?: return@run
                if (firstPart.startsWith('@')) {
                    // Center
                    // https://map.baidu.com/@<CENTER_X>,<CENTER_Y>,<CENTER_Z>
                    (CENTER matchNaivePoint firstPart)?.also { points.add(it) }

                } else if (firstPart == "poi") {
                    // Place
                    // https://map.baidu.com/poi/<NAME>/@<X>,<Y>,<Z>
                    (CENTER matchNaivePoint parts.getOrNull(2))
                        ?.also { points.add(it.copy(name = parts.getOrNull(1))) }

                } else if (firstPart == "dir") {
                    // Directions
                    // https://map.baidu.com/dir/...?sn=<START_POINT>&en=<WAYPOINT_POINT>$$1$$%20to:<DEST_POINT>
                    (WAYPOINT findNaivePoint queryParams["sn"])?.also { points.add(it) }
                    points.addAll(WAYPOINT findAllNaivePoint queryParams["en"])

                    // Directions without params
                    // https://map.baidu.com/dir/<START_NAME>/<WAYPOINT_NAME>/<DEST_NAME>/@<CENTER_X>,<CENTER_Y>,<CENTER_Z>z
                    if (points.isEmpty()) {
                        parts
                            .drop(1)
                            .filterNot { it.startsWith('@') }
                            .map { NaivePoint(0.0, 0.0, name = it) }
                            .map { points.add(it) }
                    }
                }
            }
        }
            .asBD09MC()
            .toParseUriResult()
}

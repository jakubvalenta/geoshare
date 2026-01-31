package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatNamePoint
import page.ooooo.geoshare.lib.extensions.toLonLatZPoint
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asBD09MC
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseUriResult

object BaiduMapInput : Input {
    private const val X = """(\d+(?:\.\d+)?)"""
    private const val Y = """(\d+(?:\.\d+)?)"""
    private const val CENTER = """@$X,$Y,${Z}z.*"""
    private const val WAYPOINT = """1\$\$\$\$$X,$Y\$\$([^$]+)"""

    override val uriPattern = Regex("""(?:https?://)?map\.baidu\.com/\S+""")
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
                    Regex(CENTER).matchEntire(firstPart)?.toLonLatZPoint()?.also { points.add(it) }

                } else if (firstPart == "poi") {
                    // Place
                    // https://map.baidu.com/poi/<NAME>/@<X>,<Y>,<Z>
                    Regex(CENTER).matchEntire(parts.getOrNull(2))
                        ?.toLonLatZPoint()
                        ?.also { points.add(it.copy(name = parts.getOrNull(1))) }

                } else if (firstPart == "dir") {
                    // Directions
                    // https://map.baidu.com/dir/...?sn=<START_POINT>&en=<WAYPOINT_POINT>$$1$$%20to:<DEST_POINT>
                    val pattern = Regex(WAYPOINT)
                    pattern.find(queryParams["sn"])?.toLonLatNamePoint()?.also { points.add(it) }
                    points.addAll((pattern.findAll(queryParams["en"])).mapNotNull { it.toLonLatNamePoint() })

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

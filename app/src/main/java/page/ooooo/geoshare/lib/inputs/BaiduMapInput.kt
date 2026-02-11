package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
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

object BaiduMapInput : Input.HasShortUri, Input.HasWeb {
    private const val X = """(\d+(?:\.\d+)?)"""
    private const val Y = """(\d+(?:\.\d+)?)"""
    private const val CENTER = """@$X,$Y,${Z}z.*"""
    private const val WAYPOINT = """1\$\$\$\$$X,$Y\$\$([^$]+)"""

    override val uriPattern = Regex("""(?:https?://)?(?:j\.)?map\.baidu\.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.BAIDU_MAP,
        nameResId = R.string.converter_baidu_map_name,
        items = listOf(
            InputDocumentationItem.Url(35, "https://j.map.baidu.com"),
            InputDocumentationItem.Url(33, "https://map.baidu.com"),
        ),
    )
    override val shortUriPattern = Regex("""(?:https?://)?j\.map\.baidu\.com/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                val parts = uri.pathParts.drop(1)
                val firstPart = parts.firstOrNull() ?: return@run

                if (firstPart == "") {
                    if (!queryParams["poiShareId"].isNullOrEmpty() || !queryParams["poiShareUid"].isNullOrEmpty()) {
                        // Shared point or shared place
                        // https://map.baidu.com/?poiShareId=<ID>
                        // https://map.baidu.com/?shareurl=1&poiShareUid=<UID>
                        webUriString = uri.toString()
                    }

                } else if (firstPart.startsWith('@')) {
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
                            .forEach { points.add(it) }
                    }
                }
            }
        }.asBD09MC()
    }

    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".css")
            || requestUrlString.endsWith(".ico")
            || requestUrlString.endsWith(".png")
            || requestUrlString.endsWith("/static/common/images/new/loading")

            // Map tiles
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "bdimg.com/tile/")

            // Tracking
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "/alog.min.js")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "map.baidu.com/newmap_test/static/common/images/transparent.gif")

    @StringRes
    override val permissionTitleResId = R.string.converter_baidu_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_baidu_map_loading_indicator_title
}

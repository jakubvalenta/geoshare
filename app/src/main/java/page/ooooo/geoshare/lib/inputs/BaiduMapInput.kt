package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatNamePoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.extensions.toLonLatZPoint
import page.ooooo.geoshare.lib.point.BD09MCPoint

object BaiduMapInput : ShortUriInput, WebInput {
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
    override val shortUriMethod = ShortUriInput.Method.HEAD

    @Suppress("SpellCheckingInspection")
    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        uri.run {
            val parts = uri.pathParts.drop(1)
            val firstPart = parts.firstOrNull() ?: return@run

            if (firstPart == "") {
                if (!queryParams["poiShareId"].isNullOrEmpty() || !queryParams["poiShareUid"].isNullOrEmpty()) {
                    // Shared point or shared place
                    // https://map.baidu.com/?poiShareId={id}
                    // https://map.baidu.com/?shareurl=1&poiShareUid={uid}
                    webUriString = uri.toString()
                }

            } else if (firstPart.startsWith('@')) {
                // Center
                // https://map.baidu.com/@{center_x},{center_y},{center_z}
                Regex(CENTER).matchEntire(firstPart)?.toLonLatZPoint()?.let {
                    points = persistentListOf(it.asBD09MC())
                }

            } else if (firstPart == "poi") {
                // Place
                // https://map.baidu.com/poi/{name}/@{x},{y},{z}
                Regex(CENTER).matchEntire(parts.getOrNull(2))?.toLonLatZPoint()?.let {
                    points = persistentListOf(it.asBD09MC().copy(name = parts.getOrNull(1)))
                }

            } else if (firstPart == "dir") {
                // Directions with query params
                // https://map.baidu.com/dir/...?sn={start_point}&en={waypoint_point}$$1$$%20to:{dest_point}
                val pattern = Regex(WAYPOINT)
                points = listOfNotNull(
                    pattern.find(queryParams["sn"])?.toLonLatNamePoint()?.asBD09MC(),
                    *pattern.findAll(queryParams["en"]).mapNotNull { it.toLonLatNamePoint()?.asBD09MC() }
                        .toList().toTypedArray(),
                ).takeIf { it.isNotEmpty() }?.toImmutableList() ?:
                    // Directions with waypoint names only (ignore center)
                    // https://map.baidu.com/dir/{start_name}/{waypoint_name}/{dest_name}/@{center_x},{center_y},{center_z}z
                    parts
                        .drop(1)
                        .filterNot { it.startsWith('@') }
                        .map { BD09MCPoint(0.0, 0.0, name = it) }
                        .toImmutableList()

            } else if (firstPart == "mobile") {
                // Mobile place detail with coords
                // https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid={uid}/act=read_share&vt=map&da_from=weixin&openna=1&sharegeo={lon}%2c{lat}"
                Regex("""sharegeo=$X,$Y""").find(parts.lastOrNull())?.toLonLatPoint()?.also {
                    points = persistentListOf(it.asBD09MC())
                }
                    ?: run {
                        // Mobile place detail without coords
                        // "https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid={uid}/act=read_share&vt=map&da_from=weixin&openna=1"
                        webUriString = uri.toString()
                    }
            }
        }
    }

    override fun extendWebSettings(settings: WebSettings) {
        settings.domStorageEnabled = true
        settings.userAgentString = NetworkTools.DESKTOP_USER_AGENT
    }

    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".css")
            || requestUrlString.endsWith(".ico")
            || (requestUrlString.endsWith(".png") && !requestUrlString.contains("/image/api/"))
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

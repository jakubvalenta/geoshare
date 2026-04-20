package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatNamePoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.extensions.toLonLatZPoint
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.NetworkTools

object BaiduMapInput : ShortUriInput, WebInput {
    private const val X = """(\d+(?:\.\d+)?)"""
    private const val Y = """(\d+(?:\.\d+)?)"""
    private const val CENTER = """@$X,$Y,${Z}z.*"""
    private const val WAYPOINT = """1\$\$\$\$$X,$Y\$\$([^$]+)"""

    override val uriPattern = Regex("""(?:https?://)?(?:j\.)?map\.baidu\.com/$URI_REST""")
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
    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            val parts = uri.pathParts.drop(1)
            val firstPart = parts.firstOrNull() ?: return@run

            if (firstPart == "") {
                if (
                    !queryParams["poiShareId"].isNullOrEmpty() ||
                    !queryParams["poiShareUid"].isNullOrEmpty() ||
                    queryParams["s"]?.contains("uid=") == true
                ) {
                    // Shared coordinates or shared POI
                    // https://map.baidu.com/?poiShareId={id}
                    // https://map.baidu.com/?shareurl=1&poiShareUid={uid}
                    // https://map.baidu.com/?newmap=1&s=inf%26uid%3D{uid}
                    webUriString = toString()
                }

            } else if (firstPart.startsWith('@')) {
                // Map center
                // https://map.baidu.com/@{centerX},{centerY},{centerZ}
                Regex(CENTER).matchEntire(firstPart)?.toLonLatZPoint(Source.MAP_CENTER)?.let {
                    points = persistentListOf(BD09MCPoint(it))
                }

            } else if (firstPart == "poi") {
                // POI
                // https://map.baidu.com/poi/{name}/@{x},{y},{z}
                Regex(CENTER).matchEntire(parts.getOrNull(2))?.toLonLatZPoint(Source.MAP_CENTER)?.let {
                    points = persistentListOf(BD09MCPoint(it).copy(name = parts.getOrNull(1)))
                }

            } else if (firstPart == "dir") {
                // Directions with query params
                // https://map.baidu.com/dir/...?sn={startPoint}&en={waypointPoint}$$1$$%20to:{destPoint}
                val pattern = Regex(WAYPOINT)
                points = listOfNotNull(
                    pattern.find(queryParams["sn"])?.toLonLatNamePoint(Source.URI)?.let { BD09MCPoint(it) },
                    *pattern.findAll(queryParams["en"])
                        .mapNotNull { m -> m.toLonLatNamePoint(Source.URI)?.let { BD09MCPoint(it) } }
                        .toList().toTypedArray(),
                ).takeIf { it.isNotEmpty() }?.toImmutableList() ?:
                    // Directions with waypoint names only (ignore center)
                    // https://map.baidu.com/dir/{startName}/{waypointName}/{destName}/@{centerX},{centerY},{centerZ}z
                    parts
                        .drop(1)
                        .filterNot { it.startsWith('@') }
                        .map { BD09MCPoint(name = it, source = Source.URI) }
                        .toImmutableList()

            } else if (firstPart == "mobile") {
                // Mobile place detail with coords
                // https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid={uid}/act=read_share&vt=map&da_from=weixin&openna=1&sharegeo={lon}%2c{lat}"
                Regex("""sharegeo=$X,$Y""").find(parts.lastOrNull())?.toLonLatPoint(Source.URI)?.also {
                    points = persistentListOf(BD09MCPoint(it))
                }
                    ?: run {
                        // Mobile place detail without coords
                        // "https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid={uid}/act=read_share&vt=map&da_from=weixin&openna=1"
                        webUriString = toString()
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
        requestUrlString.endsWith(".ico")
            || (requestUrlString.endsWith(".png") && !requestUrlString.contains("/image/api/"))
            || requestUrlString.endsWith("/static/common/images/new/loading")
            // Notice that we don't block .css, so that links such as https://j.map.baidu.com/a7/GXfM redirect to the
            // correct URL

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

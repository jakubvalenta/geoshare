package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatNamePoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.extensions.toLonLatZPoint
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source

object BaiduMapUriInput : UriInput {
    private const val X = """(\d+(?:\.\d+)?)"""
    private const val Y = """(\d+(?:\.\d+)?)"""
    private const val CENTER = """@$X,$Y,${Z}z.*"""
    private const val WAYPOINT = """1\$\$\$\$$X,$Y\$\$([^$]+)"""

    override val pattern = Regex("""((?:https?://)?(?:j\.)?map\.baidu\.com/$URI_REST)""")

    override val documentation = InputDocumentation(
        id = InputDocumentationId.BAIDU_MAP, // TODO Group documentations by id
        nameResId = R.string.converter_baidu_map_name,
        items = listOf(
            InputDocumentationItem.Url(33, "https://map.baidu.com"),
        ),
    )

    @Suppress("SpellCheckingInspection")
    override suspend fun parse(data: Uri, prevPoints: Points?, uriQuote: UriQuote, log: ILog) = buildParseResult {
        data.run {
            val parts = data.pathParts.drop(1)
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
                    nextInput = BaiduMapWebInput
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
                        nextInput = BaiduMapWebInput
                    }
            }
        }
    }
}

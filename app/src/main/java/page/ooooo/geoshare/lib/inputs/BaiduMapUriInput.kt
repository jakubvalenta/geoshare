package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.toLonLatNamePoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.extensions.toLonLatZPoint
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaiduMapUriInput @Inject constructor(
    private val baiduMapWebViewInput: dagger.Lazy<BaiduMapWebViewInput>,
    val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.BAIDU_MAP
    override val changelog = persistentListOf(
        InputChangelogItem.Url(33, "https://map.baidu.com"),
    )

    override val pattern = Regex("""((?:https?://)?(?:j\.)?map\.baidu\.com/$URI_REST)""")

    override fun parse(match: String, resources: Resources): ParseResult =
        Uri.parse(match, uriQuote).run {
            parseResult {
                var name: String? = null
                val parts = pathParts.drop(1)
                val firstPart = parts.firstOrNull() ?: return@parseResult

                // Shared coordinates or shared POI
                // https://map.baidu.com/?poiShareId={id}
                // https://map.baidu.com/?shareurl=1&poiShareUid={uid}
                // https://map.baidu.com/?newmap=1&s=inf%26uid%3D{uid}
                if (
                    firstPart == "" && (
                        !queryParams["poiShareId"]?.firstOrNull().isNullOrEmpty() ||
                            !queryParams["poiShareUid"]?.firstOrNull().isNullOrEmpty() ||
                            queryParams["s"]?.firstOrNull()?.contains("uid=") == true
                        )
                ) {
                    next = MatchedInput(baiduMapWebViewInput.get(), match)
                    return@parseResult
                }

                // Map center from path (contains zoom)
                // https://map.baidu.com/.../@{centerX},{centerY},{centerZ}
                val centerFromPath = parts
                    .firstOrNull { it.startsWith('@') }
                    ?.let { part ->
                        Regex(CENTER).matchEntire(part)?.toLonLatZPoint(Source.MAP_CENTER)
                    }
                val z = centerFromPath?.z

                when (firstPart) {
                    "poi", "search" -> {
                        name = parts.getOrNull(1)?.takeIf { it.isNotEmpty() }
                        // Continue to map center parsing
                    }

                    "dir" -> {
                        // Directions with query params
                        // https://map.baidu.com/dir/...?sn={point1Name}&en={point2Name}$$1$$%20to:{lastPointName}
                        val pattern = Regex(WAYPOINT)
                        points = listOfNotNull(
                            pattern.find(queryParams["sn"]?.firstOrNull())?.toLonLatNamePoint(Source.URI)
                                ?.let { BD09MCPoint(it, z) },
                            *pattern.findAll(queryParams["en"]?.firstOrNull())
                                .mapNotNull { m -> m.toLonLatNamePoint(Source.URI)?.let { BD09MCPoint(it, z) } }
                                .toList().toTypedArray(),
                        )
                            .takeIf { it.isNotEmpty() }
                            ?.toImmutableList()
                            // Directions with waypoint names only (ignore center)
                            // https://map.baidu.com/dir/{point1Name}/{point2Name}/{lastPointName}/@{centerX},{centerY},{centerZ}z
                            ?: parts
                                .drop(1)
                                .filterNot { it.startsWith('@') }
                                .map { BD09MCPoint(z = z, name = it, source = Source.URI) }
                                .toImmutableList()
                        return@parseResult
                    }

                    "mobile" -> {
                        // Mobile place detail with coords
                        // https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid={uid}/act=read_share&vt=map&da_from=weixin&openna=1&sharegeo={lon}%2c{lat}"
                        Regex("""sharegeo=$X,$Y""").find(parts.lastOrNull())?.toLonLatPoint(Source.URI)?.also {
                            points = persistentListOf(BD09MCPoint(it, z))
                        }
                            ?: run {
                                // Mobile place detail without coords
                                // "https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid={uid}/act=read_share&vt=map&da_from=weixin&openna=1"
                                next = MatchedInput(baiduMapWebViewInput.get(), match)
                            }
                        return@parseResult
                    }
                }

                // Map center from query parameters (takes precedence over center from path)
                // https://map.baidu.com/?nb_x={centerX}&nb_y={centerY}
                (queryParams["nb_x"]?.firstOrNull()?.toDoubleOrNull()?.let { lon ->
                    queryParams["nb_y"]?.firstOrNull()?.toDoubleOrNull()?.let { lat ->
                        NaivePoint(lat, lon, source = Source.MAP_CENTER)
                    }
                } ?: centerFromPath)?.let {
                    points = persistentListOf(BD09MCPoint(it, z, name))
                    return@parseResult
                }

                if (name != null) {
                    points = persistentListOf(BD09MCPoint(name = name, source = Source.URI))
                }
            }
        }

    override fun toString() = "BaiduMapUriInput"

    private companion object {
        private const val X = """(\d+(?:\.\d+)?)"""
        private const val Y = """(\d+(?:\.\d+)?)"""
        private const val CENTER = """@$X,$Y,${Z}z.*"""
        private const val WAYPOINT = """1\$\$\$\$$X,$Y\$\$([^$]+)"""
    }
}

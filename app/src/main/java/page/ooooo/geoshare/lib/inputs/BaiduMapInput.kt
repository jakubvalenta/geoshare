package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findAllLatLonZName
import page.ooooo.geoshare.lib.extensions.matchLatLonZName
import page.ooooo.geoshare.lib.position.LatLonZName
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.buildPosition

object BaiduMapInput : Input {
    private const val X = """(?P<lon>\d+\.\d+)"""
    private const val Y = """(?P<lat>\d+\.\d+)"""
    private const val POINT = """1\$\$\$\$$X,$Y\$\$(?P<name>[^\$]+)\$\$0\$\$\$\$"""

    private val srs = Srs.GCJ02

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?map\.baidu\.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.BAIDU_MAP,
        nameResId = R.string.converter_baidu_map_name,
        items = listOf(
            InputDocumentationItem.Url(33, "https://map.baidu.com"),
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        val position = buildPosition(srs) {
            uri.run {
                // Directions
                // https://map.baidu.com/dir/...?sn=<START_POINT>&en=<WAYPOINT_POINT>$$1$$%20to:<DEST_POINT>
                setPointIfNull { POINT matchLatLonZName queryParams["sn"] } // Start
                addPoints { POINT findAllLatLonZName queryParams["en"] } // Waypoints and destination
                if (hasPoint()) {
                    return@run
                }

                // Other
                // https://map.baidu.com/@<CENTER_X>,<CENTER_Y>,<CENTER_Z>
                // https://map.baidu.com/dir/<START_NAME>/<WAYPOINT_NAME>/<DEST_NAME>/@<CENTER_X>,<CENTER_Y>,<CENTER_Z>z
                // https://map.baidu.com/poi/<NAME>/@<X>,<Y>,<Z>
                val parseUriParts = setOf("dir", "poi")
                val parts = uri.pathParts.drop(1)
                val firstPart = parts.firstOrNull()
                if (firstPart != null && (firstPart in parseUriParts || firstPart.startsWith('@'))) {
                    addPoints {
                        sequence {
                            var lastName: String? = null
                            parts.dropWhile { it in parseUriParts }.forEach { part ->
                                if (part.startsWith('@')) {
                                    ("""@$X,$Y,${Z}""" matchLatLonZName part)?.let { latLonZName ->
                                        yield(latLonZName.copy(name = lastName))
                                        lastName = null
                                    }
                                } else {
                                    if (lastName != null) {
                                        yield(LatLonZName(0.0, 0.0, name = lastName))
                                    }
                                    lastName = part
                                }
                            }
                            if (lastName != null) {
                                yield(LatLonZName(0.0, 0.0, name = lastName))
                            }
                        }
                    }
                }
            }
        }
        return ParseUriResult.from(position)
    }
}

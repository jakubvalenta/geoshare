package page.ooooo.geoshare.lib

import kotlin.math.max
import kotlin.math.roundToInt

fun decodeGeoHash(
    hash: String,
    map: Map<Char, Int>,
    bitCount: Int,
    zoomChar: Char? = null,
): Triple<Double, Double, Int> {
    val coords = arrayOf(
        arrayOf(-90.0, 90.0),
        arrayOf(-180.0, 180.0),
    )
    var currCoord = 1
    var hashBitCount = 0
    hash.forEach { char ->
        map[char]?.let { digit ->
            digit.forEachBinaryDigit(bitCount) { bit ->
                val coord = coords[currCoord]
                coord[bit xor 1] = (coord[0] + coord[1]) / 2
                currCoord = currCoord xor 1
                hashBitCount++
            }
        }
    }
    val lat = ((coords[0][0] + coords[0][1]) / 2).toScale(6)
    val lon = ((coords[1][0] + coords[1][1]) / 2).toScale(6)

    // To calculate zoom: take the precision of the hash, add a constant that zooms out a bit, and subtract -2 if the
    // hash ends with "--" or -1 if it ends with "-" or 0 if it ends with "---" etc.
    val baseZoom = hashBitCount / 2.0
    val zoomCharCount = zoomChar?.let { zoomChar -> hash.takeLastWhile { it == zoomChar }.length } ?: 0
    val relativeZoom = (zoomCharCount + 2).mod(3) - 2
    val osmZoomAdjustment = -8
    val zoom = baseZoom + (relativeZoom + osmZoomAdjustment) * bitCount / 6.0
    val z = max(zoom, 0.0).roundToInt()

    return Triple(lat, lon, z)
}

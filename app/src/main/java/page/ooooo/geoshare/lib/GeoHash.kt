package page.ooooo.geoshare.lib

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

@Suppress("SpellCheckingInspection")
private val modifiedBase64Map = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_~"
    .mapIndexed { i, char -> char to i }.toMap()
private const val modifiedBase64ZoomChar = '-'
private const val modifiedBase64DigitBitCount = 6

@Suppress("SpellCheckingInspection")
private val base32Map = "0123456789bcdefghjkmnpqrstuvwxyz"
    .mapIndexed { i, char -> char to i }.toMap()
private const val base32DigitBitCount = 5

private const val osmZoomAdjustment = -8

/**
 * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
 */
fun decodeGeoHash(hash: String): Triple<Double, Double, Int> {
    val coords = arrayOf(
        arrayOf(-90.0, 90.0),
        arrayOf(-180.0, 180.0),
    )
    var currCoord = 1
    var hashBitCount = 0
    hash.forEach { char ->
        base32Map[char]?.let { digit ->
            digit.forEachBinaryDigit(base32DigitBitCount) { bit ->
                val coord = coords[currCoord]
                coord[bit xor 1] = (coord[0] + coord[1]) / 2
                currCoord = currCoord xor 1
                hashBitCount++
            }
        }
    }
    val lat = ((coords[0][0] + coords[0][1]) / 2).toScale(6)
    val lon = ((coords[1][0] + coords[1][1]) / 2).toScale(6)
    val z = max(hashBitCount / 2.0 + osmZoomAdjustment * base32DigitBitCount / 6.0, 0.0).roundToInt()
    return Triple(lat, lon, z)
}

/**
 * See https://community.openstreetmap.org/t/python-sample-of-short-link-decode/110707
 */
fun decodeQuadTile(hash: String): Triple<Double, Double, Int> {
    var x = 0
    var y = 0
    var hashBitCount = 0
    hash.forEach { char ->
        modifiedBase64Map[char]?.let { digit ->
            digit.forEachBinaryDigit(modifiedBase64DigitBitCount) { bit ->
                if (hashBitCount.isOdd()) {
                    // Put odd bits into x
                    x = x shl 1 or bit
                } else {
                    // Put even bits into y
                    y = y shl 1 or bit
                }
                hashBitCount++
            }
        }
    }

    val lon = x * 2.0.pow(2 - 3 * hashBitCount / modifiedBase64DigitBitCount) * 90 - 180
    val lat = y * 2.0.pow(2 - 3 * hashBitCount / modifiedBase64DigitBitCount) * 45 - 90

    // Relative zoom works like this:
    // - If the hash doesn't end with "", subtract 0.
    // - If the hash ends with "-", subtract -2.
    // - If the hash ends with "--", subtract -1.
    // - If the hash ends with "---", subtract 0.
    // - If the hash ends with "----", subtract -2.
    // - etc.
    val zoomCharCount = hash.takeLastWhile { it == modifiedBase64ZoomChar }.length
    val relativeZoom = (zoomCharCount + 2).mod(3) - 2
    val z = max(hashBitCount / 2.0 + relativeZoom + osmZoomAdjustment, 0.0).roundToInt()

    return Triple(lat, lon, z)
}

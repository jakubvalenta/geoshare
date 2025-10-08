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
 * A Geohash decoding algorithm that works for:
 * - Waze base32 Geohashes
 * - OpenStreetMap modified base64 QuadTile hashes
 *
 * See:
 * - https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
 * - https://wiki.openstreetmap.org/wiki/Shortlink#How_the_encoding_works
 */
private fun decodeGeoHash(
    hash: String,
    charMap: Map<Char, Int>,
    digitBitCount: Int,
    useMeanValue: Boolean = false,
    zoomChar: Char? = null,
): Triple<Double, Double, Int> {

    // Collect odd bits of the hash into x and even bits into y.
    // E.g. base32 hash "ezs" (0b01101_11111_11000) will have x=124 (0b01111100) and y=94 (0b1011110)
    var x = 0
    var y = 0
    var bitCount = 0
    var xBitCount = 0
    hash.forEach { char ->
        charMap[char]?.let { digit ->
            for (i in digitBitCount - 1 downTo 0) {
                val bit = (digit shr i) and 1
                if (bitCount and 1 == 0) {
                    x = x shl 1 or bit
                    xBitCount++
                } else {
                    y = y shl 1 or bit
                }
                bitCount++
            }
        }
    }
    val yBitCount = bitCount - xBitCount

    // Calculate into how many cells a hash of this precision splits longitude
    // E.g. base32 hash "ezs" splits longitude into 256 cells, because its max value is "zzz" (0b11111_11111_11111)
    val xCellCount = 2.0.pow(xBitCount)
    val yCellCount = 2.0.pow(yBitCount)  // Do the same for latitude

    // Split the longitude range (-180 to 180) into the calculated number of cells and read the value of cell number x
    // E.g. base32 hash "ezs" splits longitude into 256 cells and reads the value of the cell number 124
    val meanValueAdjustment = if (useMeanValue) 1 else 0  // Optionally read middle value of the cell; default is left
    val lon = (2 * x + meanValueAdjustment) / xCellCount * 180 - 180
    val lat = (2 * y + meanValueAdjustment) / yCellCount * 90 - 90  // Do the same for latitude

    // Calculate zoom based on the precision of the hash; higher precision results in higher zoom
    var z = bitCount / 2.0

    // Adjust zoom by a magic constant that OpenStreetMap uses for their short links
    var zoomAdjustment = osmZoomAdjustment

    // Add relative zoom, which works like this:
    // - If the hash doesn't end with "-", add 0.
    // - If the hash ends with "-", add -2.
    // - If the hash ends with "--", add -1.
    // - If the hash ends with "---", add 0.
    // - If the hash ends with "----", add -2.
    // - etc.
    if (zoomChar != null) {
        val zoomCharCount = hash.takeLastWhile { it == zoomChar }.length
        if (zoomCharCount > 0) {
            val relativeZoom = (zoomCharCount + 2).mod(3) - 2
            zoomAdjustment += relativeZoom
        }
    }

    // Because the zoom adjustments were designed for base64 hashes, multiply them so they work for base32 hashes too
    z += zoomAdjustment * (digitBitCount.toDouble() / modifiedBase64DigitBitCount)

    return Triple(lat, lon, max(z, 0.0).roundToInt())
}

fun decodeBase32GeoHash(hash: String): Triple<Double, Double, Int> =
    decodeGeoHash(hash, base32Map, base32DigitBitCount, useMeanValue = true)

fun decodeModifiedBase64GeoHash(hash: String): Triple<Double, Double, Int> =
    decodeGeoHash(hash, modifiedBase64Map, modifiedBase64DigitBitCount, zoomChar = modifiedBase64ZoomChar)

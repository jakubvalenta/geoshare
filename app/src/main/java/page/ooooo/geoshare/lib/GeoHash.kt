package page.ooooo.geoshare.lib

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A Geohash decoding algorithm that works for:
 * - Waze base32 Geohashes
 * - OpenStreetMap modified base64 QuadTile hashes
 * - Organic Maps and CoMaps base64 Geohashes
 *
 * See:
 * - https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
 * - https://wiki.openstreetmap.org/wiki/Shortlink#How_the_encoding_works
 * - https://github.com/organicmaps/url-processor/blob/d7b873dd1ea044fc6c5b7e63b570855dfe24f259/src/ge0.ts#L120-L156
 */
fun decodeGeoHash(
    hash: String,
    charMap: Map<Char, Int>,
    digitBitCount: Int,
    isLonOddBits: Boolean = true,
    useMeanValue: Boolean = false,
    zoomAdjustmentConst: Int = -8,
): Triple<Double, Double, Int> {

    // Collect odd bits of the hash into x and even bits into y (or the other way around, if isLonOddBits is false).
    // E.g. base32 hash "ezs" (0b01101_11111_11000) will have x=124 (0b01111100) and y=94 (0b1011110)
    var x = 0
    var y = 0
    var bitCount = 0
    var xBitCount = 0
    val lonPosition = if (isLonOddBits) 0 else 1
    hash.forEach { char ->
        charMap[char]?.let { digit ->
            for (i in digitBitCount - 1 downTo 0) {
                val bit = (digit shr i) and 1
                if (bitCount and 1 == lonPosition) {
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
    val xCellCount = 1 shl xBitCount
    val yCellCount = 1 shl yBitCount  // Do the same for latitude

    // Split the longitude range (-180 to 180) into the calculated number of cells and read the value of cell number x
    // E.g. base32 hash "ezs" splits longitude into 256 cells and reads the value of the cell number 124
    val meanValueAdjustment =
        if (useMeanValue) 0.5 else 0.0  // Optionally read middle value of the cell; default is left
    val lon = (x + meanValueAdjustment) / xCellCount * 360 - 180
    val lat = (y + meanValueAdjustment) / yCellCount * 180 - 90  // Do the same for latitude

    // Calculate zoom based on the precision of the hash; higher precision results in higher zoom
    var z = bitCount / 2.0

    // Adjust zoom by a magic constant that OpenStreetMap uses for their short links; the constant was designed for
    // base64 hashes, so we need to multiply to make it work for base32 hashes too
    z += zoomAdjustmentConst * (digitBitCount.toDouble() / 6.0)

    return Triple(lat, lon, max(z, 0.0).roundToInt())
}

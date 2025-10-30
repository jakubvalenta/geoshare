package page.ooooo.geoshare.lib

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

private enum class GeoHashBitOrder { LONLAT, LATLON }
private enum class GeoHashRoundingMode { LEFT, MIDDLE }

/**
 * A universal Geohash decoding algorithm that works for various character sets and rounding modes.
 */
private fun decodeGeoHash(
    hash: String,
    charMap: Map<Char, Int>,
    digitBitCount: Int,
    bitOrder: GeoHashBitOrder = GeoHashBitOrder.LONLAT,
    roundingMode: GeoHashRoundingMode = GeoHashRoundingMode.LEFT,
    yCellCountAdjustment: (yBitCount: Int) -> Double = { 0.0 },
    zoomAdjustmentConst: Int = -8,
): Triple<Double, Double, Int> {

    // Collect odd bits of the hash into x and even bits into y (or the other way around, if isLonOddBits is false).
    // E.g. base32 hash "ezs" (0b01101_11111_11000) will have x=124 (0b01111100) and y=94 (0b1011110)
    var x = 0
    var y = 0
    var bitCount = 0
    var xBitCount = 0
    val lonPosition = when (bitOrder) {
        GeoHashBitOrder.LONLAT -> 0
        GeoHashBitOrder.LATLON -> 1
    }
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
    val yCellCount = (1 shl yBitCount) +  // Do the same for latitude
            // Adjust latitude cell count, if required by the particular Geohash algorithm
            yCellCountAdjustment(yBitCount)

    // Split the longitude range (-180 to 180) into the calculated number of cells and read the value of cell number x
    // E.g. base32 hash "ezs" splits longitude into 256 cells and reads the value of the cell number 124
    val roundingAdjustment = when (roundingMode) {
        // Decide which exact value of the cell is read -- left edge of the cell, middle of the cell, etc.
        GeoHashRoundingMode.LEFT -> 0.0
        GeoHashRoundingMode.MIDDLE -> 0.5
    }

    val lon = (x + roundingAdjustment) / xCellCount * 360 - 180
    val lat = (y + roundingAdjustment) / yCellCount * 180 - 90  // Do the same for latitude

    // Calculate zoom based on the precision of the hash; higher precision results in higher zoom
    var z = bitCount / 2.0

    // Adjust zoom by a magic constant that OpenStreetMap uses for their short links; the constant was designed for
    // base64 hashes, so we need to multiply it to make it work for base32 hashes too
    z += zoomAdjustmentConst * (digitBitCount.toDouble() / 6.0)

    return Triple(lat, lon, max(z, 0.0).roundToInt())
}

@Suppress("SpellCheckingInspection")
private val OPEN_STREET_MAP_HASH_CHAR_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_~"
    .mapIndexed { i, char -> char to i }.toMap()

/**
 * See https://wiki.openstreetmap.org/wiki/Shortlink#How_the_encoding_works
 */
fun decodeOpenStreetMapQuadTileHash(hash: String) = decodeGeoHash(
    hash = hash,
    charMap = OPEN_STREET_MAP_HASH_CHAR_MAP,
    digitBitCount = 6,
).let { (lat, lon, z) ->
    // Add relative zoom, which works like this:
    // - If the hash doesn't end with "-", add 0.
    // - If the hash ends with "-", add -2.
    // - If the hash ends with "--", add -1.
    // - If the hash ends with "---", add 0.
    // - If the hash ends with "----", add -2.
    // - etc.
    val relativeZoom = hash.takeLastWhile { it == '-' }.length.takeIf { it > 0 }
        ?.let { zoomCharCount -> (zoomCharCount + 2).mod(3) - 2 } ?: 0
    Triple(lat, lon, max(z + relativeZoom, 0))
}

@Suppress("SpellCheckingInspection")
private val ORGANIC_MAPS_HASH_CHAR_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    .mapIndexed { i, char -> char to i }.toMap()

/**
 * See https://github.com/mapsme/ge0_url_decoder/blob/c609a6503fa91d424d5169c74158424e9eaf6f06/mwm_api.php#L7-L51
 */
fun decodeGe0Hash(hash: String) = decodeGeoHash(
    hash = try {
        hash.substring(1)
    } catch (_: IndexOutOfBoundsException) {
        ""
    },
    charMap = ORGANIC_MAPS_HASH_CHAR_MAP,
    digitBitCount = 6,
    bitOrder = GeoHashBitOrder.LATLON,
    roundingMode = GeoHashRoundingMode.MIDDLE,
    yCellCountAdjustment = { yBitCount -> -(2.0.pow(yBitCount - 30)) },
).let { (lat, lon, z) ->
    val zFromHash = hash.getOrNull(0)
        ?.let { ORGANIC_MAPS_HASH_CHAR_MAP[it] }
        ?.let { (it / 4.0 + 4).roundToInt() }
    Triple(lat, lon, zFromHash ?: z)
}

@Suppress("SpellCheckingInspection")
private val WAZE_HASH_CHAR_MAP = "0123456789bcdefghjkmnpqrstuvwxyz"
    .mapIndexed { i, char -> char to i }.toMap()

/**
 * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
 */
fun decodeWazeGeoHash(hash: String) = decodeGeoHash(
    hash = hash,
    charMap = WAZE_HASH_CHAR_MAP,
    digitBitCount = 5,
    roundingMode = GeoHashRoundingMode.MIDDLE,
)

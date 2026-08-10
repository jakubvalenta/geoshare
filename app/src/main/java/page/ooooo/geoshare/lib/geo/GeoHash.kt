package page.ooooo.geoshare.lib.geo

import com.google.openlocationcode.OpenLocationCode
import page.ooooo.geoshare.lib.extensions.toScale
import kotlin.math.max
import kotlin.math.pow

private enum class GeoHashBitOrder { LON_LAT, LAT_LON }
private enum class GeoHashRoundingMode { LEFT, MIDDLE }

/**
 * A universal Geohash decoding algorithm that works for various character sets and rounding modes.
 */
private fun decodeGeoHash(
    hash: String,
    charMap: Map<Char, Int>,
    numberBitCount: Int,
    bitOrder: GeoHashBitOrder = GeoHashBitOrder.LON_LAT,
    roundingMode: GeoHashRoundingMode = GeoHashRoundingMode.LEFT,
    yCellCountAdjustment: (yBitCount: Int) -> Double = { 0.0 },
    zoomAdjustmentConst: Int = -8,
): NaivePoint {

    // Collect odd bits of the hash into x and even bits into y (or the other way around, if isLonOddBits is false).
    // E.g. base32 hash "ezs" (0b01101_11111_11000) will have x=124 (0b01111100) and y=94 (0b1011110)
    var x = 0
    var y = 0
    var bitCount = 0
    var xBitCount = 0
    val lonPosition = when (bitOrder) {
        GeoHashBitOrder.LON_LAT -> 0
        GeoHashBitOrder.LAT_LON -> 1
    }
    hash.forEach { char ->
        charMap[char]?.let { number ->
            for (i in numberBitCount - 1 downTo 0) {
                val bit = (number shr i) and 1
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
    z += zoomAdjustmentConst * (numberBitCount.toDouble() / 6.0)

    return NaivePoint(lat, lon, max(z, 0.0).toScale(0), source = Source.HASH)
}

private val OPEN_STREET_MAP_HASH_CHAR_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_~"
    .mapIndexed { i, char -> char to i }.toMap()

/**
 * See https://wiki.openstreetmap.org/wiki/Shortlink#How_the_encoding_works
 */
fun decodeOpenStreetMapQuadTileHash(hash: String): NaivePoint {
    val naivePoint = decodeGeoHash(hash = hash, charMap = OPEN_STREET_MAP_HASH_CHAR_MAP, numberBitCount = 6)
    if (naivePoint.z == null) {
        return naivePoint
    }
    // Add relative zoom, which works like this:
    // - If the hash doesn't end with "-", add 0.
    // - If the hash ends with "-", add -2.
    // - If the hash ends with "--", add -1.
    // - If the hash ends with "---", add 0.
    // - If the hash ends with "----", add -2.
    // - etc.
    val relativeZoom = hash.takeLastWhile { it == '-' }.length.takeIf { it > 0 }
        ?.let { zoomCharCount -> (zoomCharCount + 2).mod(3) - 2 } ?: 0
    return naivePoint.copy(z = max(naivePoint.z + relativeZoom, 0.0))
}

private val ORGANIC_MAPS_HASH_CHAR_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    .mapIndexed { i, char -> char to i }.toMap()

/**
 * See https://github.com/mapsme/ge0_url_decoder/blob/c609a6503fa91d424d5169c74158424e9eaf6f06/mwm_api.php#L7-L51
 */
fun decodeGe0Hash(hash: String): NaivePoint {
    val naivePoint = decodeGeoHash(
        hash = try {
            hash.substring(1)
        } catch (_: IndexOutOfBoundsException) {
            ""
        },
        charMap = ORGANIC_MAPS_HASH_CHAR_MAP,
        numberBitCount = 6,
        bitOrder = GeoHashBitOrder.LAT_LON,
        roundingMode = GeoHashRoundingMode.MIDDLE,
        yCellCountAdjustment = { yBitCount -> -(2.0.pow(yBitCount - 30)) },
    )
    val zFromHash = hash.getOrNull(0)
        ?.let { ORGANIC_MAPS_HASH_CHAR_MAP[it] }
        ?.let { (it / 4.0 + 4).toScale(0) }
    return naivePoint.copy(z = zFromHash ?: naivePoint.z)
}

private val WAZE_HASH_CHAR_MAP = "0123456789bcdefghjkmnpqrstuvwxyz"
    .mapIndexed { i, char -> char to i }.toMap()

/**
 * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
 */
fun decodeWazeGeoHash(hash: String) = decodeGeoHash(
    hash = hash,
    charMap = WAZE_HASH_CHAR_MAP,
    numberBitCount = 5,
    roundingMode = GeoHashRoundingMode.MIDDLE,
)

private val MAPY_COM_CHAR_MAP = "0ABCD2EFGH4IJKLMN6OPQRST8UVWXYZ-1abcd3efgh5ijklmn7opqrst9uvwxyz."
    .mapIndexed { i, char -> char to i }.toMap()

/**
 * Decode the hash that Mapy.com uses to store a list of coordinates.
 *
 * The hash starts by the absolute coordinates of the first point. Then follow the coordinates of the second point as a
 * relative offset from the first point. Then follow the coordinates of the third point as a relative offset from the
 * second point, etc.
 *
 * Each coordinate can be described by a different number of characters, either 5, 3, or 2 characters. The information
 * about the size of each coordinate is stored in the first two bits of the coordinate number.
 *
 * So this hash:
 *
 * ```
 * 9gz-HxYH7ngonA6
 * ```
 *
 * Is composed of:
 *
 * - `9gz-H` = 1st point x (`9` determines that coordinate is 5 characters long)
 * - `xYH7n` = 1st point y (`x` determines that coordinate is 5 characters long)
 * - `gon` = 2nd point x as an offset from the 1st point x (`g` determines that the coordinate is 3 characters long)
 * - `A6` = 2nd point y as an offset from the 1st point y (`A` determines that the coordinate is 2 characters long)
 */
fun decodeMapyComGeoHash(
    hash: String,
    charMap: Map<Char, Int> = MAPY_COM_CHAR_MAP,
    numberBitCount: Int = 6,
): List<NaivePoint> = buildList {
    var x = 0
    var y = 0
    var charsToRead = 0
    var readingXCoord = true

    hash.forEach { char ->
        charMap[char]?.let { number ->
            if (charsToRead > 0) {
                // Reading of a coordinate is in progress
                charsToRead--
                if (readingXCoord) {
                    x += number shl (numberBitCount * charsToRead)
                    if (charsToRead == 0) {
                        // Reading of the x coordinate has finished, switch to reading the y coordinate
                        readingXCoord = false
                    }
                } else {
                    y += number shl (numberBitCount * charsToRead)
                    if (charsToRead == 0) {
                        // Reading of the y coordinate has finished, switch to reading the x coordinate, and add both
                        // coordinates to result
                        readingXCoord = true
                        add(
                            NaivePoint(
                                lat = (y / (1 shl 28).toDouble()) * 180 - 90,
                                lon = (x / (1 shl 28).toDouble()) * 360 - 180,
                                source = Source.HASH,
                            )
                        )
                    }
                }
            } else {
                // Start reading a coordinate; first determine the type of the coordinate from its first number
                if (number >= 48) {
                    // Coordinate type is a 5-character coordinate
                    if (readingXCoord) {
                        // Subtract 48 from the number, so that we drop the first two bits, which are used only to
                        // determine the coordinate type
                        x = (number - 48) shl (numberBitCount * 4)
                    } else {
                        y = (number - 48) shl (numberBitCount * 4)
                    }
                    charsToRead = 4
                } else if (number >= 32) {
                    // Coordinate type is a 3-character offset from the previous coordinate
                    if (readingXCoord) {
                        // Subtract 32 from the number, so that we drop the first two bits, which are used only to
                        // determine the coordinate type; then subtract a large number, so that the offset can be
                        // negative
                        x += ((number - 32) shl (numberBitCount * 2)) - 32_768
                    } else {
                        y += ((number - 32) shl (numberBitCount * 2)) - 32_768
                    }
                    charsToRead = 2
                } else {
                    // Coordinate type is a 2-character offset from the previous coordinate
                    if (readingXCoord) {
                        // Subtract a large number, so that the offset can be negative
                        x += (number shl numberBitCount) - 1024
                    } else {
                        y += (number shl numberBitCount) - 1024
                    }
                    charsToRead = 1
                }
            }
        }
    }
}

/**
 * See https://github.com/google/open-location-code
 */
fun decodePlusCode(codeString: String): NaivePoint? =
    try {
        OpenLocationCode(codeString)
    } catch (_: IllegalArgumentException) {
        // Invalid code
        null
    }
        ?.run {
            if (isFull) {
                // The code is full
                this
            } else {
                // Complete the code using a geocoding service (not implemented yet)
                null
            }
        }
        ?.decode()
        ?.run { NaivePoint(lat = centerLatitude, lon = centerLongitude, source = Source.HASH) }

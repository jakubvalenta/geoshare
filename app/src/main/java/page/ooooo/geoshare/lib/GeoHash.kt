package page.ooooo.geoshare.lib

private val base32Map = hashMapOf(
    '0' to 0,
    '1' to 1,
    '2' to 2,
    '3' to 3,
    '4' to 4,
    '5' to 5,
    '6' to 6,
    '7' to 7,
    '8' to 8,
    '9' to 9,
    'b' to 10,
    'c' to 11,
    'd' to 12,
    'e' to 13,
    'f' to 14,
    'g' to 15,
    'h' to 16,
    'j' to 17,
    'k' to 18,
    'm' to 19,
    'n' to 20,
    'p' to 21,
    'q' to 22,
    'r' to 23,
    's' to 24,
    't' to 25,
    'u' to 26,
    'v' to 27,
    'w' to 28,
    'x' to 29,
    'y' to 30,
    'z' to 31,
)

fun decodeGeoHash(hash: String): Pair<Double, Double> {
    val coords = arrayOf(
        arrayOf(-90.0, 90.0),
        arrayOf(-180.0, 180.0),
    )
    var currCoord = 1
    hash.forEach { char ->
        base32Map[char]?.let { dec ->
            dec.forEachBinaryDigit(5) { bit ->
                val coord = coords[currCoord]
                coord[bit xor 1] = (coord[0] + coord[1]) / 2
                currCoord = currCoord xor 1
            }
        }
    }
    println("${coords[0][0]}, ${coords[0][1]}")
    val lat = ((coords[0][0] + coords[0][1]) / 2).toScale(6)
    val lon = ((coords[1][0] + coords[1][1]) / 2).toScale(6)
    return lat to lon
}

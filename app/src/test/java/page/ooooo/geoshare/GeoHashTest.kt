package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.converters.OpenStreetMapUrlConverter.Companion.MODIFIED_BASE64_BIT_COUNT
import page.ooooo.geoshare.lib.converters.OpenStreetMapUrlConverter.Companion.MODIFIED_BASE64_ZOOM_CHAR
import page.ooooo.geoshare.lib.converters.OpenStreetMapUrlConverter.Companion.modifiedBase64Map
import page.ooooo.geoshare.lib.converters.WazeUrlConverter.Companion.BASE32_BIT_COUNT
import page.ooooo.geoshare.lib.converters.WazeUrlConverter.Companion.base32Map
import page.ooooo.geoshare.lib.decodeGeoHash

@Suppress("SpellCheckingInspection")
class GeoHashTest {
    fun decodeGeoHashBase32(hash: String) =
        decodeGeoHash(hash, base32Map, BASE32_BIT_COUNT)

    fun decodeGeoHashModifiedBase64(hash: String) =
        decodeGeoHash(hash, modifiedBase64Map, MODIFIED_BASE64_BIT_COUNT, MODIFIED_BASE64_ZOOM_CHAR)

    @Test
    fun decodeGeoHash_base32_returnsZoomBasedOnHashLength() {
        listOf(0, 0, 1, 3, 6, 8, 11, 13, 16, 18).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeGeoHashBase32("9".repeat(i + 1)).third)
        }
    }

    @Test
    fun decodeGeoHash_modifiedBase64_returnsZoomBasedOnHashLength() {
        listOf(0, 0, 1, 4, 7, 10, 13, 16, 19, 22).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeGeoHashModifiedBase64("9".repeat(i + 1)).third)
        }
    }

    @Test
    fun decodeGeoHash_modifiedBase64WithOneZoomChar_returnsZoomBasedOnHashLengthDecreasedByOne() {
        listOf(0, 0, 0, 3, 6, 9, 12, 15, 18, 21).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeGeoHashModifiedBase64("9".repeat(i + 1) + "-").third)
        }
    }

    @Test
    fun decodeGeoHash_modifiedBase64WithTwoZoomChars_returnsZoomBasedOnHashLengthDecreasedByTwo() {
        listOf(0, 0, 0, 2, 5, 8, 11, 14, 17, 20).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeGeoHashModifiedBase64("9".repeat(i + 1) + "--").third)
        }
    }

    /**
     * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
     */
    @Test
    fun decodeGeoHash_wikipediaExample() {
        assertEquals(
            Triple(42.60498, -5.603027, 6),
            decodeGeoHashBase32("ezs42")
        )
    }

    @Test
    fun decodeGeoHash_waze() {
        assertEquals(
            Triple(52.77714, 11.951087, 16),
            decodeGeoHashBase32("u32kpzy90")
        )
        assertEquals(
            Triple(-12.581599, 15.743902, 16),
            decodeGeoHashBase32("kmg0qm86g")
        )
        assertEquals(
            Triple(22.6531, 88.343575, 16),
            decodeGeoHashBase32("tunbf7cgf")
        )
        assertEquals(
            Triple(19.402564, -99.165666, 16),
            decodeGeoHashBase32("9g3qrkju0")
        )
    }

    /**
     * https://wiki.openstreetmap.org/wiki/Shortlink#Example
     */
    @Test
    fun decodeGeoHash_osmExample() {
        assertEquals(
            Triple(51.510772705078125, 0.054931640625, 9),
            decodeGeoHashModifiedBase64("0EEQjE--"),
        )
        assertEquals(
            Triple(51.510998010635376, 0.05499601364135742, 16),
            decodeGeoHashModifiedBase64("0EEQjEEb"),
        )
    }

    @Test
    fun decodeGeoHash_osmZoom() {
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13),
            decodeGeoHashModifiedBase64("NuJWxJh"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11),
            decodeGeoHashModifiedBase64("NuJWxJh-"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 12),
            decodeGeoHashModifiedBase64("NuJWxJh--"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13),
            decodeGeoHashModifiedBase64("NuJWxJh---"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11),
            decodeGeoHashModifiedBase64("NuJWxJh----"),
        )
    }
}

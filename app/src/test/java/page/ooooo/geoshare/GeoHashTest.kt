package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.decodeGeoHash
import page.ooooo.geoshare.lib.decodeQuadTile

@Suppress("SpellCheckingInspection")
class GeoHashTest {
    @Test
    fun decodeGeoHash_returnsZoomBasedOnHashLength() {
        listOf(0, 0, 1, 3, 6, 8, 11, 13, 16, 18).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeGeoHash("9".repeat(i + 1)).third)
        }
    }

    @Test
    fun decodeQuadTile_returnsZoomBasedOnHashLength() {
        listOf(0, 0, 1, 4, 7, 10, 13, 16, 19, 22).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeQuadTile("9".repeat(i + 1)).third)
        }
    }

    @Test
    fun decodeQuadTile_returnsZoomBasedOnHashLengthDecreasedByOne() {
        listOf(0, 0, 0, 3, 6, 9, 12, 15, 18, 21).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeQuadTile("9".repeat(i + 1) + "--").third)
        }
    }

    @Test
    fun decodeQuadTile_returnsZoomBasedOnHashLengthDecreasedByTwo() {
        listOf(0, 0, 0, 2, 5, 8, 11, 14, 17, 20).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeQuadTile("9".repeat(i + 1) + "-").third)
        }
    }

    /**
     * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
     */
    @Test
    fun decodeGeoHash_wikipediaExample() {
        assertEquals(
            Triple(42.60498, -5.603027, 6),
            decodeGeoHash("ezs42")
        )
    }

    @Test
    fun decodeGeoHash_waze() {
        assertEquals(
            Triple(52.77714, 11.951087, 16),
            decodeGeoHash("u32kpzy90")
        )
        assertEquals(
            Triple(-12.581599, 15.743902, 16),
            decodeGeoHash("kmg0qm86g")
        )
        assertEquals(
            Triple(22.6531, 88.343575, 16),
            decodeGeoHash("tunbf7cgf")
        )
        assertEquals(
            Triple(19.402564, -99.165666, 16),
            decodeGeoHash("9g3qrkju0")
        )
    }

    /**
     * https://wiki.openstreetmap.org/wiki/Shortlink#Example
     */
    @Test
    fun decodeQuadTile_osmExample() {
        assertEquals(
            Triple(51.510772705078125, 0.054931640625, 9),
            decodeQuadTile("0EEQjE--"),
        )
        assertEquals(
            Triple(51.510998010635376, 0.05499601364135742, 16),
            decodeQuadTile("0EEQjEEb"),
        )
    }

    @Test
    fun decodeQuadTile_osmZoom() {
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13),
            decodeQuadTile("NuJWxJh"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11),
            decodeQuadTile("NuJWxJh-"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 12),
            decodeQuadTile("NuJWxJh--"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13),
            decodeQuadTile("NuJWxJh---"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11),
            decodeQuadTile("NuJWxJh----"),
        )
    }
}

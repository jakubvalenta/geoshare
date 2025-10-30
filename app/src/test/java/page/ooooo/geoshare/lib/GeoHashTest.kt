package page.ooooo.geoshare.lib

import org.junit.Assert
import org.junit.Test

@Suppress("SpellCheckingInspection")
class GeoHashTest {
    @Test
    fun decodeWazeGeoHash_returnsZoomBasedOnHashLength() {
        listOf(0, 0, 1, 3, 6, 8, 11, 13, 16, 18).forEachIndexed { i, expectedZoom ->
            Assert.assertEquals(expectedZoom, decodeWazeGeoHash("9".repeat(i + 1)).third)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLength() {
        listOf(0, 0, 1, 4, 7, 10, 13, 16, 19, 22).forEachIndexed { i, expectedZoom ->
            Assert.assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1)).third)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLengthDecreasedByOne() {
        listOf(0, 0, 0, 3, 6, 9, 12, 15, 18, 21).forEachIndexed { i, expectedZoom ->
            Assert.assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1) + "--").third)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLengthDecreasedByTwo() {
        listOf(0, 0, 0, 2, 5, 8, 11, 14, 17, 20).forEachIndexed { i, expectedZoom ->
            Assert.assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1) + "-").third)
        }
    }

    /**
     * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
     */
    @Test
    fun decodeWazeGeoHash_wikipediaExample() {
        Assert.assertEquals(
            Triple(42.60498046875, -5.60302734375, 6),
            decodeWazeGeoHash("ezs42")
        )
    }

    @Test
    fun decodeWazeGeoHash_examples() {
        Assert.assertEquals(
            Triple(52.77714014053345, 11.951086521148682, 16),
            decodeWazeGeoHash("u32kpzy90")
        )
        Assert.assertEquals(
            Triple(-12.58159875869751, 15.74390172958374, 16),
            decodeWazeGeoHash("kmg0qm86g")
        )
        Assert.assertEquals(
            Triple(22.65310049057007, 88.34357500076294, 16),
            decodeWazeGeoHash("tunbf7cgf")
        )
        Assert.assertEquals(
            Triple(19.40256357192993, -99.16566610336304, 16),
            decodeWazeGeoHash("9g3qrkju0")
        )
    }

    /**
     * https://wiki.openstreetmap.org/wiki/Shortlink#Example
     */
    @Test
    fun decodeOpenStreetMapQuadTileHash_osmExample() {
        Assert.assertEquals(
            Triple(51.510772705078125, 0.054931640625, 9),
            decodeOpenStreetMapQuadTileHash("0EEQjE--"),
        )
        Assert.assertEquals(
            Triple(51.510998010635376, 0.05499601364135742, 16),
            decodeOpenStreetMapQuadTileHash("0EEQjEEb"),
        )
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_osmZoom() {
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13),
            decodeOpenStreetMapQuadTileHash("NuJWxJh"),
        )
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11),
            decodeOpenStreetMapQuadTileHash("NuJWxJh-"),
        )
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 12),
            decodeOpenStreetMapQuadTileHash("NuJWxJh--"),
        )
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13),
            decodeOpenStreetMapQuadTileHash("NuJWxJh---"),
        )
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11),
            decodeOpenStreetMapQuadTileHash("NuJWxJh----"),
        )
    }

    @Test
    fun decodeGe0Hash_examples() {
        Assert.assertEquals(
            // Reference implementation returns 51.000001, -108.999988 due to different rounding algorithm
            Triple(51.000000312924385, -108.99998679757118, 4),
            decodeGe0Hash("ApYSV0YTAl"),
        )
        Assert.assertEquals(
            // Reference implementation returns -18.924943, 46.441639 due to different rounding algorithm
            Triple(-18.924943283200264, 46.441640406847, 4),
            decodeGe0Hash("AbCMCNp0LO"),
        )
        Assert.assertEquals(
            // Reference implementation returns 52.4877386, 13.3815234 due to different rounding algorithm
            Triple(52.48773850500584, 13.381523340940475, 14),
            decodeGe0Hash("o4MnIOApKp"),
        )
        Assert.assertEquals(
            // Reference implementation returns 40.71274, -74.0059965 due to different rounding algorithm.
            Triple(40.71274034678936, -74.00599703192711, 9),
            decodeGe0Hash("Umse5f0H8a"),
        )
        Assert.assertEquals(
            // Reference implementation returns 40.71274, -74.0059965 due to different rounding algorithm.
            Triple(40.71274034678936, -74.00599703192711, 5),
            decodeGe0Hash("Emse5f0H8a"),
        )
    }
}

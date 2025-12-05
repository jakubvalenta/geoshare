package page.ooooo.geoshare.lib.geo

import org.junit.Assert
import org.junit.Test

class GeoHashTest {
    @Test
    fun decodeWazeGeoHash_returnsZoomBasedOnHashLength() {
        listOf(0.0, 0.0, 1.0, 3.0, 6.0, 8.0, 11.0, 13.0, 16.0, 18.0).forEachIndexed { i, expectedZoom ->
            Assert.assertEquals(expectedZoom, decodeWazeGeoHash("9".repeat(i + 1)).third, 0.0)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLength() {
        listOf(0.0, 0.0, 1.0, 4.0, 7.0, 10.0, 13.0, 16.0, 19.0, 22.0).forEachIndexed { i, expectedZoom ->
            Assert.assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1)).third, 0.0)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLengthDecreasedByOne() {
        listOf(0.0, 0.0, 0.0, 3.0, 6.0, 9.0, 12.0, 15.0, 18.0, 21.0).forEachIndexed { i, expectedZoom ->
            Assert.assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1) + "--").third, 0.0)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLengthDecreasedByTwo() {
        listOf(0.0, 0.0, 0.0, 2.0, 5.0, 8.0, 11.0, 14.0, 17.0, 20.0).forEachIndexed { i, expectedZoom ->
            Assert.assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1) + "-").third, 0.0)
        }
    }

    /**
     * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
     */
    @Test
    fun decodeWazeGeoHash_wikipediaExample() {
        Assert.assertEquals(
            Triple(42.60498046875, -5.60302734375, 6.0),
            decodeWazeGeoHash("ezs42")
        )
    }

    @Test
    fun decodeWazeGeoHash_examples() {
        Assert.assertEquals(
            Triple(52.77714014053345, 11.951086521148682, 16.0),
            @Suppress("SpellCheckingInspection")
            (decodeWazeGeoHash("u32kpzy90"))
        )
        Assert.assertEquals(
            Triple(-12.58159875869751, 15.74390172958374, 16.0),
            decodeWazeGeoHash("kmg0qm86g")
        )
        Assert.assertEquals(
            Triple(22.65310049057007, 88.34357500076294, 16.0),
            @Suppress("SpellCheckingInspection")
            (decodeWazeGeoHash("tunbf7cgf"))
        )
        Assert.assertEquals(
            Triple(19.40256357192993, -99.16566610336304, 16.0),
            @Suppress("SpellCheckingInspection")
            (decodeWazeGeoHash("9g3qrkju0"))
        )
    }

    /**
     * https://wiki.openstreetmap.org/wiki/Shortlink#Example
     */
    @Test
    fun decodeOpenStreetMapQuadTileHash_osmExample() {
        Assert.assertEquals(
            Triple(51.510772705078125, 0.054931640625, 9.0),
            decodeOpenStreetMapQuadTileHash("0EEQjE--"),
        )
        Assert.assertEquals(
            Triple(51.510998010635376, 0.05499601364135742, 16.0),
            decodeOpenStreetMapQuadTileHash("0EEQjEEb"),
        )
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_osmZoom() {
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13.0),
            decodeOpenStreetMapQuadTileHash("NuJWxJh"),
        )
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11.0),
            decodeOpenStreetMapQuadTileHash("NuJWxJh-"),
        )
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 12.0),
            decodeOpenStreetMapQuadTileHash("NuJWxJh--"),
        )
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13.0),
            decodeOpenStreetMapQuadTileHash("NuJWxJh---"),
        )
        Assert.assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11.0),
            decodeOpenStreetMapQuadTileHash("NuJWxJh----"),
        )
    }

    @Test
    fun decodeGe0Hash_examples() {
        Assert.assertEquals(
            Triple(51.00000044424087, -108.99998679757118, 4.0),
            decodeGe0Hash("ApYSV0YTAl"),
        )
        Assert.assertEquals(
            Triple(-18.924943217006458, 46.441640406847, 4.0),
            decodeGe0Hash("AbCMCNp0LO"),
        )
        Assert.assertEquals(
            Triple(52.48773863770788, 13.381523340940475, 14.0),
            decodeGe0Hash("o4MnIOApKp"),
        )
        Assert.assertEquals(
            Triple(40.712740468525084, -74.00599703192711, 9.0),
            @Suppress("SpellCheckingInspection")
            (decodeGe0Hash("Umse5f0H8a")),
        )
        Assert.assertEquals(
            Triple(40.712740468525084, -74.00599703192711, 5.0),
            @Suppress("SpellCheckingInspection")
            (decodeGe0Hash("Emse5f0H8a")),
        )
    }
}

package page.ooooo.geoshare.lib.geo

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Source

class GeoHashTest {
    @Test
    fun decodeWazeGeoHash_returnsZoomBasedOnHashLength() {
        listOf(0.0, 0.0, 1.0, 3.0, 6.0, 8.0, 11.0, 13.0, 16.0, 18.0).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeWazeGeoHash("9".repeat(i + 1)).z!!, 0.0)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLength() {
        listOf(0.0, 0.0, 1.0, 4.0, 7.0, 10.0, 13.0, 16.0, 19.0, 22.0).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1)).z!!, 0.0)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLengthDecreasedByOne() {
        listOf(0.0, 0.0, 0.0, 3.0, 6.0, 9.0, 12.0, 15.0, 18.0, 21.0).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1) + "--").z!!, 0.0)
        }
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_returnsZoomBasedOnHashLengthDecreasedByTwo() {
        listOf(0.0, 0.0, 0.0, 2.0, 5.0, 8.0, 11.0, 14.0, 17.0, 20.0).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, decodeOpenStreetMapQuadTileHash("9".repeat(i + 1) + "-").z!!, 0.0)
        }
    }

    /**
     * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
     */
    @Test
    fun decodeWazeGeoHash_wikipediaExample() {
        assertEquals(
            NaivePoint(42.60498046875, -5.60302734375, 6.0, source = Source.HASH),
            decodeWazeGeoHash("ezs42")
        )
    }

    @Test
    fun decodeWazeGeoHash_examples() {
        assertEquals(
            NaivePoint(52.77714014053345, 11.951086521148682, 16.0, source = Source.HASH),
            @Suppress("SpellCheckingInspection")
            (decodeWazeGeoHash("u32kpzy90"))
        )
        assertEquals(
            NaivePoint(-12.58159875869751, 15.74390172958374, 16.0, source = Source.HASH),
            decodeWazeGeoHash("kmg0qm86g")
        )
        assertEquals(
            NaivePoint(22.65310049057007, 88.34357500076294, 16.0, source = Source.HASH),
            @Suppress("SpellCheckingInspection")
            (decodeWazeGeoHash("tunbf7cgf"))
        )
        assertEquals(
            NaivePoint(19.40256357192993, -99.16566610336304, 16.0, source = Source.HASH),
            @Suppress("SpellCheckingInspection")
            (decodeWazeGeoHash("9g3qrkju0"))
        )
    }

    /**
     * https://wiki.openstreetmap.org/wiki/Shortlink#Example
     */
    @Test
    fun decodeOpenStreetMapQuadTileHash_osmExample() {
        assertEquals(
            NaivePoint(51.510772705078125, 0.054931640625, 9.0, source = Source.HASH),
            decodeOpenStreetMapQuadTileHash("0EEQjE--"),
        )
        assertEquals(
            NaivePoint(51.510998010635376, 0.05499601364135742, 16.0, source = Source.HASH),
            decodeOpenStreetMapQuadTileHash("0EEQjEEb"),
        )
    }

    @Test
    fun decodeOpenStreetMapQuadTileHash_osmZoom() {
        assertEquals(
            NaivePoint(-16.23152732849121, -49.08348083496094, 13.0, source = Source.HASH),
            decodeOpenStreetMapQuadTileHash("NuJWxJh"),
        )
        assertEquals(
            NaivePoint(-16.23152732849121, -49.08348083496094, 11.0, source = Source.HASH),
            decodeOpenStreetMapQuadTileHash("NuJWxJh-"),
        )
        assertEquals(
            NaivePoint(-16.23152732849121, -49.08348083496094, 12.0, source = Source.HASH),
            decodeOpenStreetMapQuadTileHash("NuJWxJh--"),
        )
        assertEquals(
            NaivePoint(-16.23152732849121, -49.08348083496094, 13.0, source = Source.HASH),
            decodeOpenStreetMapQuadTileHash("NuJWxJh---"),
        )
        assertEquals(
            NaivePoint(-16.23152732849121, -49.08348083496094, 11.0, source = Source.HASH),
            decodeOpenStreetMapQuadTileHash("NuJWxJh----"),
        )
    }

    @Test
    fun decodeGe0Hash_examples() {
        assertEquals(
            NaivePoint(51.00000044424087, -108.99998679757118, 4.0, source = Source.HASH),
            decodeGe0Hash("ApYSV0YTAl"),
        )
        assertEquals(
            NaivePoint(-18.924943217006458, 46.441640406847, 4.0, source = Source.HASH),
            decodeGe0Hash("AbCMCNp0LO"),
        )
        assertEquals(
            NaivePoint(52.48773863770788, 13.381523340940475, 14.0, source = Source.HASH),
            decodeGe0Hash("o4MnIOApKp"),
        )
        assertEquals(
            NaivePoint(40.712740468525084, -74.00599703192711, 9.0, source = Source.HASH),
            @Suppress("SpellCheckingInspection")
            (decodeGe0Hash("Umse5f0H8a")),
        )
        assertEquals(
            NaivePoint(40.712740468525084, -74.00599703192711, 5.0, source = Source.HASH),
            @Suppress("SpellCheckingInspection")
            (decodeGe0Hash("Emse5f0H8a")),
        )
    }
}

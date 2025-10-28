package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.converters.OpenStreetMapUrlConverter
import page.ooooo.geoshare.lib.converters.OrganicMapsUrlConverter
import page.ooooo.geoshare.lib.converters.WazeUrlConverter

@Suppress("SpellCheckingInspection")
class GeoHashTest {
    @Test
    fun decodeWazeGeoHash_returnsZoomBasedOnHashLength() {
        listOf(0, 0, 1, 3, 6, 8, 11, 13, 16, 18).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, WazeUrlConverter.decodeGeoHash("9".repeat(i + 1)).third)
        }
    }

    @Test
    fun decodeOpenStreetMapGeoHash_returnsZoomBasedOnHashLength() {
        listOf(0, 0, 1, 4, 7, 10, 13, 16, 19, 22).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, OpenStreetMapUrlConverter.decodeGeoHash("9".repeat(i + 1)).third)
        }
    }

    @Test
    fun decodeOpenStreetMapGeoHash_returnsZoomBasedOnHashLengthDecreasedByOne() {
        listOf(0, 0, 0, 3, 6, 9, 12, 15, 18, 21).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, OpenStreetMapUrlConverter.decodeGeoHash("9".repeat(i + 1) + "--").third)
        }
    }

    @Test
    fun decodeOpenStreetMapGeoHash_returnsZoomBasedOnHashLengthDecreasedByTwo() {
        listOf(0, 0, 0, 2, 5, 8, 11, 14, 17, 20).forEachIndexed { i, expectedZoom ->
            assertEquals(expectedZoom, OpenStreetMapUrlConverter.decodeGeoHash("9".repeat(i + 1) + "-").third)
        }
    }

    /**
     * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
     */
    @Test
    fun decodeWazeGeoHash_wikipediaExample() {
        assertEquals(
            Triple(42.60498046875, -5.60302734375, 6),
            WazeUrlConverter.decodeGeoHash("ezs42")
        )
    }

    @Test
    fun decodeWazeGeoHash_waze() {
        assertEquals(
            Triple(52.77714014053345, 11.951086521148682, 16),
            WazeUrlConverter.decodeGeoHash("u32kpzy90")
        )
        assertEquals(
            Triple(-12.58159875869751, 15.74390172958374, 16),
            WazeUrlConverter.decodeGeoHash("kmg0qm86g")
        )
        assertEquals(
            Triple(22.65310049057007, 88.34357500076294, 16),
            WazeUrlConverter.decodeGeoHash("tunbf7cgf")
        )
        assertEquals(
            Triple(19.40256357192993, -99.16566610336304, 16),
            WazeUrlConverter.decodeGeoHash("9g3qrkju0")
        )
    }

    /**
     * https://wiki.openstreetmap.org/wiki/Shortlink#Example
     */
    @Test
    fun decodeOpenStreetMapGeoHash_osmExample() {
        assertEquals(
            Triple(51.510772705078125, 0.054931640625, 9),
            OpenStreetMapUrlConverter.decodeGeoHash("0EEQjE--"),
        )
        assertEquals(
            Triple(51.510998010635376, 0.05499601364135742, 16),
            OpenStreetMapUrlConverter.decodeGeoHash("0EEQjEEb"),
        )
    }

    @Test
    fun decodeOpenStreetMapGeoHash_osmZoom() {
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13),
            OpenStreetMapUrlConverter.decodeGeoHash("NuJWxJh"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11),
            OpenStreetMapUrlConverter.decodeGeoHash("NuJWxJh-"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 12),
            OpenStreetMapUrlConverter.decodeGeoHash("NuJWxJh--"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 13),
            OpenStreetMapUrlConverter.decodeGeoHash("NuJWxJh---"),
        )
        assertEquals(
            Triple(-16.23152732849121, -49.08348083496094, 11),
            OpenStreetMapUrlConverter.decodeGeoHash("NuJWxJh----"),
        )
    }

    @Test
    fun decodeOrganicMapsGeoHash() {
        assertEquals(
            Triple(40.71274034678936, -74.00599703192711, 9), // Should be 40.71274, -74.0059965
            OrganicMapsUrlConverter.decodeGeoHash("Umse5f0H8a"),
        )
        assertEquals(
            Triple(40.71274034678936, -74.00599703192711, 5), // Should be 40.71274, -74.0059965
            OrganicMapsUrlConverter.decodeGeoHash("Emse5f0H8a"),
        )
    }
}

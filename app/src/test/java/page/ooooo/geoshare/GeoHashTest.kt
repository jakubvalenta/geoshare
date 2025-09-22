package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.decodeGeoHash

@Suppress("SpellCheckingInspection")
class GeoHashTest {
    /**
     * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
     */
    @Test
    fun wikipediaExample() {
        assertEquals(42.60498 to -5.603027, decodeGeoHash("ezs42"))
    }

    @Test
    fun waze() {
        assertEquals(52.77714 to 11.951087, decodeGeoHash("u32kpzy90"))
        assertEquals(-12.581599 to 15.743902, decodeGeoHash("kmg0qm86g"))
        assertEquals(22.6531 to 88.343575, decodeGeoHash("tunbf7cgf"))
        assertEquals(19.402564 to -99.165666, decodeGeoHash("9g3qrkju0"))
    }
}

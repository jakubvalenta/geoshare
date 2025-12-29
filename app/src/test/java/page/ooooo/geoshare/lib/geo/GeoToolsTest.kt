package page.ooooo.geoshare.lib.geo

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog

class GeoToolsTest {
    private val log = FakeLog

    @Test
    fun exactIsPointInChina_pointIsInChina_returnsTrue() {
        assertTrue(exactIsPointInChina(116.331538, 39.920439, log))
    }

    @Test
    fun exactIsPointInChina_pointIsInJapan_returnsFalse() {
        assertFalse(exactIsPointInChina(133.7583428, 34.5945482, log))
    }

    @Test
    fun quickIsPointInChina_pointIsInChina_returnsTrue() {
        assertTrue(quickIsPointInChina(116.331538, 39.920439))
    }

    @Test
    fun quickIsPointInChina_pointIsInJapan_returnsTrue() {
        assertTrue(quickIsPointInChina(133.7583428, 34.5945482))
    }
}

package page.ooooo.geoshare.lib.geo

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog

class GeoToolsTest {
    private val log = FakeLog()

    @Test
    fun isPointInChina_pointIsInChina_returnsTrue() {
        assertTrue(isPointInChina(116.331538, 39.920439, log))
    }

    @Test
    fun isPointInChina_pointIsInJapan_returnsFalse() {
        assertFalse(isPointInChina(133.7583428, 34.5945482, log))
    }
}

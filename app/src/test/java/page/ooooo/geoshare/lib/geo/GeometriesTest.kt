package page.ooooo.geoshare.lib.geo

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometriesTest {
    @Test
    fun china_containsPoint_pointIsWithinMainlandChina_returnsTrue() {
        assertTrue(mockGeometries.greaterChina.containsPoint(116.331538, 39.920439))
    }

    @Test
    fun china_containsPoint_pointIsInWesternJapan_returnsFalse() {
        assertFalse(mockGeometries.greaterChina.containsPoint(133.7583428, 34.5945482))
    }
}

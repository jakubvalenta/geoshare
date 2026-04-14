package page.ooooo.geoshare.lib.geo

import android.content.Context
import android.content.res.AssetManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class GeometriesTest {
    private val mockAssetManager: AssetManager = mock {
        on { open("greater_china.wkb") } doReturn
            (GeometriesTest::class.java.getResourceAsStream("/greater_china.wkb")
                ?: error("greater_china.wkb not found in test resources"))
        on { open("macao.wkb") } doReturn
            (GeometriesTest::class.java.getResourceAsStream("/macao.wkb")
                ?: error("macao.wkb not found in test resources"))
        on { open("taiwan.wkb") } doReturn
            (GeometriesTest::class.java.getResourceAsStream("/taiwan.wkb")
                ?: error("taiwan.wkb not found in test resources"))
    }
    private val mockContext: Context = mock {
        on { assets } doReturn mockAssetManager
    }
    private val geometries = Geometries(mockContext)

    @Test
    fun china_containsPoint_pointIsWithinMainlandChina_returnsTrue() {
        assertTrue(geometries.greaterChina.containsPoint(116.331538, 39.920439))
    }

    @Test
    fun china_containsPoint_pointIsInWesternJapan_returnsFalse() {
        assertFalse(geometries.greaterChina.containsPoint(133.7583428, 34.5945482))
    }
}

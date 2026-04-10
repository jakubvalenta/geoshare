package page.ooooo.geoshare.lib.geo

import android.content.Context
import android.content.res.AssetManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ChinaGeometryTest {
    private val mockAssetManager: AssetManager = mock {
        on { open("china.wkb") } doReturn
            (ChinaGeometryTest::class.java.getResourceAsStream("/china.wkb")
                ?: error("china.wkb not found in test resources"))
    }
    private val mockContext: Context = mock {
        on { assets } doReturn mockAssetManager
    }
    private val chinaGeometry = ChinaGeometry(mockContext)

    @Test
    fun containsPoint_pointIsInChina_returnsTrue() {
        assertTrue(chinaGeometry.containsPoint(116.331538, 39.920439))
    }

    @Test
    fun containsPoint_pointIsInWesternJapan_returnsFalse() {
        assertFalse(chinaGeometry.containsPoint(133.7583428, 34.5945482))
    }
}

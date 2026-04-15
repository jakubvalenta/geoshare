package page.ooooo.geoshare.lib.geo

import android.content.Context
import android.content.res.AssetManager
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

interface GeoTest {
    fun mockGeometries(): Geometries {
        val mockAssetManager: AssetManager = mock {
            on { open("greater_china.wkb") } doReturn
                (GeometriesTest::class.java.getResourceAsStream("/greater_china.wkb")
                    ?: error("greater_china.wkb not found in test resources"))
            on { open("hong_kong.wkb") } doReturn
                (GeometriesTest::class.java.getResourceAsStream("/hong_kong.wkb")
                    ?: error("hong_kong.wkb not found in test resources"))
            on { open("macao.wkb") } doReturn
                (GeometriesTest::class.java.getResourceAsStream("/macao.wkb")
                    ?: error("macao.wkb not found in test resources"))
            on { open("taiwan.wkb") } doReturn
                (GeometriesTest::class.java.getResourceAsStream("/taiwan.wkb")
                    ?: error("taiwan.wkb not found in test resources"))
        }
        val mockContext: Context = mock {
            on { assets } doReturn mockAssetManager
        }
        return Geometries(mockContext)
    }
}

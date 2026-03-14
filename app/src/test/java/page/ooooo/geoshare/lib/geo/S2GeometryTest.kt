package page.ooooo.geoshare.lib.geo

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.point.NaivePoint

class S2GeometryTest {
    @Test
    fun decodeS2CellId_whenIdIsInEurope_returnsPoint() {
        assertEquals(
            NaivePoint(52.485822218541934, 13.423688319399824),
            decodeS2CellId(0x47a84fb831937021u),
        )
    }

    @Test
    fun decodeS2CellId_whenIdIsInChina_returnsPoint() {
        assertEquals(
            NaivePoint(39.916947439886265, 116.39073095659673),
            decodeS2CellId(0x35f052e94515d43du),
        )
    }

    @Test
    fun decodeS2CellId_whenIdIsInJapan_returnsPoint() {
        assertEquals(
            NaivePoint(34.5945681010353, 133.75838190375345),
            decodeS2CellId(0x3551565394d96b57u),
        )
    }

    @Test
    fun decodeS2CellId_whenIdIsVeryLowOrHighValue_doesNotThrowException() {
        assertEquals(
            NaivePoint(-35.264389682754654, -45.0),
            decodeS2CellId(ULong.MIN_VALUE),
        )
        assertEquals(
            NaivePoint(-35.264389716294055, lon = -45.0),
            decodeS2CellId(ULong.MAX_VALUE),
        )
    }
}

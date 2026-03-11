package page.ooooo.geoshare.lib.geo

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.point.NaivePoint

class S2GeometryTest {
    @Test
    fun decodeS2CellId_whenIdIsReasonableValue_returnsPoint() {
        assertEquals(
            NaivePoint(52.485822218541934, 13.423688319399824),
            decodeS2CellId(0x47a84fb831937021),
        )
    }

    @Test
    fun decodeS2CellId_whenIdIsStrangeValue_doesNotThrowException() {
        assertEquals(
            NaivePoint(-35.264389682754654, -45.0),
            decodeS2CellId(0),
        )
        assertEquals(
            NaivePoint(-35.264389716294055, -45.0),
            decodeS2CellId(-1),
        )
        assertEquals(
            NaivePoint(35.26438966598495, -135.00000003557392),
            decodeS2CellId(Long.MAX_VALUE),
        )
    }
}

package page.ooooo.geoshare.lib.formatters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class PlusCodeFormatterTest {
    @Test
    fun formatPlusCode_pointIsOutsideMainlandChina() {
        assertEquals(
            "9C2C4VFG+9JM",
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(50.123456, -11.123456, source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun formatPlusCode_pointIsWithinMainlandChina() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "8PFRW98W+WRG",
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(39.917313, 116.397063, source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun formatPlusCode_pointIsWithinWesternJapan() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "8Q6MHQV5+R88",
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(34.5945482, 133.7583428, source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun formatPlusCode_pointHasInvalidCoordinates() {
        assertEquals(
            "C6XXX2X2+X2R",
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(999.0, 999.0, source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun formatPlusCode_pointIsEmpty_returnsNull() {
        assertNull(
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(source = Source.GENERATED)
            )
        )
    }
}

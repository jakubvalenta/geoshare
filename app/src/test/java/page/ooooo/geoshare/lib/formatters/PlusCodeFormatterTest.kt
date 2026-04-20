package page.ooooo.geoshare.lib.formatters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class PlusCodeFormatterTest {
    @Test
    fun formatPlusCode_pointIsOutsideMainlandChina() {
        assertEquals(
            "9C2C4VFG+9JM",
            PlusCodeFormatter.formatPlusCode(WGS84Point(50.123456, -11.123456, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatPlusCode_pointIsWithinMainlandChina() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "8PFRW98R+88P", // TODO Fix coordinate system
            PlusCodeFormatter.formatPlusCode(WGS84Point(39.915833, 116.390833, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatPlusCode_pointIsWithinWesternJapan() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "CGXPXHXV+XRV", // TODO Fix invalid code
            PlusCodeFormatter.formatPlusCode(WGS84Point(133.7583428, 34.5945482, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatPlusCode_pointHasInvalidCoordinates() {
        assertEquals(
            "C6XXX2X2+X2R",
            PlusCodeFormatter.formatPlusCode(WGS84Point(999.0, 999.0, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatPlusCode_pointIsEmpty_returnsNull() {
        assertNull(PlusCodeFormatter.formatPlusCode(WGS84Point(source = Source.GENERATED)))
    }
}

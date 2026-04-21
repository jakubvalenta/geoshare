package page.ooooo.geoshare.lib.formatters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class PlusCodeFormatterTest {
    @Test
    fun formatPlusCode_and_formatGoogleMapsPlusCodeUri_pointIsOutsideMainlandChina() {
        assertEquals(
            "9C2C4VFG+9JM",
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(50.123456, -11.123456, source = Source.GENERATED)
            ),
        )
        assertEquals(
            "https://www.google.com/maps/place/9C2C4VFG%2B9JM",
            PlusCodeFormatter.formatGoogleMapsPlusCodeUri(
                GCJ02MainlandChinaPoint(50.123456, -11.123456, source = Source.GENERATED),
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatPlusCode_and_formatGoogleMapsPlusCodeUri_pointIsWithinMainlandChina() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "8PFRW98W+WRG",
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(39.917313, 116.397063, source = Source.GENERATED)
            ),
        )
        assertEquals(
            "https://www.google.com/maps/place/8PFRW98W%2BWRG",
            PlusCodeFormatter.formatGoogleMapsPlusCodeUri(
                GCJ02MainlandChinaPoint(39.917313, 116.397063, source = Source.GENERATED),
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatPlusCode_and_formatGoogleMapsPlusCodeUri_pointIsWithinWesternJapan() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "8Q6MHQV5+R88",
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(34.5945482, 133.7583428, source = Source.GENERATED)
            ),
        )
        assertEquals(
            "https://www.google.com/maps/place/8Q6MHQV5%2BR88",
            PlusCodeFormatter.formatGoogleMapsPlusCodeUri(
                GCJ02MainlandChinaPoint(34.5945482, 133.7583428, source = Source.GENERATED),
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatPlusCode_and_formatGoogleMapsPlusCodeUri_pointHasInvalidCoordinates() {
        assertEquals(
            "C6XXX2X2+X2R",
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(999.0, 999.0, source = Source.GENERATED)
            ),
        )
        assertEquals(
            "https://www.google.com/maps/place/C6XXX2X2%2BX2R",
            PlusCodeFormatter.formatGoogleMapsPlusCodeUri(
                GCJ02MainlandChinaPoint(999.0, 999.0, source = Source.GENERATED),
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatPlusCode_and_formatGoogleMapsPlusCodeUri_pointIsEmpty_returnsNull() {
        assertNull(
            PlusCodeFormatter.formatPlusCode(
                GCJ02MainlandChinaPoint(source = Source.GENERATED)
            )
        )
        assertNull(
            PlusCodeFormatter.formatGoogleMapsPlusCodeUri(
                GCJ02MainlandChinaPoint(source = Source.GENERATED),
                uriQuote = FakeUriQuote,
            )
        )
    }
}

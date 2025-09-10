package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.GeoUrlConverter
import page.ooooo.geoshare.lib.converters.UrlConverter

class GeoUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter: UrlConverter = GeoUrlConverter()

    @Test
    fun parseUrl_returnsAllCoordsAndParams() {
        assertEquals(
            Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"),
            parseUrl("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"),
        )
    }
}

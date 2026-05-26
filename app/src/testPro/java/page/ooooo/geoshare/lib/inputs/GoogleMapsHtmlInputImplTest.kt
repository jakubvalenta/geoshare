package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleMapsHtmlInputImplTest : InputTest {
    private val input = GoogleMapsHtmlInputImpl()

    @Test
    fun parse_returnsNoPoints() = runTest {
        assertEquals(ParseResult(), input.parse(Unit, "https://maps.google.com/foo"))
    }
}

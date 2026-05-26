package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleMapsPlaceListInputImplTest : InputTest {
    private val input = GoogleMapsPlaceListInputImpl()

    @Test
    fun parse_returnsNoPoints() = runTest {
        assertEquals(ParseResult(), input.parse("https://maps.google.com/foo"))
    }
}

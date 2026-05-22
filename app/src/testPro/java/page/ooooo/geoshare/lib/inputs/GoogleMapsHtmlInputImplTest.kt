package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleMapsHtmlInputImplTest {
    private val input = GoogleMapsHtmlInputImpl()

    @Test
    fun itIsNoopInput() {
        @Suppress("USELESS_IS_CHECK")
        assertTrue(input is NoopInput)
    }
}

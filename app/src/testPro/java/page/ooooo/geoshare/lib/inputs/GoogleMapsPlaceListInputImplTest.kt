package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleMapsPlaceListInputImplTest {
    private val input = GoogleMapsPlaceListInputImpl()

    @Test
    fun itIsNoopInput() {
        @Suppress("USELESS_IS_CHECK")
        assertTrue(input is NoopInput)
    }
}

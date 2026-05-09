package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BaiduMapWebViewInputTest : InputTest {
    private val input = BaiduMapWebViewInput

    @Test
    fun parse_returnsNextInput() = runTest {
        assertEquals(
            ParseResult(nextInput = BaiduMapUriInput),
            input.parse("foo"),
        )
    }
}

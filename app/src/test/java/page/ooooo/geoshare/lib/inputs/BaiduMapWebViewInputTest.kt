package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BaiduMapWebViewInputTest : InputTest {
    private val input = BaiduMapWebViewInput

    @Test
    fun parse_returnsNextStep() = runTest {
        assertEquals(
            ParseResult(nextStep = NextStep(BaiduMapUriInput, "https://map.baidu.com/redirected")),
            input.parse("https://map.baidu.com/redirected", "https://map.baidu.com/original"),
        )
    }
}

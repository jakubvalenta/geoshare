package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository

class YahooMapsShortLinkInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.yahooMapsShortLinkInput

    @Test
    fun match_correct() {
        assertEquals("https://yahoo.jp/qDc2x2", input.match("https://yahoo.jp/qDc2x2"))
        assertEquals("yahoo.jp/qDc2x2", input.match("yahoo.jp/qDc2x2"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/qDc2x2"))
    }

    @Test
    fun parse_returnsNextStep() = runTest {
        assertEquals(
            ParseResult.Success(
                next = MatchedInput(
                    FakeInputRepository.yahooMapsUriInput,
                    "https://map.yahoo.co.jp/place?gid=MBd0PFPhR-Y&lat=35.69548&lon=139.77065&zoom=17&maptype=basic"
                )
            ),
            input.parse("https://map.yahoo.co.jp/place?gid=MBd0PFPhR-Y&lat=35.69548&lon=139.77065&zoom=17&maptype=basic"),
        )
    }
}

package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.inputs.ParseHtmlResult
import page.ooooo.geoshare.lib.inputs.buildParseHtmlResult
import page.ooooo.geoshare.lib.point.WGS84Point

class ParseResultTest {

    @Test
    fun parseHtmlResultBuilder_lastPointHasNameOnlyAndRedirectUriStringIsNull_returnsSucceeded() = runTest {
        val points = persistentListOf(
            WGS84Point(1.0, 2.0),
            WGS84Point(name = "foo bar"),
        )
        assertEquals(
            ParseHtmlResult.Succeeded(points),
            buildParseHtmlResult {
                this.points = points
            },
        )
    }
}

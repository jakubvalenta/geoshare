package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

// TODO Update test case names
class ParseResultTest {
    @Test
    fun parseUriResult_from_whenPositionHasAPoint_returnsSucceeded() {
        assertEquals(
            ParseUriResult.from(persistentListOf(WGS84Point(1.0, 2.0))),
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(1.0, 2.0))),
        )
        assertEquals(
            ParseUriResult.from(persistentListOf(WGS84Point(1.0, 2.0)), "https://example.com/"),
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(1.0, 2.0))),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasEmptyPointsAndHtmlUriString_returnsSucceededAndSupportsHtmlParsing() {
        assertEquals(
            ParseUriResult.from(persistentListOf(), "https://example.com/"),
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                persistentListOf(),
                "https://example.com/",
            ),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasEmptyPointsAndNullHtmlUriStringAndQuery_returnsSucceeded() {
        assertEquals(
            ParseUriResult.from(persistentListOf(WGS84Point(name = "foo"))),
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "foo"))),
        )
        assertEquals(
            ParseUriResult.from(persistentListOf(WGS84Point(name = "foo"))),
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "foo"))),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasEmptyPointsAndNullHtmlUriStringAndNullOrEmptyQuery_returnsNull() {
        assertNull(ParseUriResult.from(persistentListOf()))
        assertNull(ParseUriResult.from(persistentListOf(WGS84Point(name = ""))))
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasAPoint_returnsSucceeded() {
        assertEquals(
            ParseHtmlResult.from(persistentListOf(), persistentListOf(WGS84Point(1.0, 2.0))),
            ParseHtmlResult.Succeeded(persistentListOf(WGS84Point(1.0, 2.0))),
        )
        assertEquals(
            ParseHtmlResult.from(persistentListOf(), persistentListOf(WGS84Point(1.0, 2.0)), "https://example.com/"),
            ParseHtmlResult.Succeeded(persistentListOf(WGS84Point(1.0, 2.0))),
        )
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasAPointAndPositionFromUriHasQuery_returnsSucceededWithQueryAsName() {
        assertEquals(
            ParseHtmlResult.from(
                persistentListOf(WGS84Point(name = "foo")),
                persistentListOf(WGS84Point(1.0, 2.0)),
            ),
            ParseHtmlResult.Succeeded(persistentListOf(WGS84Point(1.0, 2.0, name = "foo"))),
        )
        assertEquals(
            ParseHtmlResult.from(
                persistentListOf(WGS84Point(name = "foo")),
                persistentListOf(WGS84Point(1.0, 2.0)),
                "https://example.com/"
            ),
            ParseHtmlResult.Succeeded(persistentListOf(WGS84Point(1.0, 2.0, name = "foo"))),
        )
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasEmptyPointsAndRedirectUriString_returnsRequiresRedirect() {
        assertEquals(
            ParseHtmlResult.from(
                persistentListOf(),
                persistentListOf(),
                "https://example.com/"
            ),
            ParseHtmlResult.RequiresRedirect("https://example.com/"),
        )
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasEmptyPointsAndNullRedirectUriStringAndNullOrEmptyQuery_returnsNull() {
        assertNull(ParseHtmlResult.from(persistentListOf(), persistentListOf()))
        assertNull(ParseHtmlResult.from(persistentListOf(), persistentListOf(WGS84Point(name = ""))))
    }
}

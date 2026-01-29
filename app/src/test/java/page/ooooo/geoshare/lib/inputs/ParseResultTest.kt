package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.point.Position
import page.ooooo.geoshare.lib.point.Srs

class ParseResultTest {
    @Test
    fun parseUriResult_from_whenPositionHasAPoint_returnsSucceeded() {
        assertEquals(
            ParseUriResult.from(points(Srs.WGS84, 1.0, 2.0)),
            ParseUriResult.Succeeded(WGS84Point(1.0, 2.0)),
        )
        assertEquals(
            ParseUriResult.from(points(Srs.WGS84, 1.0, 2.0), "https://example.com/"),
            ParseUriResult.Succeeded(WGS84Point(1.0, 2.0)),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasNullOrEmptyPointsAndHtmlUriString_returnsSucceededAndSupportsHtmlParsing() {
        assertEquals(
            ParseUriResult.from(points(points = null), "https://example.com/"),
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(points = null),
                "https://example.com/",
            ),
        )
        assertEquals(
            ParseUriResult.from(points(points = persistentListOf()), "https://example.com/"),
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(points = persistentListOf()),
                "https://example.com/",
            ),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasNullOrEmptyPointsAndNullHtmlUriStringAndQuery_returnsSucceeded() {
        assertEquals(
            ParseUriResult.from(points(points = null, q = "foo")),
            ParseUriResult.Succeeded(Position(points = null, q = "foo")),
        )
        assertEquals(
            ParseUriResult.from(points(points = persistentListOf(), q = "foo")),
            ParseUriResult.Succeeded(Position(points = persistentListOf(), q = "foo")),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasNullOrEmptyPointsAndNullHtmlUriStringAndNullOrEmptyQuery_returnsNull() {
        assertNull(ParseUriResult.from(points(points = null, q = null)))
        assertNull(ParseUriResult.from(points(points = null, q = "")))
        assertNull(ParseUriResult.from(points(points = persistentListOf(), q = null)))
        assertNull(ParseUriResult.from(points(points = persistentListOf(), q = "")))
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasAPoint_returnsSucceeded() {
        assertEquals(
            ParseHtmlResult.from(Position(), WGS84Point(1.0, 2.0)),
            ParseHtmlResult.Succeeded(WGS84Point(1.0, 2.0)),
        )
        assertEquals(
            ParseHtmlResult.from(Position(), WGS84Point(1.0, 2.0), "https://example.com/"),
            ParseHtmlResult.Succeeded(WGS84Point(1.0, 2.0)),
        )
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasAPointAndPositionFromUriHasQuery_returnsSucceededWithQueryAsName() {
        assertEquals(
            ParseHtmlResult.from(Position(q = "foo"), WGS84Point(1.0, 2.0)),
            ParseHtmlResult.Succeeded(WGS84Point(1.0, 2.0, name = "foo")),
        )
        assertEquals(
            ParseHtmlResult.from(Position(q = "foo"), WGS84Point(1.0, 2.0), "https://example.com/"),
            ParseHtmlResult.Succeeded(WGS84Point(1.0, 2.0, name = "foo")),
        )
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasNullOrEmptyPointsAndRedirectUriString_returnsRequiresRedirect() {
        assertEquals(
            ParseHtmlResult.from(Position(), Position(points = null), "https://example.com/"),
            ParseHtmlResult.RequiresRedirect("https://example.com/"),
        )
        assertEquals(
            ParseHtmlResult.from(Position(), Position(points = persistentListOf()), "https://example.com/"),
            ParseHtmlResult.RequiresRedirect("https://example.com/"),
        )
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasNullOrEmptyPointsAndNullRedirectUriStringAndNullOrEmptyQuery_returnsNull() {
        assertNull(ParseHtmlResult.from(Position(), Position(points = null, q = null)))
        assertNull(ParseHtmlResult.from(Position(), Position(points = null, q = "")))
        assertNull(ParseHtmlResult.from(Position(), Position(points = persistentListOf(), q = null)))
        assertNull(ParseHtmlResult.from(Position(), Position(points = persistentListOf(), q = "")))
    }
}

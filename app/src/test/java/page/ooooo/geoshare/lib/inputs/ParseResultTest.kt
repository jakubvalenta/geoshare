package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class ParseResultTest {
    @Test
    fun parseUriResult_from_whenPositionHasAPoint_returnsSucceeded() {
        assertEquals(
            ParseUriResult.from(Position(Srs.WGS84, 1.0, 2.0)),
            ParseUriResult.Succeeded(Position(Srs.WGS84, 1.0, 2.0)),
        )
        assertEquals(
            ParseUriResult.from(Position(Srs.WGS84, 1.0, 2.0), "https://example.com/"),
            ParseUriResult.Succeeded(Position(Srs.WGS84, 1.0, 2.0)),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasNullOrEmptyPointsAndHtmlUriString_returnsSucceededAndSupportsHtmlParsing() {
        assertEquals(
            ParseUriResult.from(Position(points = null), "https://example.com/"),
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(points = null),
                "https://example.com/",
            ),
        )
        assertEquals(
            ParseUriResult.from(Position(points = persistentListOf()), "https://example.com/"),
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(points = persistentListOf()),
                "https://example.com/",
            ),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasNullOrEmptyPointsAndNullHtmlUriStringAndQuery_returnsSucceeded() {
        assertEquals(
            ParseUriResult.from(Position(points = null, q = "foo")),
            ParseUriResult.Succeeded(Position(points = null, q = "foo")),
        )
        assertEquals(
            ParseUriResult.from(Position(points = persistentListOf(), q = "foo")),
            ParseUriResult.Succeeded(Position(points = persistentListOf(), q = "foo")),
        )
    }

    @Test
    fun parseUriResult_from_whenPositionHasNullOrEmptyPointsAndNullHtmlUriStringAndNullOrEmptyQuery_returnsNull() {
        assertNull(ParseUriResult.from(Position(points = null, q = null)))
        assertNull(ParseUriResult.from(Position(points = null, q = "")))
        assertNull(ParseUriResult.from(Position(points = persistentListOf(), q = null)))
        assertNull(ParseUriResult.from(Position(points = persistentListOf(), q = "")))
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasAPoint_returnsSucceeded() {
        assertEquals(
            ParseHtmlResult.from(Position(), Position(Srs.WGS84, 1.0, 2.0)),
            ParseHtmlResult.Succeeded(Position(Srs.WGS84, 1.0, 2.0)),
        )
        assertEquals(
            ParseHtmlResult.from(Position(), Position(Srs.WGS84, 1.0, 2.0), "https://example.com/"),
            ParseHtmlResult.Succeeded(Position(Srs.WGS84, 1.0, 2.0)),
        )
    }

    @Test
    fun parseHtmlResult_from_whenPositionHasAPointAndPositionFromUriHasQuery_returnsSucceededWithQueryAsName() {
        assertEquals(
            ParseHtmlResult.from(Position(q = "foo"), Position(Srs.WGS84, 1.0, 2.0)),
            ParseHtmlResult.Succeeded(Position(Srs.WGS84, 1.0, 2.0, name = "foo")),
        )
        assertEquals(
            ParseHtmlResult.from(Position(q = "foo"), Position(Srs.WGS84, 1.0, 2.0), "https://example.com/"),
            ParseHtmlResult.Succeeded(Position(Srs.WGS84, 1.0, 2.0, name = "foo")),
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

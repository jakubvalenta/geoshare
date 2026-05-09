package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class CartesIGNUriInputTest : InputTest {
    private val input = CartesIGNUriInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14",
            input.match("https://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14")
        )
        assertEquals(
            "cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14",
            input.match("cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14")
        )
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/?lng=-11.123456&lat=50.123456&z=3.14"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14",
            input.match("ftp://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://cartes-ign.ign.fr?q=foobar",
            input.match("https://cartes-ign.ign.fr?q=foobar ")
        )
        assertEquals(
            "https://cartes-ign.ign.fr?q=foo bar",
            input.match("https://cartes-ign.ign.fr?q=foo bar ")
        )
        assertEquals(
            "https://cartes-ign.ign.fr?q=foo",
            input.match("https://cartes-ign.ign.fr?q=foo  bar")
        )
        assertEquals(
            "https://cartes-ign.ign.fr?q=foo",
            input.match("https://cartes-ign.ign.fr?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("https://cartes-ign.ign.fr/"))
        assertEquals(ParseResult(), input.parse("https://cartes-ign.ign.fr/?spam=1"))
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(WGS84Point(50.123456, -11.123456, z = 3.14, source = Source.URI))
            ),
            input.parse("https://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14"),
        )
    }
}

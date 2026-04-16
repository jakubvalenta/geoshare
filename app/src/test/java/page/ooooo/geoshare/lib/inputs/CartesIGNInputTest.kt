package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class CartesIGNInputTest : InputTest {
    override val input = CartesIGNInput

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14",
            getUri("https://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14")
        )
        assertEquals(
            "cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14",
            getUri("cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14")
        )
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/?lng=-11.123456&lat=50.123456&z=3.14"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14",
            getUri("ftp://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14"),
        )
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertEquals(ParseUriResult(), parseUri("https://cartes-ign.ign.fr/"))
        assertEquals(ParseUriResult(), parseUri("https://cartes-ign.ign.fr/?spam=1"))
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(WGS84Point(50.123456, -11.123456, z = 3.14, source = Source.URI))
            ),
            parseUri("https://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14"),
        )
    }

}

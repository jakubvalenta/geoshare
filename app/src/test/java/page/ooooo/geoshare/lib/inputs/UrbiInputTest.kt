package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class UrbiInputTest : BaseInputTest() {
    override val input = UrbiInput

    @Test
    fun uriPattern_fullUrl() {
        // TODO Test all Urbi domains
        assertTrue(doesUriPatternMatch("https://2gis.uz/tashkent/firm/70000001060803297"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://go.2gis.com/WSTdK"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://example.com/tashkent/firm/70000001060803297"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://2gis.uz/tashkent/firm/70000001060803297"))
    }

    @Test
    fun parseUri_fullUrl() {
        assertEquals(
            Position(Srs.WGS84) to "https://2gis.uz/tashkent/firm/70000001060803297",
            parseUri("https://2gis.uz/tashkent/firm/70000001060803297"),
        )
    }

    @Test
    fun parseUri_shortLink() {
        assertEquals(
            Position(Srs.WGS84) to "https://go.2gis.com/WSTdK",
            parseUri("https://go.2gis.com/WSTdK"),
        )
    }

    @Test
    fun parseUri_api() {
        assertEquals(
            Position(Srs.WGS84, 41.285765, 69.234083, z = 17.0) to null,
            parseUri("https://share.api.2gis.ru/getimage?city=tashkent&zoom=17&center=69.234083%2C41.285765&title=Music%20Store%2C%20%D0%BC%D0%B0%D0%B3%D0%B0%D0%B7%D0%B8%D0%BD%20%D0%BC%D1%83%D0%B7%D1%8B%D0%BA%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%D0%BD%D1%81%D1%82%D1%80%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%BE%D0%B2&desc=%D0%A3%D0%BB%D0%B8%D1%86%D0%B0%20%D0%9C%D1%83%D0%BA%D0%B8%D0%BC%D0%B8%2C%C2%A098%D0%B0%3Cbr%20%2F%3E%D0%A2%D0%B0%D1%88%D0%BA%D0%B5%D0%BD%D1%82")
        )
    }

    @Test
    fun parseHtml() = runTest {
        assertEquals(
            Position(Srs.WGS84, 41.285765, 69.234083, z = 17.0) to null,
            parseHtml(
                """<html>
<head>
  <meta property="twitter:image" content="https://share.api.2gis.ru/getimage?city=tashkent&amp;zoom=17&amp;center=69.234083%2C41.285765&amp;title=Music%20Store%2C%20%D0%BC%D0%B0%D0%B3%D0%B0%D0%B7%D0%B8%D0%BD%20%D0%BC%D1%83%D0%B7%D1%8B%D0%BA%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%D0%BD%D1%81%D1%82%D1%80%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%BE%D0%B2&amp;desc=%D0%A3%D0%BB%D0%B8%D1%86%D0%B0%20%D0%9C%D1%83%D0%BA%D0%B8%D0%BC%D0%B8%2C%C2%A098%D0%B0%3Cbr%20%2F%3E%D0%A2%D0%B0%D1%88%D0%BA%D0%B5%D0%BD%D1%82" />
</head>
<body></body>
</html>
"""
            ),
        )
    }
}

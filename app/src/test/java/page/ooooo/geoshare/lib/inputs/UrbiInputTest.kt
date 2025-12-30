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
        assertTrue(doesUriPatternMatch("https://2gis.uz/tashkent/firm/70000001060803297"))
        assertTrue(doesUriPatternMatch("https://2gis.ae/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.am/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.az/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.cl/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.com.cy/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.com/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.cz/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.it/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.kg/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.kz/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.ru/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://2gis.uz/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://urbi-bh.com/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://urbi-eg.com/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://urbi-kw.com/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://urbi-om.com/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://urbi-qa.com/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://urbi-sa.com/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://urbi.bh/dubai/geo/55.171971%2C25.289452"))
        assertTrue(doesUriPatternMatch("https://urbi.qa/dubai/geo/55.171971%2C25.289452"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://go.2gis.com/WSTdK"))
        assertTrue(doesUriPatternMatch("https://go.urbi.ae/3JtpM"))
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
    fun parseUri_point() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 25.284889, 55.172173)),
            parseUri("https://maps.urbi.ae/dubai/geo/55.172173%2C25.284889"),
        )
    }

    @Test
    fun parseUri_pointWithMarker() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 25.25915, 55.225263, z = 12.77)),
            parseUri("https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452?m=55.225263%2C25.25915%2F12.77"),
        )
    }

    @Test
    fun parseUri_geo() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(Srs.WGS84),
                "https://maps.urbi.ae/dubai/geo/13933621232533580",
            ),
            parseUri("https://maps.urbi.ae/dubai/geo/13933621232533580"),
        )
    }

    @Test
    fun parseUri_firm() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(Srs.WGS84),
                "https://2gis.uz/tashkent/firm/70000001060803297",
            ),
            parseUri("https://2gis.uz/tashkent/firm/70000001060803297"),
        )
    }

    @Test
    fun parseUri_firmWithCoordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 25.19925, 55.332211)),
            parseUri("https://maps.urbi.ae/dubai/firm/70000001043503020/55.332211%2C25.19925"),
        )
    }

    @Test
    fun parseUri_firmWithMarker() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 25.196707, 55.320748, z = 14.82)),
            parseUri("https://maps.urbi.ae/dubai/firm/70000001043503020/55.332211%2C25.19925?m=55.320748%2C25.196707%2F14.82"),
        )
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(Position(Srs.WGS84), "https://go.2gis.com/WSTdK"),
            parseUri("https://go.2gis.com/WSTdK"),
        )
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(Position(Srs.WGS84), "https://go.urbi.ae/3JtpM"),
            parseUri("https://go.urbi.ae/3JtpM"),
        )
    }

    @Test
    fun parseUri_api() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 41.285765, 69.234083, z = 17.0)),
            parseUri("https://share.api.2gis.ru/getimage?city=tashkent&zoom=17&center=69.234083%2C41.285765&title=Music%20Store%2C%20%D0%BC%D0%B0%D0%B3%D0%B0%D0%B7%D0%B8%D0%BD%20%D0%BC%D1%83%D0%B7%D1%8B%D0%BA%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%D0%BD%D1%81%D1%82%D1%80%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%BE%D0%B2&desc=%D0%A3%D0%BB%D0%B8%D1%86%D0%B0%20%D0%9C%D1%83%D0%BA%D0%B8%D0%BC%D0%B8%2C%C2%A098%D0%B0%3Cbr%20%2F%3E%D0%A2%D0%B0%D1%88%D0%BA%D0%B5%D0%BD%D1%82"),
        )
    }

    @Test
    fun parseHtml() = runTest {
        assertEquals(
            ParseHtmlResult.Succeeded(Position(Srs.WGS84, 41.285765, 69.234083, z = 17.0)),
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

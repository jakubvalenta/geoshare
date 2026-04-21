package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class UrbiInputTest : InputTest {
    override val input = UrbiInput

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://2gis.uz/tashkent/firm/70000001060803297",
            getUri("https://2gis.uz/tashkent/firm/70000001060803297")
        )
        assertEquals(
            "https://2gis.ae/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.ae/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.am/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.am/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.az/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.az/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.cl/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.cl/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.com.cy/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.com.cy/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.com/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.cz/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.cz/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.it/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.it/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.kg/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.kg/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.kz/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.kz/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.ru/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.ru/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.uz/dubai/geo/55.171971%2C25.289452",
            getUri("https://2gis.uz/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452",
            getUri("https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-bh.com/dubai/geo/55.171971%2C25.289452",
            getUri("https://urbi-bh.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-eg.com/dubai/geo/55.171971%2C25.289452",
            getUri("https://urbi-eg.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-kw.com/dubai/geo/55.171971%2C25.289452",
            getUri("https://urbi-kw.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-om.com/dubai/geo/55.171971%2C25.289452",
            getUri("https://urbi-om.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-qa.com/dubai/geo/55.171971%2C25.289452",
            getUri("https://urbi-qa.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-sa.com/dubai/geo/55.171971%2C25.289452",
            getUri("https://urbi-sa.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi.bh/dubai/geo/55.171971%2C25.289452",
            getUri("https://urbi.bh/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi.qa/dubai/geo/55.171971%2C25.289452",
            getUri("https://urbi.qa/dubai/geo/55.171971%2C25.289452")
        )
    }

    @Test
    fun uriPattern_shortUrl() {
        assertEquals("https://go.2gis.com/WSTdK", getUri("https://go.2gis.com/WSTdK"))
        assertEquals("https://go.urbi.ae/3JtpM", getUri("https://go.urbi.ae/3JtpM"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://example.com/tashkent/firm/70000001060803297"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "2gis.uz/tashkent/firm/70000001060803297",
            getUri("ftp://2gis.uz/tashkent/firm/70000001060803297"),
        )
    }

    @Test
    fun uriPattern_spaces() {
        assertEquals(
            "https://maps.apple.com/?q=foobar",
            getUri("https://maps.apple.com/?q=foobar ")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo bar",
            getUri("https://maps.apple.com/?q=foo bar ")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo",
            getUri("https://maps.apple.com/?q=foo  bar")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo",
            getUri("https://maps.apple.com/?q=foo\tbar")
        )
    }

    @Test
    fun parseUri_point() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(25.284889, 55.172173, source = Source.URI))),
            parseUri("https://maps.urbi.ae/dubai/geo/55.172173%2C25.284889"),
        )
    }

    @Test
    fun parseUri_pointWithMarker() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(25.25915, 55.225263, z = 12.77, source = Source.URI))),
            parseUri("https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452?m=55.225263%2C25.25915%2F12.77"),
        )
    }

    @Test
    fun parseUri_geo() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://maps.urbi.ae/dubai/geo/13933621232533580",
            ),
            parseUri("https://maps.urbi.ae/dubai/geo/13933621232533580"),
        )
    }

    @Test
    fun parseUri_firm() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://2gis.uz/tashkent/firm/70000001060803297",
            ),
            parseUri("https://2gis.uz/tashkent/firm/70000001060803297"),
        )
    }

    @Test
    fun parseUri_firmWithCoordinates() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(25.19925, 55.332211, source = Source.URI))),
            parseUri("https://maps.urbi.ae/dubai/firm/70000001043503020/55.332211%2C25.19925"),
        )
    }

    @Test
    fun parseUri_firmWithMarker() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(25.196707, 55.320748, z = 14.82, source = Source.URI))),
            parseUri("https://maps.urbi.ae/dubai/firm/70000001043503020/55.332211%2C25.19925?m=55.320748%2C25.196707%2F14.82"),
        )
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://go.2gis.com/WSTdK",
            ),
            parseUri("https://go.2gis.com/WSTdK"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://go.urbi.ae/3JtpM",
            ),
            parseUri("https://go.urbi.ae/3JtpM"),
        )
    }

    @Test
    fun parseUri_api() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        41.285765, 69.234083,
                        z = 17.0,
                        name = "Music Store, магазин музыкальных инструментов",
                        source = Source.MAP_CENTER,
                    ),
                )
            ),
            parseUri("https://share.api.2gis.ru/getimage?city=tashkent&zoom=17&center=69.234083%2C41.285765&title=Music%20Store%2C%20%D0%BC%D0%B0%D0%B3%D0%B0%D0%B7%D0%B8%D0%BD%20%D0%BC%D1%83%D0%B7%D1%8B%D0%BA%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%D0%BD%D1%81%D1%82%D1%80%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%BE%D0%B2&desc=%D0%A3%D0%BB%D0%B8%D1%86%D0%B0%20%D0%9C%D1%83%D0%BA%D0%B8%D0%BC%D0%B8%2C%C2%A098%D0%B0%3Cbr%20%2F%3E%D0%A2%D0%B0%D1%88%D0%BA%D0%B5%D0%BD%D1%82"),
        )
    }

    @Test
    fun parseUri_apiWithoutTitle() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(41.285765, 69.234083, z = 17.0, source = Source.MAP_CENTER))),
            parseUri("https://share.api.2gis.ru/getimage?city=tashkent&zoom=17&center=69.234083%2C41.285765"),
        )
    }

    @Test
    fun parseHtml() = runTest {
        assertEquals(
            ParseHtmlResult(
                persistentListOf(
                    WGS84Point(
                        41.285765, 69.234083,
                        z = 17.0,
                        name = "Music Store, магазин музыкальных инструментов",
                        source = Source.MAP_CENTER,
                    ),
                )
            ),
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

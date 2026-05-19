package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class UrbiUriInputTest : InputTest {
    private val urbiHtmlInput = UrbiHtmlInput(urbiUriInput = { throw NotImplementedError() })
    private val input = UrbiUriInput(urbiHtmlInput = { urbiHtmlInput })

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://2gis.uz/tashkent/firm/70000001060803297",
            input.match("https://2gis.uz/tashkent/firm/70000001060803297")
        )
        assertEquals(
            "https://2gis.ae/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.ae/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.am/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.am/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.az/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.az/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.cl/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.cl/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.com.cy/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.com.cy/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.com/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.cz/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.cz/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.it/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.it/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.kg/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.kg/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.kz/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.kz/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.ru/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.ru/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://2gis.uz/dubai/geo/55.171971%2C25.289452",
            input.match("https://2gis.uz/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452",
            input.match("https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-bh.com/dubai/geo/55.171971%2C25.289452",
            input.match("https://urbi-bh.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-eg.com/dubai/geo/55.171971%2C25.289452",
            input.match("https://urbi-eg.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-kw.com/dubai/geo/55.171971%2C25.289452",
            input.match("https://urbi-kw.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-om.com/dubai/geo/55.171971%2C25.289452",
            input.match("https://urbi-om.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-qa.com/dubai/geo/55.171971%2C25.289452",
            input.match("https://urbi-qa.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi-sa.com/dubai/geo/55.171971%2C25.289452",
            input.match("https://urbi-sa.com/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi.bh/dubai/geo/55.171971%2C25.289452",
            input.match("https://urbi.bh/dubai/geo/55.171971%2C25.289452")
        )
        assertEquals(
            "https://urbi.qa/dubai/geo/55.171971%2C25.289452",
            input.match("https://urbi.qa/dubai/geo/55.171971%2C25.289452")
        )
    }

    @Test
    fun match_shortLink() {
        assertEquals("https://go.2gis.com/WSTdK", input.match("https://go.2gis.com/WSTdK"))
        assertEquals("https://go.urbi.ae/3JtpM", input.match("https://go.urbi.ae/3JtpM"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://example.com/tashkent/firm/70000001060803297"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "2gis.uz/tashkent/firm/70000001060803297",
            input.match("ftp://2gis.uz/tashkent/firm/70000001060803297"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://2gis.com/?q=foobar",
            input.match("https://2gis.com/?q=foobar ")
        )
        assertEquals(
            "https://2gis.com/?q=foo bar",
            input.match("https://2gis.com/?q=foo bar ")
        )
        assertEquals(
            "https://2gis.com/?q=foo",
            input.match("https://2gis.com/?q=foo  bar")
        )
        assertEquals(
            "https://2gis.com/?q=foo",
            input.match("https://2gis.com/?q=foo\tbar")
        )
    }

    @Test
    fun parse_point() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(25.284889, 55.172173, source = Source.URI))),
            input.parse("https://maps.urbi.ae/dubai/geo/55.172173%2C25.284889"),
        )
    }

    @Test
    fun parse_pointWithMarker() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(25.25915, 55.225263, z = 12.77, source = Source.URI))),
            input.parse("https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452?m=55.225263%2C25.25915%2F12.77"),
        )
    }

    @Test
    fun parse_geo() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    urbiHtmlInput,
                    "https://maps.urbi.ae/dubai/geo/13933621232533580"
                )
            ),
            input.parse("https://maps.urbi.ae/dubai/geo/13933621232533580"),
        )
    }

    @Test
    fun parse_firm() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    urbiHtmlInput,
                    "https://2gis.uz/tashkent/firm/70000001060803297"
                )
            ),
            input.parse("https://2gis.uz/tashkent/firm/70000001060803297"),
        )
    }

    @Test
    fun parse_firmWithCoordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(25.19925, 55.332211, source = Source.URI))),
            input.parse("https://maps.urbi.ae/dubai/firm/70000001043503020/55.332211%2C25.19925"),
        )
    }

    @Test
    fun parse_firmWithMarker() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(25.196707, 55.320748, z = 14.82, source = Source.URI))),
            input.parse("https://maps.urbi.ae/dubai/firm/70000001043503020/55.332211%2C25.19925?m=55.320748%2C25.196707%2F14.82"),
        )
    }

    @Test
    fun parse_shortLink() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    urbiHtmlInput,
                    "https://go.2gis.com/WSTdK"
                )
            ),
            input.parse("https://go.2gis.com/WSTdK"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    urbiHtmlInput,
                    "https://go.urbi.ae/3JtpM"
                )
            ),
            input.parse("https://go.urbi.ae/3JtpM"),
        )
    }

    @Test
    fun parse_api() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        41.285765, 69.234083,
                        z = 17.0,
                        name = "Music Store, магазин музыкальных инструментов",
                        source = Source.MAP_CENTER,
                    ),
                )
            ),
            input.parse("https://share.api.2gis.ru/getimage?city=tashkent&zoom=17&center=69.234083%2C41.285765&title=Music%20Store%2C%20%D0%BC%D0%B0%D0%B3%D0%B0%D0%B7%D0%B8%D0%BD%20%D0%BC%D1%83%D0%B7%D1%8B%D0%BA%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D1%85%20%D0%B8%D0%BD%D1%81%D1%82%D1%80%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%BE%D0%B2&desc=%D0%A3%D0%BB%D0%B8%D1%86%D0%B0%20%D0%9C%D1%83%D0%BA%D0%B8%D0%BC%D0%B8%2C%C2%A098%D0%B0%3Cbr%20%2F%3E%D0%A2%D0%B0%D1%88%D0%BA%D0%B5%D0%BD%D1%82"),
        )
    }

    @Test
    fun parse_apiWithoutTitle() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(41.285765, 69.234083, z = 17.0, source = Source.MAP_CENTER))),
            input.parse("https://share.api.2gis.ru/getimage?city=tashkent&zoom=17&center=69.234083%2C41.285765"),
        )
    }
}

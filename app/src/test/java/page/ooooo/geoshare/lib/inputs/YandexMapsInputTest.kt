package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class YandexMapsInputTest : InputTest {
    override val input = YandexMapsInput

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8",
            getUri("https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8")
        )
        @Suppress("SpellCheckingInspection")
        assertEquals(
            "yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8",
            getUri("yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8")
        )
        assertEquals(
            "https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/",
            getUri("https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/"),
        )
    }

    @Test
    fun uriPattern_shortUrl() {
        assertEquals("https://yandex.com/maps/-/CLAvMI18", getUri("https://yandex.com/maps/-/CLAvMI18"))
        assertEquals("yandex.com/maps/-/CLAvMI18", getUri("yandex.com/maps/-/CLAvMI18"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8",
            getUri("ftp://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"),
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
    fun shortUriPattern_correct() {
        assertEquals("https://yandex.com/maps/-/CLAvMI18", getShortUri("https://yandex.com/maps/-/CLAvMI18"))
    }

    @Test
    fun shortUriPattern_unknownPath() {
        assertNull(getShortUri("https://yandex.com/"))
        assertNull(getShortUri("https://yandex.com/maps/"))
        assertNull(getShortUri("https://yandex.com/maps/-/"))
        assertNull(getShortUri("https://yandex.com/foo"))
    }

    @Test
    fun shortUriPattern_unknownHost() {
        assertNull(getShortUri("https://www.example.com/foo"))
    }

    @Test
    fun parseUri_unknownPathOrParams() = runTest {
        assertEquals(ParseUriResult(), parseUri("https://yandex.com"))
        assertEquals(ParseUriResult(), parseUri("https://yandex.com/"))
        assertEquals(ParseUriResult(), parseUri("https://yandex.com/maps"))
        assertEquals(ParseUriResult(), parseUri("https://yandex.com/maps/?spam=1"))
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(WGS84Point(-37.81384550094835, 144.96315783657042, z = 17.852003, source = Source.URI))
            ),
            parseUri("https://yandex.com/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003"),
        )
    }

    @Test
    fun parseUri_coordinatesAndPoint() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        -37.81384550131279, 144.96315783657045,
                        z = 17.2,
                        source = Source.URI,
                    )
                )
            ),
            parseUri("https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.2&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"),
        )
    }

    @Test
    fun parseUri_poiWithCoordinatesAndPoint() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(52.294001, 8.065475, z = 13.24, source = Source.URI))),
            parseUri("https://yandex.com/maps/100513/osnabruck/?ll=8.055899%2C52.280743&mode=whatshere&whatshere%5Bpoint%5D=8.065475%2C52.294001&whatshere%5Bzoom%5D=13.24&z=15"),
        )
    }

    @Test
    fun parseUri_poiWithCoordinates() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.111192, 8.668963, z = 14.19, source = Source.URI))),
            parseUri("https://yandex.com/maps/org/94933420809/?display-text=Cafes&ll=8.668963%2C50.111192&mode=search&sctx=ZAAAAAgBEAAaKAoSCTHO34RCVCFAETJyFva0DUlAEhIJRii2gqYldj8R51JcVfZdYT8iBgABAgMEBSgKOABAZEgBYkZyZWFycj1zY2hlbWVfTG9jYWwvR2VvdXBwZXIvQWR2ZXJ0cy9SZWFycmFuZ2VCeUF1Y3Rpb24vQ2FjaGUvRW5hYmxlZD0xagJkZZ0BzczMPaABAKgBAL0ByteiIsIBkAGZj5fsswa4y%2FDcfpayrLSaAYmW5NPhAure6aC4Abn7yYWlA%2F3d2IRjiqSy14AG5PuvhaAE%2BoyK7rEC5Pu%2F75oF7L%2FyxdIDnOOpmucBt6iSh6UCyK%2FLuGyX48CmjwWFwNHQXv7d0vblBLXx6pSFA5y6x%2BXwBYy0i4Jx4oP6l8QG%2FevBrP0FnZn7uHOCpuWC9AaCAgjQmtCw0YTQtYoCNjE4NDEwNjM5MCQzNTE5MzExNDkzNyQxODQxMDYzOTQkMTg0MTA2MzkyJDIyMzA1MDc4MDc4NJICAJoCDGRlc2t0b3AtbWFwcw%3D%3D&sll=8.674635%2C50.129382&sspn=0.076143%2C0.041160&text=%D0%9A%D0%B0%D1%84%D0%B5&z=14.19"),
        )
    }

    @Test
    fun parseUri_poiWithoutCoordinates() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        name = @Suppress("SpellCheckingInspection") "keramicheskiy proyezd",
                        source = Source.URI,
                    )
                ),
                htmlUriString = "https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/",
            ),
            parseUri("https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://yandex.com/maps/org/94933420809",
            ),
            parseUri("https://yandex.com/maps/org/94933420809"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://yandex.com/maps/org/94933420809/",
            ),
            parseUri("https://yandex.com/maps/org/94933420809/"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://yandex.com/maps/org/94933420809?spam",
            ),
            parseUri("https://yandex.com/maps/org/94933420809?spam"),
        )
    }

    @Test
    fun parseUri_trDomain() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(WGS84Point(-37.81384550094835, 144.96315783657042, z = 17.852003, source = Source.URI))
            ),
            parseUri("https://yandex.com.tr/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003"),
        )
    }

    @Test
    fun parseHtml_containsCoordinates_returnsPoint() = runTest {
        assertEquals(
            ParseHtmlResult(
                persistentListOf(
                    WGS84Point(
                        55.882227, 37.566898,
                        name = @Suppress("SpellCheckingInspection") "Keramichesky Drive",
                        source = Source.HTML,
                    )
                )
            ),
            parseHtml(
                @Suppress("SpellCheckingInspection")
                """<meta property="og:image" content="https://static-maps.yandex.ru/1.x/?api_key=xxx&amp;theme=light&amp;lang=en_US&amp;size=520%2C440&amp;l=map&amp;spn=0.012927%2C0.024085&amp;ll=37.563875%2C55.881952&amp;lg=0&amp;cr=0&amp;pt=37.566898%2C55.882227%2Cplacemark&amp;signature=xxx">
                <h1 class="card-title-view__title" itemProp="name">Keramichesky Drive</h1>""".trimIndent()
            ),
        )
    }

    @Test
    fun parseHtml_doesNotContainCoordinates_returnsNull() = runTest {
        assertEquals(ParseHtmlResult(), parseHtml("""<html></html>"""))
    }
}

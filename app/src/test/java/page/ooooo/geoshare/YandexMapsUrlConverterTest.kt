package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.YandexMapsUrlConverter

class YandexMapsUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = YandexMapsUrlConverter()

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"))
        @Suppress("SpellCheckingInspection")
        assertTrue(doesUriPatternMatch("yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://yandex.com/maps/-/CLAvMI18"))
        assertTrue(doesUriPatternMatch("yandex.com/maps/-/CLAvMI18"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("https://yandex.com"))
        assertNull(parseUrl("https://yandex.com/"))
        assertNull(parseUrl("https://yandex.com/maps"))
        assertNull(parseUrl("https://yandex.com/maps/?spam=1"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("-37.81384550094835", "144.96315783657042", z = "17.852003"),
            parseUrl("https://yandex.com/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003")
        )
    }

    @Test
    fun parseUrl_coordinatesAndPoint() {
        assertEquals(
            Position("-37.81384550131279", "144.96315783657045", z = "17.2"),
            parseUrl("https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.2&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8")
        )
    }

    @Test
    fun parseUrl_placeAndCoordinatesAndPoint() {
        assertEquals(
            Position("52.294001", "8.065475", z = "13.24"),
            parseUrl("https://yandex.com/maps/100513/osnabruck/?ll=8.055899%2C52.280743&mode=whatshere&whatshere%5Bpoint%5D=8.065475%2C52.294001&whatshere%5Bzoom%5D=13.24&z=15")
        )
    }

    @Test
    fun parseUrl_orgAndCoordinates() {
        assertEquals(
            Position("50.111192", "8.668963", z = "14.19"),
            parseUrl("https://yandex.com/maps/org/94933420809/?display-text=Cafes&ll=8.668963%2C50.111192&mode=search&sctx=ZAAAAAgBEAAaKAoSCTHO34RCVCFAETJyFva0DUlAEhIJRii2gqYldj8R51JcVfZdYT8iBgABAgMEBSgKOABAZEgBYkZyZWFycj1zY2hlbWVfTG9jYWwvR2VvdXBwZXIvQWR2ZXJ0cy9SZWFycmFuZ2VCeUF1Y3Rpb24vQ2FjaGUvRW5hYmxlZD0xagJkZZ0BzczMPaABAKgBAL0ByteiIsIBkAGZj5fsswa4y%2FDcfpayrLSaAYmW5NPhAure6aC4Abn7yYWlA%2F3d2IRjiqSy14AG5PuvhaAE%2BoyK7rEC5Pu%2F75oF7L%2FyxdIDnOOpmucBt6iSh6UCyK%2FLuGyX48CmjwWFwNHQXv7d0vblBLXx6pSFA5y6x%2BXwBYy0i4Jx4oP6l8QG%2FevBrP0FnZn7uHOCpuWC9AaCAgjQmtCw0YTQtYoCNjE4NDEwNjM5MCQzNTE5MzExNDkzNyQxODQxMDYzOTQkMTg0MTA2MzkyJDIyMzA1MDc4MDc4NJICAJoCDGRlc2t0b3AtbWFwcw%3D%3D&sll=8.674635%2C50.129382&sspn=0.076143%2C0.041160&text=%D0%9A%D0%B0%D1%84%D0%B5&z=14.19")
        )
    }

    @Test
    fun parseUrl_orgOnly() {
        assertEquals(
            Position(),
            parseUrl("https://yandex.com/maps/org/94933420809/")
        )
    }

    @Test
    fun parseUrl_trDomain() {
        assertEquals(
            Position("-37.81384550094835", "144.96315783657042", z = "17.852003"),
            parseUrl("https://yandex.com.tr/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003")
        )
    }

    @Test
    fun parseHtml_containsDataCoordinates_returnsPosition() {
        assertEquals(
            Position("50.106376", "8.664164"),
            parseHtml("""<html><div data-coordinates="8.664164,50.106376"></div></html>""")
        )
    }

    @Test
    fun parseHtml_containsInvalidDataCoordinates_returnsNull() {
        assertNull(
            parseHtml("""<html><div data-coordinates="spam"></div></html>""")
        )
    }

    @Test
    fun parseHtml_doesNotContainCoordinates_returnsNull() {
        assertNull(
            parseHtml("""<html></html>""")
        )
    }

    @Test
    fun isShortUrl_correct() {
        assertTrue(isShortUrl("https://yandex.com/maps/-/CLAvMI18"))
    }

    @Test
    fun isShortUri_wrongPath() {
        assertFalse(isShortUrl("https://yandex.com/"))
        assertFalse(isShortUrl("https://yandex.com/maps/"))
        assertFalse(isShortUrl("https://yandex.com/maps/-/"))
        assertFalse(isShortUrl("https://yandex.com/foo"))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(isShortUrl("https://www.example.com/foo"))
    }
}

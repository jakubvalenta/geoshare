package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class YandexMapsInputTest : BaseInputTest() {
    override val input = YandexMapsInput

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
    fun parseUri_noPathOrKnownUrlQueryParams() {
        assertEquals(
            Position() to null,
            parseUri("https://yandex.com")
        )
        assertEquals(
            Position() to null,
            parseUri("https://yandex.com/")
        )
        assertEquals(
            Position() to null,
            parseUri("https://yandex.com/maps")
        )
        assertEquals(
            Position() to null,
            parseUri("https://yandex.com/maps/?spam=1")
        )
    }

    @Test
    fun parseUri_coordinates() {
        assertEquals(
            Position(Srs.WGS84, -37.81384550094835, 144.96315783657042, z = 17.852003) to null,
            parseUri("https://yandex.com/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003")
        )
    }

    @Test
    fun parseUri_coordinatesAndPoint() {
        assertEquals(
            Position(Srs.WGS84, -37.81384550131279, 144.96315783657045, z = 17.2) to null,
            parseUri("https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.2&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8")
        )
    }

    @Test
    fun parseUri_placeAndCoordinatesAndPoint() {
        assertEquals(
            Position(Srs.WGS84, 52.294001, 8.065475, z = 13.24) to null,
            parseUri("https://yandex.com/maps/100513/osnabruck/?ll=8.055899%2C52.280743&mode=whatshere&whatshere%5Bpoint%5D=8.065475%2C52.294001&whatshere%5Bzoom%5D=13.24&z=15")
        )
    }

    @Test
    fun parseUri_orgAndCoordinates() {
        assertEquals(
            Position(Srs.WGS84, 50.111192, 8.668963, z = 14.19) to null,
            parseUri("https://yandex.com/maps/org/94933420809/?display-text=Cafes&ll=8.668963%2C50.111192&mode=search&sctx=ZAAAAAgBEAAaKAoSCTHO34RCVCFAETJyFva0DUlAEhIJRii2gqYldj8R51JcVfZdYT8iBgABAgMEBSgKOABAZEgBYkZyZWFycj1zY2hlbWVfTG9jYWwvR2VvdXBwZXIvQWR2ZXJ0cy9SZWFycmFuZ2VCeUF1Y3Rpb24vQ2FjaGUvRW5hYmxlZD0xagJkZZ0BzczMPaABAKgBAL0ByteiIsIBkAGZj5fsswa4y%2FDcfpayrLSaAYmW5NPhAure6aC4Abn7yYWlA%2F3d2IRjiqSy14AG5PuvhaAE%2BoyK7rEC5Pu%2F75oF7L%2FyxdIDnOOpmucBt6iSh6UCyK%2FLuGyX48CmjwWFwNHQXv7d0vblBLXx6pSFA5y6x%2BXwBYy0i4Jx4oP6l8QG%2FevBrP0FnZn7uHOCpuWC9AaCAgjQmtCw0YTQtYoCNjE4NDEwNjM5MCQzNTE5MzExNDkzNyQxODQxMDYzOTQkMTg0MTA2MzkyJDIyMzA1MDc4MDc4NJICAJoCDGRlc2t0b3AtbWFwcw%3D%3D&sll=8.674635%2C50.129382&sspn=0.076143%2C0.041160&text=%D0%9A%D0%B0%D1%84%D0%B5&z=14.19")
        )
    }

    @Test
    fun parseUri_orgOnly() {
        assertEquals(
            Position() to "https://yandex.com/maps/org/94933420809",
            parseUri("https://yandex.com/maps/org/94933420809"),
        )
        assertEquals(
            Position() to "https://yandex.com/maps/org/94933420809/",
            parseUri("https://yandex.com/maps/org/94933420809/"),
        )
        assertEquals(
            Position() to "https://yandex.com/maps/org/94933420809?spam",
            parseUri("https://yandex.com/maps/org/94933420809?spam"),
        )
    }

    @Test
    fun parseUri_trDomain() {
        assertEquals(
            Position(Srs.WGS84, -37.81384550094835, 144.96315783657042, z = 17.852003) to null,
            parseUri("https://yandex.com.tr/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003")
        )
    }

    @Test
    fun parseHtml_containsCoordinates_returnsPosition() = runTest {
        assertEquals(
            Position(Srs.WGS84, 50.107130, 8.660903) to null,
            @Suppress("SpellCheckingInspection")
            parseHtml("""3ad6&amp;theme=light&amp;lang=en_US&amp;size=520%2C440&amp;l=map&amp;spn=0.009641%2C0.005481&amp;ll=8.660903%2C50.107130&amp;lg=0&amp;cr=0&amp;pt=8.664164%2C50.106376%2Cplacemark&amp;signature=cSM2mE5qjL5""")
        )
    }

    @Test
    fun parseHtml_doesNotContainCoordinates_returnsNull() = runTest {
        assertEquals(
            Position() to null,
            parseHtml("""<html></html>""")
        )
    }

    @Test
    fun isShortUri_correct() {
        assertTrue(isShortUri("https://yandex.com/maps/-/CLAvMI18"))
    }

    @Test
    fun isShortUri_wrongPath() {
        assertFalse(isShortUri("https://yandex.com/"))
        assertFalse(isShortUri("https://yandex.com/maps/"))
        assertFalse(isShortUri("https://yandex.com/maps/-/"))
        assertFalse(isShortUri("https://yandex.com/foo"))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(isShortUri("https://www.example.com/foo"))
    }
}

package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class YandexMapsUriInputTest : InputTest {
    private val input = YandexMapsUriInput(YandexMapsHtmlInput())

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8",
            input.match("https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8")
        )
        @Suppress("SpellCheckingInspection")
        assertEquals(
            "yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8",
            input.match("yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8")
        )
        assertEquals(
            "https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/",
            input.match("https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/"),
        )
    }

    @Test
    fun match_shortLink() {
        assertEquals("https://yandex.com/maps/-/CLAvMI18", input.match("https://yandex.com/maps/-/CLAvMI18"))
        assertEquals("yandex.com/maps/-/CLAvMI18", input.match("yandex.com/maps/-/CLAvMI18"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8",
            input.match("ftp://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.852003&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://yandex.com/maps?q=foobar",
            input.match("https://yandex.com/maps?q=foobar ")
        )
        assertEquals(
            "https://yandex.com/maps?q=foo bar",
            input.match("https://yandex.com/maps?q=foo bar ")
        )
        assertEquals(
            "https://yandex.com/maps?q=foo",
            input.match("https://yandex.com/maps?q=foo  bar")
        )
        assertEquals(
            "https://yandex.com/maps?q=foo",
            input.match("https://yandex.com/maps?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("https://yandex.com"))
        assertEquals(ParseResult(), input.parse("https://yandex.com/"))
        assertEquals(ParseResult(), input.parse("https://yandex.com/maps"))
        assertEquals(ParseResult(), input.parse("https://yandex.com/maps/?spam=1"))
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(WGS84Point(-37.81384550094835, 144.96315783657042, z = 17.852003, source = Source.URI))
            ),
            input.parse("https://yandex.com/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003"),
        )
    }

    @Test
    fun parse_coordinatesAndPoint() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        -37.81384550131279, 144.96315783657045,
                        z = 17.2,
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://yandex.com/maps?whatshere%5Bpoint%5D=144.96315783657045%2C-37.81384550131279&whatshere%5Bzoom%5D=17.2&ll=144.96315783657042%2C-37.81384550094835&z=17.852003&si=6u8menx2bg23cfx27y7p1je8y8"),
        )
    }

    @Test
    fun parse_directions() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(51.106893, 6.569370, source = Source.URI),
                    WGS84Point(51.228358, 6.502698, source = Source.URI),
                )
            ),
            input.parse("https://yandex.com/maps?rtext=51.106893%2C6.569370~51.228358%2C6.502698&rtt=auto"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(51.106893, 6.569370, z = 13.0, source = Source.URI),
                    WGS84Point(51.197102, 6.606140, z = 13.0, source = Source.URI),
                    WGS84Point(51.228358, 6.502698, z = 13.0, source = Source.URI),
                )
            ),
            input.parse("https://yandex.com/maps/?ll=6.549320%2C51.168727&mode=routes&rtext=51.106893%2C6.569370~51.197102%2C6.606140~51.228358%2C6.502698&rtt=auto&ruri=~~&z=13"),
        )
    }

    @Test
    fun parse_poiWithCoordinatesAndPoint() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.294001, 8.065475, z = 13.24, source = Source.URI))),
            input.parse("https://yandex.com/maps/100513/osnabruck/?ll=8.055899%2C52.280743&mode=whatshere&whatshere%5Bpoint%5D=8.065475%2C52.294001&whatshere%5Bzoom%5D=13.24&z=15"),
        )
    }

    @Test
    fun parse_poiWithCoordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.111192, 8.668963, z = 14.19, source = Source.URI))),
            input.parse("https://yandex.com/maps/org/94933420809/?display-text=Cafes&ll=8.668963%2C50.111192&mode=search&sctx=ZAAAAAgBEAAaKAoSCTHO34RCVCFAETJyFva0DUlAEhIJRii2gqYldj8R51JcVfZdYT8iBgABAgMEBSgKOABAZEgBYkZyZWFycj1zY2hlbWVfTG9jYWwvR2VvdXBwZXIvQWR2ZXJ0cy9SZWFycmFuZ2VCeUF1Y3Rpb24vQ2FjaGUvRW5hYmxlZD0xagJkZZ0BzczMPaABAKgBAL0ByteiIsIBkAGZj5fsswa4y%2FDcfpayrLSaAYmW5NPhAure6aC4Abn7yYWlA%2F3d2IRjiqSy14AG5PuvhaAE%2BoyK7rEC5Pu%2F75oF7L%2FyxdIDnOOpmucBt6iSh6UCyK%2FLuGyX48CmjwWFwNHQXv7d0vblBLXx6pSFA5y6x%2BXwBYy0i4Jx4oP6l8QG%2FevBrP0FnZn7uHOCpuWC9AaCAgjQmtCw0YTQtYoCNjE4NDEwNjM5MCQzNTE5MzExNDkzNyQxODQxMDYzOTQkMTg0MTA2MzkyJDIyMzA1MDc4MDc4NJICAJoCDGRlc2t0b3AtbWFwcw%3D%3D&sll=8.674635%2C50.129382&sspn=0.076143%2C0.041160&text=%D0%9A%D0%B0%D1%84%D0%B5&z=14.19"),
        )
    }

    @Test
    fun parse_poiWithoutCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        name = @Suppress("SpellCheckingInspection") "keramicheskiy proyezd",
                        source = Source.URI,
                    )
                ),
                nextStep = NextStep(
                    YandexMapsHtmlInput,
                    "https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/"
                )
            ),
            input.parse("https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    YandexMapsHtmlInput,
                    "https://yandex.com/maps/org/94933420809"
                )
            ),
            input.parse("https://yandex.com/maps/org/94933420809"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    YandexMapsHtmlInput,
                    "https://yandex.com/maps/org/94933420809/"
                )
            ),
            input.parse("https://yandex.com/maps/org/94933420809/"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    YandexMapsHtmlInput,
                    "https://yandex.com/maps/org/94933420809?spam"
                )
            ),
            input.parse("https://yandex.com/maps/org/94933420809?spam"),
        )
    }

    @Test
    fun parse_trDomain() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(WGS84Point(-37.81384550094835, 144.96315783657042, z = 17.852003, source = Source.URI))
            ),
            input.parse("https://yandex.com.tr/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003"),
        )
    }
}

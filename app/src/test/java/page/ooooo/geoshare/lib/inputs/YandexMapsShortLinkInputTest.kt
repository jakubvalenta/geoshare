package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YandexMapsShortLinkInputTest : InputTest {
    private val input = YandexMapsShortLinkInput

    @Test
    fun match_correct() {
        assertEquals("https://yandex.com/maps/-/CLAvMI18", input.match("https://yandex.com/maps/-/CLAvMI18"))
    }

    @Test
    fun match_unknownPath() {
        assertNull(input.match("https://yandex.com/"))
        assertNull(input.match("https://yandex.com/maps/"))
        assertNull(input.match("https://yandex.com/maps/-/"))
        assertNull(input.match("https://yandex.com/foo"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/foo"))
    }

    @Test
    fun parse_returnsNextStep() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    YandexMapsUriInput,
                    "https://yandex.com/maps/org/94933420809/?display-text=Cafes&ll=8.668963,50.111192"
                )
            ),
            input.parse("https://yandex.com/maps/org/94933420809/?display-text=Cafes&ll=8.668963%2C50.111192"),
        )
    }
}

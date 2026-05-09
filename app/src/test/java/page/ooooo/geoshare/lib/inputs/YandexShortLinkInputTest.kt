package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YandexShortLinkInputTest : InputTest {
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
}

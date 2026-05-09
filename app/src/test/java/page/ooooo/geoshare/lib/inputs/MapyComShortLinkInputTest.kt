package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapyComShortLinkInputTest : InputTest {
    private val input = MapyComShortLinkInput

    @Test
    fun match_correct() {
        assertEquals("https://mapy.com/s/jakuhelasu", input.match("https://mapy.com/s/jakuhelasu"))
        assertEquals("https://www.mapy.com/s/jakuhelasu", input.match("https://www.mapy.com/s/jakuhelasu"))
        assertEquals("https://mapy.cz/s/jakuhelasu", input.match("https://mapy.cz/s/jakuhelasu"))
    }

    @Test
    fun match_unknownPath() {
        assertNull(input.match("https://mapy.com/"))
        assertNull(input.match("https://mapy.com/s"))
        assertNull(input.match("https://mapy.com/s/"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/foo"))
    }
}

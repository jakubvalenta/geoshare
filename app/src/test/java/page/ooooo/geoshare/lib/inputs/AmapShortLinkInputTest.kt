package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmapShortLinkInputTest {
    private val input = AmapShortLinkInput

    @Test
    fun match() {
        assertEquals("https://surl.amap.com/4mkKGuyJ2bz", input.match("https://surl.amap.com/4mkKGuyJ2bz"))
    }

    @Test
    fun match_correct() {
        assertEquals("https://surl.amap.com/4mkKGuyJ2bz", input.match("https://surl.amap.com/4mkKGuyJ2bz"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/4mkKGuyJ2bz"))
    }
}

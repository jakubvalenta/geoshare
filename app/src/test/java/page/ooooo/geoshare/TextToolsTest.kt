package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.truncateMiddle

class TextToolsTest {
    @Test
    fun truncateMiddle_stringIsShorterThanMaxLength_returnsStringUnchanged() {
        assertEquals("foo bar", truncateMiddle("foo bar", 9, "..."))
    }

    @Test
    fun truncateMiddle_stringIsLongerThanMaxLength_returnsStringWithEllipsisInTheMiddle() {
        assertEquals("foo ... baz", truncateMiddle("foo bar baz", 9, "..."))
    }
}

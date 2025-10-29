package page.ooooo.geoshare.lib

import org.junit.Assert.assertEquals
import org.junit.Test

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

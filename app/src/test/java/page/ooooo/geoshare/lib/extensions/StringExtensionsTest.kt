package page.ooooo.geoshare.lib.extensions

import org.junit.Assert
import org.junit.Test

class StringExtensionsTest {
    @Test
    fun truncateMiddle_stringIsShorterThanMaxLength_returnsStringUnchanged() {
        Assert.assertEquals("foo bar", "foo bar".truncateMiddle(9, "..."))
    }

    @Test
    fun truncateMiddle_stringIsLongerThanMaxLength_returnsStringWithEllipsisInTheMiddle() {
        Assert.assertEquals("foo ... baz", "foo bar baz".truncateMiddle(9, "..."))
    }
}

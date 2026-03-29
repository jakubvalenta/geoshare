package page.ooooo.geoshare.lib.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StringExtensionsTest {
    @Test
    fun truncateMiddle_stringIsShorterThanMaxLength_returnsStringUnchanged() {
        assertEquals("foo bar", "foo bar".truncateMiddle(9, "..."))
    }

    @Test
    fun truncateMiddle_stringIsLongerThanMaxLength_returnsStringWithEllipsisInTheMiddle() {
        assertEquals("foo ... baz", "foo bar baz".truncateMiddle(9, "..."))
    }

    @Test
    fun firstGraphemeOrNull_whenStringIsEmpty_returnsNull() {
        assertNull("".firstGraphemeOrNull())
    }

    @Test
    fun firstGraphemeOrNull_whenStringContainsAsciiCharacters_returnsFirstGraphemeCluster() {
        assertEquals("a", "abc".firstGraphemeOrNull())
    }

    @Test
    fun firstGraphemeOrNull_whenStringContainsEmojiWithSkinTone_returnsFirstGraphemeCluster() {
        assertEquals("\uD83D\uDC4D\uD83C\uDFFD", "👍🏽".firstGraphemeOrNull())
    }

    @Test
    fun firstGraphemeOrNull_whenStringContainsEmojiFlag_returnsFirstGraphemeCluster() {
        assertEquals("\uD83C\uDDEC\uD83C\uDDF7", "🇬🇷".firstGraphemeOrNull())
    }

    @Test
    fun firstGraphemeOrNull_whenStringContainsCombiningAccent_returnsFirstGraphemeCluster() {
        assertEquals("e\u0301", "e\u0301".firstGraphemeOrNull())
    }

    @Test
    fun firstGraphemeOrNull_whenStringContainsInvalidUnicodeCharacterSequence_returnsFirstGraphemeCluster() {
        assertEquals("\uD83C", "\uD83C".firstGraphemeOrNull())
    }
}

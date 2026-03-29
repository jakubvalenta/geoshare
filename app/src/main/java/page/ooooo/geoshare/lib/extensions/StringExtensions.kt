package page.ooooo.geoshare.lib.extensions

import java.text.BreakIterator

fun String.decodeBasicHtmlEntities(): String =
    this.replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")

/**
 * Returns the first grapheme cluster of this string as a [String].
 *
 * Unlike [firstOrNull], which returns a single [Char] and can split surrogate pairs, this function respects Unicode
 * grapheme cluster boundaries (UAX #29). It correctly handles surrogate pairs, combining characters, emoji modifier
 * sequences, and Zero Width Joiner (ZWJ) sequences.
 */
fun String.firstGraphemeOrNull(): String? {
    if (isEmpty()) {
        return null
    }
    val it = BreakIterator.getCharacterInstance()
    it.setText(this)
    val end = it.next()
    return substring(0, end)
}

fun String.truncateMiddle(maxLength: Int = 60, ellipsis: String = "\u2026"): String =
    if (this.length > maxLength) {
        val partLength: Int = maxLength / 2
        "${this.take(partLength)}${ellipsis}${this.substring(this.length - partLength)}"
    } else {
        this
    }

package page.ooooo.geoshare.lib.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class RegexExtensionsTest {
    @Test
    fun findAll() {
        assertEquals(
            listOf("a", "b", "c"),
            ("." findAll "abc").map { it.group() }.toList(),
        )
    }
}

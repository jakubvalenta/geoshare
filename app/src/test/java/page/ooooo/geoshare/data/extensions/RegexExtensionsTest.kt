package page.ooooo.geoshare.data.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class RegexExtensionsTest {
    @Test
    fun findAll() {
        assertEquals(
            listOf("a", "b", "c"),
            Regex(".").findAll("abc").map { it.value }.toList(),
        )
    }
}

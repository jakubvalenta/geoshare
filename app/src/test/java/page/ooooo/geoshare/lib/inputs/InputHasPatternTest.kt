package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InputHasPatternTest {
    @Test
    fun match_returnsFirstRegexGroup() {
        val input = object : BasicOfflineInput, Input.HasPattern {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override val pattern = Regex("""(foo)""")

            override fun parse(match: String, resources: Resources): ParseResult {
                throw NotImplementedError()
            }
        }
        assertEquals("foo", input.match("spam foo spam"))
        assertNull(input.match("spam"))
    }
}

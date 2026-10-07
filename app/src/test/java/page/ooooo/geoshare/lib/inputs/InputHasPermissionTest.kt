package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.network.FetchTools

class InputHasPermissionTest {
    @Test
    fun match_returnsFirstRegexGroup() {
        val input = object : BasicInput, Input.HasPattern {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override val pattern = Regex("""(foo)""")

            override suspend fun parse(
                match: String,
                resources: Resources,
                fetchTools: FetchTools,
            ): ParseResult {
                throw NotImplementedError()
            }
        }
        assertEquals("foo", input.match("spam foo spam"))
        assertNull(input.match("spam"))
    }
}

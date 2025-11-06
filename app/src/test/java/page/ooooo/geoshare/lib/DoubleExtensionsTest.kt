package page.ooooo.geoshare.lib

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class DoubleExtensionsTest {
    @Test
    fun toTrimmedString_whenNumberHasNoDecimalPlaces_returnsStringWithNoDecimalPlaces() {
        assertEquals("0", 0.0.toTrimmedString())
        assertEquals("3", 3.0.toTrimmedString())
        assertEquals("-3", (-3.0).toTrimmedString())
    }

    @Test
    fun toTrimmedString_whenNumberHasManyDecimalPlaces_returnsStringWithAllDecimalPlaces() {
        assertEquals("3.141592653589793", PI.toTrimmedString())
        assertEquals("-3.141592653589793", (-1 * PI).toTrimmedString())
    }

    @Test
    fun toTrimmedString_whenNumberCanBeWrittenUsingScientificNotation_returnsStringWithAllDecimalPlaces() {
        assertEquals("0.0000001", 1.0E-7.toTrimmedString())
    }
}

package page.ooooo.geoshare.lib.extensions

import org.junit.Assert
import org.junit.Test
import kotlin.math.PI

class DoubleExtensionsTest {
    @Test
    fun toTrimmedString_whenNumberHasNoDecimalPlaces_returnsStringWithNoDecimalPlaces() {
        Assert.assertEquals("0", 0.0.toTrimmedString())
        Assert.assertEquals("3", 3.0.toTrimmedString())
        Assert.assertEquals("-3", (-3.0).toTrimmedString())
    }

    @Test
    fun toTrimmedString_whenNumberHasManyDecimalPlaces_returnsStringWithAllDecimalPlaces() {
        Assert.assertEquals("3.141592653589793", PI.toTrimmedString())
        Assert.assertEquals("-3.141592653589793", (-1 * PI).toTrimmedString())
    }

    @Test
    fun toTrimmedString_whenNumberCanBeWrittenUsingScientificNotation_returnsStringWithAllDecimalPlaces() {
        Assert.assertEquals("0.0000001", 1.0E-7.toTrimmedString())
    }
}

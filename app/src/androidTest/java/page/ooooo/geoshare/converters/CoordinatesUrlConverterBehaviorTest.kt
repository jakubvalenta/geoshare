package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.Position

class CoordinatesUrlConverterBehaviorTest : BaseActivityBehaviorTest() {
    @Test
    fun test() {
        // Decimal
        testTextUri(
            Position("-68.648556", "-152.775879"),
            "N-68.648556,E-152.775879",
        )
    }
}

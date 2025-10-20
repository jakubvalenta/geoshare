package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class CoordinatesUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntroIfItIsVisible()

        // Decimal
        testTextUri(
            Position("-68.648556", "-152.775879"),
            "N-68.648556,E-152.775879",
        )
    }
}

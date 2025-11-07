package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Srs

class CoordinatesUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Decimal
        testTextUri(
            Position(Srs.WGS84, -68.648556, -152.775879),
            "N-68.648556,E-152.775879",
        )
    }
}

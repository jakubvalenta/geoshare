package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class CoordinatesInputBehaviorTest : BaseInputBehaviorTest() {
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

package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class UrbiInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Short URI
        testUri(
            Position(Srs.WGS84, 41.285765, 69.234083, z = 17.0),
            "https://go.2gis.com/WSTdK",
        )
    }
}

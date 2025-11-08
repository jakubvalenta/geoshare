package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class AmapInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Short URI
        testUri(
            Position(Srs.GCJ02, 31.222811749011463, 121.46840706467624),
            "https://surl.amap.com/4mkKGuyJ2bz",
        )
    }
}

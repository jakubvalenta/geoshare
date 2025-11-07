package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Srs

class AmapUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
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

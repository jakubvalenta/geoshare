package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

@Suppress("SpellCheckingInspection")
class AmapUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Short URI
        testUri(
            Position("31.224731304675522", "121.46385323166844", desc = "WGS 84"),
            "https://surl.amap.com/4mkKGuyJ2bz",
        )
    }
}

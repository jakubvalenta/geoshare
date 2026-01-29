package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class UrbiInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Point with marker
        testUri(
            WGS84Point(25.25915, 55.225263, z = 12.77),
            "https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452?m=55.225263%2C25.25915%2F12.77"
        )

        // Short URI
        testUri(
            WGS84Point(41.285765, 69.234083, z = 17.0),
            "https://go.2gis.com/WSTdK",
        )
    }
}

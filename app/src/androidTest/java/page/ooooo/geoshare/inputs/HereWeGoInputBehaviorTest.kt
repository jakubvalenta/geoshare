package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point

class HereWeGoInputBehaviorTest : InputBehaviorTest {
    @Test
    fun hereWeGo() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(50.21972, -0.68453, z = 6.93, source = Source.MAP_CENTER),
            "https://wego.here.com/?map=50.21972,-0.68453,6.93",
        )

        // Coordinates and query
        testUri(
            WGS84Point(-38.14749, 145.14347, source = Source.HASH),
            "https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=",
        )
    }
}

package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.Position

class HereWeGoUrlConverterBehaviorTest : BaseActivityBehaviorTest() {
    @Test
    fun test() {
        // Coordinates
        testUri(
            Position("50.21972", "-0.68453", z = "6.93"),
            "https://wego.here.com/?map=50.21972,-0.68453,6.93",
        )

        // Coordinates and query
        testUri(
            Position("-38.14749", "145.14347"),
            "https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=",
        )
    }
}

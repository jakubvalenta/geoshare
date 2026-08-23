package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.closeIntro
import page.ooooo.geoshare.tests.configureConnectionPermissionPreference
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class GoogleMapsShortLinkInputBehaviorTest : InputBehaviorTest {
    @Test
    fun googleMapsShortLinkInput_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)

        // Short link within western Japan
        testUri(
            WGS84Point(34.5945482, 133.7583428, z = 17.0, name = "Steak no Don", source = Source.URI),
            "https://maps.app.goo.gl/mBtbC6qXLK2baGTV9",
        )

        // Short link within mainland China
        testUri(
            GCJ02Point(39.920439, 116.331538, source = Source.URI),
            "https://maps.app.goo.gl/FP3EV7tTUKYbmcVp7",
        )
    }
}

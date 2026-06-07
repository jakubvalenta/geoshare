package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.assumeDomainResolvable
import page.ooooo.geoshare.closeIntro
import page.ooooo.geoshare.configureConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.testUri
import page.ooooo.geoshare.waitForAppToBeVisible

class UrbiInputBehaviorTest {
    @Test
    fun urbi() = uiAutomator {
        // Point with marker
        testUri(
            WGS84Point(25.25915, 55.225263, z = 12.77, source = Source.URI),
            "https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452?m=55.225263%2C25.25915%2F12.77"
        )
    }

    @Test
    fun urbiHtml() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("go.2gis.com")
        }

        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)

        // Short link
        testUri(
            WGS84Point(
                41.285765, 69.234083,
                z = 17.0,
                name = "Music Store, магазин музыкальных инструментов",
                source = Source.MAP_CENTER,
            ),
            "https://go.2gis.com/WSTdK",
        )
    }
}

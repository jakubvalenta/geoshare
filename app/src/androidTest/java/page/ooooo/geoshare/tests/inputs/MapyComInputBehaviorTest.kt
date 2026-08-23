package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.closeIntro
import page.ooooo.geoshare.tests.configureConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class MapyComInputBehaviorTest : InputBehaviorTest {
    @Test
    fun mapyCom_offline() = uiAutomator {
        // Coordinates with international domain
        testUri(
            WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.MAP_CENTER),
            "https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
        )

        // Coordinates with local domain
        testUri(
            WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.MAP_CENTER),
            "https://mapy.cz?x=14.0184810&y=50.0525078&z=9",
        )
    }

    @Test
    fun mapyCom_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("mapy.com")
        }

        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)

        // Short link
        testUri(
            WGS84Point(50.0831498, 14.4549515, z = 17.0, source = Source.MAP_CENTER),
            "https://mapy.com/s/jakuhelasu",
        )

        // Navigation
        testUri(
            persistentListOf(
                WGS84Point(44.669848904013634, -63.578297942876816, z = 19.0, source = Source.HASH),
                WGS84Point(44.645654037594795, -63.60516831278801, z = 19.0, source = Source.HASH),
                WGS84Point(44.658605083823204, -63.61712023615837, z = 19.0, source = Source.HASH),
            ),
            "https://mapy.com/s/dufokujobu"
        )
    }
}

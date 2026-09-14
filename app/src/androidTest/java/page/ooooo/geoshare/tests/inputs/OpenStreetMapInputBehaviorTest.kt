package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.configureConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class OpenStreetMapInputBehaviorTest : InputBehaviorTest {
    @Test
    fun openStreetMap_offline() = uiAutomator {
        // Map center
        testUri(
            WGS84Point(51.49, -0.13, z = 16.0, source = Source.MAP_CENTER),
            "https://www.openstreetmap.org/#map=16/51.49/-0.13",
        )

        // Coordinates
        testUri(
            WGS84Point(51.49, -0.13, z = 16.0, source = Source.URI),
            "https://www.openstreetmap.org/?lat=51.49&lon=-0.13&zoom=16",
        )

        // Directions
        testUri(
            WGS84Point(51.0528, 13.7364, source = Source.URI),
            "https://www.openstreetmap.org/directions?to=51.0528,13.7364",
        )

        // Short link
        testUri(
            WGS84Point(-16.23152732849121, -49.08348083496094, z = 11.0, source = Source.HASH),
            "https://osm.org/go/NuJWxJh-",
        )
    }

    @Test
    fun openStreetMap_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("www.openstreetmap.org")
        }

        // Launch app
        launchApplication()
        waitForAppToBeVisible()
        configureConnectionPermissionPreference(Permission.ALWAYS)

        // Node
        testUri(
            WGS84Point(45.4771659, 9.2297918, source = Source.API),
            "https://www.openstreetmap.org/node/6284640534",
        )

        // Relation
        testUri(
            persistentListOf(
                WGS84Point(45.4772309, 9.2295862, source = Source.API),
                WGS84Point(45.4772270, 9.2292124, source = Source.API),
                WGS84Point(45.4776002, 9.2293737, source = Source.API),
                WGS84Point(45.4774959, 9.2295256, source = Source.API),
                WGS84Point(45.4774770, 9.2295534, source = Source.API),
                WGS84Point(45.4773774, 9.2295549, source = Source.API),
                WGS84Point(45.4774959, 9.2293841, source = Source.API),
                WGS84Point(45.4773606, 9.2295265, source = Source.API),
                WGS84Point(45.4774817, 9.2293589, source = Source.API),
                WGS84Point(45.4773591, 9.2293890, source = Source.API),
                WGS84Point(45.4773774, 9.2293573, source = Source.API),
                WGS84Point(45.4775994, 9.2290779, source = Source.API),
                WGS84Point(45.4776882, 9.2292067, source = Source.API),
                WGS84Point(45.4775657, 9.2290790, source = Source.API),
                WGS84Point(45.4776890, 9.2292514, source = Source.API),
                WGS84Point(45.4773416, 9.2296803, source = Source.API),
                WGS84Point(45.4773421, 9.2297926, source = Source.API),
                WGS84Point(45.4775818, 9.2290785, source = Source.API),
                WGS84Point(45.4773698, 9.2295407, source = Source.API),
                WGS84Point(45.4774878, 9.2293679, source = Source.API),
                WGS84Point(45.4770853, 9.2292154, source = Source.API),
                WGS84Point(45.4770890, 9.2295891, source = Source.API),
                WGS84Point(45.4772357, 9.2296812, source = Source.API),
                WGS84Point(45.4772362, 9.2297936, source = Source.API),
                WGS84Point(45.4774857, 9.2292097, source = Source.API),
                WGS84Point(45.4775156, 9.2297867, source = Source.API),
                WGS84Point(45.4774324, 9.2297899, source = Source.API),
                WGS84Point(45.4774308, 9.2297068, source = Source.API),
                WGS84Point(45.4775140, 9.2297036, source = Source.API),
            ),
            "https://www.openstreetmap.org/relation/910699",
        )

        // Way
        testUri(
            persistentListOf(
                WGS84Point(45.4770640, 9.2296749, source = Source.API),
                WGS84Point(45.4771158, 9.2296737, source = Source.API),
                WGS84Point(45.4771159, 9.2296361, source = Source.API),
                WGS84Point(45.4772950, 9.2296354, source = Source.API),
            ),
            "https://www.openstreetmap.org/way/596674456",
        )
    }
}

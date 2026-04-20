package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class OpenStreetMapInputBehaviorTest : InputBehaviorTest {
    @Test
    fun openStreetMap() = uiAutomator {
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

        // Short URI
        testUri(
            WGS84Point(-16.23152732849121, -49.08348083496094, z = 11.0, source = Source.HASH),
            "https://osm.org/go/NuJWxJh-",
        )
    }

    @Test
    fun openStreetMapHtml() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("www.openstreetmap.org")
        }

        // Launch app and close intro
        launchApplication()
        closeIntro()
        setUserPreferenceConnectionPermissionToAlways()

        // Node
        testUri(
            WGS84Point(45.4771659, 9.2297918, source = Source.API),
            "https://www.openstreetmap.org/node/6284640534",
        )

        // Relation
        testUri(
            persistentListOf(
                WGS84Point(45.4776025, 9.2297852, source = Source.API),
                WGS84Point(45.4773399, 9.2296095, source = Source.API),
                WGS84Point(45.4770943, 9.2295887, source = Source.API),
                WGS84Point(45.4770881, 9.2292100, source = Source.API),
                WGS84Point(45.4772588, 9.2292121, source = Source.API),
                WGS84Point(45.4776002, 9.2295189, source = Source.API),
                WGS84Point(45.4776002, 9.2293737, source = Source.API),
                WGS84Point(45.4773805, 9.2292100, source = Source.API),
                WGS84Point(45.4774762, 9.2292100, source = Source.API),
                WGS84Point(45.4774959, 9.2295256, source = Source.API),
                WGS84Point(45.4774770, 9.2295534, source = Source.API),
                WGS84Point(45.4776323, 9.2295472, source = Source.API),
                WGS84Point(45.4773177, 9.2295833, source = Source.API),
                WGS84Point(45.4776132, 9.2296902, source = Source.API),
                WGS84Point(45.4773774, 9.2295549, source = Source.API),
                WGS84Point(45.4774959, 9.2293841, source = Source.API),
                WGS84Point(45.4776040, 9.2296902, source = Source.API),
                WGS84Point(45.4773606, 9.2295265, source = Source.API),
                WGS84Point(45.4772779, 9.2291696, source = Source.API),
                WGS84Point(45.4774817, 9.2293589, source = Source.API),
                WGS84Point(45.4773591, 9.2293890, source = Source.API),
                WGS84Point(45.4773621, 9.2291674, source = Source.API),
                WGS84Point(45.4773774, 9.2293573, source = Source.API),
                WGS84Point(45.4775994, 9.2290779, source = Source.API),
                WGS84Point(45.4776882, 9.2292067, source = Source.API),
                WGS84Point(45.4775657, 9.2290790, source = Source.API),
                WGS84Point(45.4776890, 9.2292514, source = Source.API),
                WGS84Point(45.4772297, 9.2296848, source = Source.API),
                WGS84Point(45.4773415, 9.2296804, source = Source.API),
                WGS84Point(45.4772313, 9.2297776, source = Source.API),
                WGS84Point(45.4775818, 9.2290785, source = Source.API),
                WGS84Point(45.4776002, 9.2294250, source = Source.API),
                WGS84Point(45.4773300, 9.2295985, source = Source.API),
                WGS84Point(45.4773698, 9.2295407, source = Source.API),
                WGS84Point(45.4774878, 9.2293679, source = Source.API),
                WGS84Point(45.4772596, 9.2291990, source = Source.API),
                WGS84Point(45.4773797, 9.2291925, source = Source.API),
                WGS84Point(45.4776124, 9.2295200, source = Source.API),
                WGS84Point(45.4776315, 9.2296597, source = Source.API),
                WGS84Point(45.4774366, 9.2292100, source = Source.API),
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

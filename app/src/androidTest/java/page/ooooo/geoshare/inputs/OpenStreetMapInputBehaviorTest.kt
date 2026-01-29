package page.ooooo.geoshare.inputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import page.ooooo.geoshare.lib.point.Position
import page.ooooo.geoshare.lib.point.Srs

class OpenStreetMapInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates
        testUri(
            WGS84Point(51.49, -0.13, z = 16.0),
            "https://www.openstreetmap.org/#map=16/51.49/-0.13",
        )

        // Directions
        testUri(
            Position(
                points = persistentListOf(
                    WGS84Point(51.0528, 13.7364),
                ),
            ),
            "https://www.openstreetmap.org/directions?to=51.0528,13.7364",
        )

        // Node
        testUri(
            Position(
                points = persistentListOf(
                    WGS84Point(45.4771659, 9.2297918),
                ),
            ),
            "https://www.openstreetmap.org/node/6284640534",
        )

        // Relation
        testUri(
            Position(
                points = persistentListOf(
                    WGS84Point(45.4776025, 9.2297852),
                    WGS84Point(45.4773399, 9.2296095),
                    WGS84Point(45.4770943, 9.2295887),
                    WGS84Point(45.4770881, 9.2292100),
                    WGS84Point(45.4772588, 9.2292121),
                    WGS84Point(45.4776002, 9.2295189),
                    WGS84Point(45.4776002, 9.2293737),
                    WGS84Point(45.4773805, 9.2292100),
                    WGS84Point(45.4774762, 9.2292100),
                    WGS84Point(45.4774959, 9.2295256),
                    WGS84Point(45.4774770, 9.2295534),
                    WGS84Point(45.4776323, 9.2295472),
                    WGS84Point(45.4773177, 9.2295833),
                    WGS84Point(45.4776132, 9.2296902),
                    WGS84Point(45.4773774, 9.2295549),
                    WGS84Point(45.4774959, 9.2293841),
                    WGS84Point(45.4776040, 9.2296902),
                    WGS84Point(45.4773606, 9.2295265),
                    WGS84Point(45.4772779, 9.2291696),
                    WGS84Point(45.4774817, 9.2293589),
                    WGS84Point(45.4773591, 9.2293890),
                    WGS84Point(45.4773621, 9.2291674),
                    WGS84Point(45.4773774, 9.2293573),
                    WGS84Point(45.4775994, 9.2290779),
                    WGS84Point(45.4776882, 9.2292067),
                    WGS84Point(45.4775657, 9.2290790),
                    WGS84Point(45.4776890, 9.2292514),
                    WGS84Point(45.4772297, 9.2296848),
                    WGS84Point(45.4773415, 9.2296804),
                    WGS84Point(45.4772313, 9.2297776),
                    WGS84Point(45.4775818, 9.2290785),
                    WGS84Point(45.4776002, 9.2294250),
                    WGS84Point(45.4773300, 9.2295985),
                    WGS84Point(45.4773698, 9.2295407),
                    WGS84Point(45.4774878, 9.2293679),
                    WGS84Point(45.4772596, 9.2291990),
                    WGS84Point(45.4773797, 9.2291925),
                    WGS84Point(45.4776124, 9.2295200),
                    WGS84Point(45.4776315, 9.2296597),
                    WGS84Point(45.4774366, 9.2292100),
                ),
            ),
            "https://www.openstreetmap.org/relation/910699",
        )

        // Way
        testUri(
            Position(
                points = persistentListOf(
                    WGS84Point(45.4770640, 9.2296749),
                    WGS84Point(45.4771158, 9.2296737),
                    WGS84Point(45.4771159, 9.2296361),
                    WGS84Point(45.4772950, 9.2296354),
                ),
            ),
            "https://www.openstreetmap.org/way/596674456",
        )

        // Short URI
        testUri(
            WGS84Point(-16.23152732849121, -49.08348083496094, z = 11.0),
            "https://osm.org/go/NuJWxJh-",
        )
    }
}

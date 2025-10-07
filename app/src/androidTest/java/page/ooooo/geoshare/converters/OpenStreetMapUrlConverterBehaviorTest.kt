package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class OpenStreetMapUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Launch app and set connection permission to Always
        launchApplication()
        clickIntroCloseButton()
        setUserPreferenceConnectionPermissionToAlways()

        // Coordinates
        testUri(
            Position("51.49", "-0.13", z = "16"),
            "https://www.openstreetmap.org/#map=16/51.49/-0.13",
        )

        // Node
        testUri(
            Position(
                points = listOf(
                    "45.4771659" to "9.2297918",
                ),
            ),
            "https://www.openstreetmap.org/node/6284640534",
        )

        // Relation
        testUri(
            Position(
                points = listOf(
                    "45.4776025" to "9.2297852",
                    "45.4773399" to "9.2296095",
                    "45.4770943" to "9.2295887",
                    "45.4770881" to "9.2292100",
                    "45.4772588" to "9.2292121",
                    "45.4776002" to "9.2295189",
                    "45.4776002" to "9.2293737",
                    "45.4773805" to "9.2292100",
                    "45.4774762" to "9.2292100",
                    "45.4774959" to "9.2295256",
                    "45.4774770" to "9.2295534",
                    "45.4776323" to "9.2295472",
                    "45.4773177" to "9.2295833",
                    "45.4776132" to "9.2296902",
                    "45.4773774" to "9.2295549",
                    "45.4774959" to "9.2293841",
                    "45.4776040" to "9.2296902",
                    "45.4773606" to "9.2295265",
                    "45.4772779" to "9.2291696",
                    "45.4774817" to "9.2293589",
                    "45.4773591" to "9.2293890",
                    "45.4773621" to "9.2291674",
                    "45.4773774" to "9.2293573",
                    "45.4775994" to "9.2290779",
                    "45.4776882" to "9.2292067",
                    "45.4775657" to "9.2290790",
                    "45.4776890" to "9.2292514",
                    "45.4772297" to "9.2296848",
                    "45.4773415" to "9.2296804",
                    "45.4772313" to "9.2297776",
                    "45.4775818" to "9.2290785",
                    "45.4776002" to "9.2294250",
                    "45.4773300" to "9.2295985",
                    "45.4773698" to "9.2295407",
                    "45.4774878" to "9.2293679",
                    "45.4772596" to "9.2291990",
                    "45.4773797" to "9.2291925",
                    "45.4776124" to "9.2295200",
                    "45.4776315" to "9.2296597",
                    "45.4774366" to "9.2292100",
                ),
            ),
            "https://www.openstreetmap.org/relation/910699",
        )

        // Way
        testUri(
            Position(
                points = listOf(
                    "45.4770640" to "9.2296749",
                    "45.4771158" to "9.2296737",
                    "45.4771159" to "9.2296361",
                    "45.4772950" to "9.2296354",
                ),
            ),
            "https://www.openstreetmap.org/way/596674456",
        )

        // Short URI
        testUri(
            Position("-16.23152732849121", "-49.08348083496094"),
            "https://osm.org/go/NuJWxJh-",
        )
    }
}

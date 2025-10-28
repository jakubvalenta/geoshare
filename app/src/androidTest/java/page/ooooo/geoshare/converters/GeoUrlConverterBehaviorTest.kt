package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class GeoUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Coordinates, query and zoom
        testUri(
            Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"),
            "geo:50.123456,-11.123456?q=foo%20bar&z=3.4",
        )

        // Text
        testTextUri(
            Position("40.7127400", "-74.0059965", z = "9"),
            "Organic Maps: geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                    "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )
    }
}

package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.Position

@Suppress("SpellCheckingInspection")
class MapyComUrlConverterBehaviorTest : BaseActivityBehaviorTest() {
    @Test
    fun test() {
        // Launch app and set connection permission to Always
        launchApplication()
        clickIntroCloseButton()
        setUserPreferenceConnectionPermissionToAlways()

        // Coordinates
        testUri(
            Position("50.0525078", "14.0184810", z = "9"),
            "https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
        )
        testUri(
            Position("50.0525078", "14.0184810", z = "9"),
            "https://mapy.cz?x=14.0184810&y=50.0525078&z=9",
        )

        // Place
        testUri(
            Position("50.0992553", "14.4336590", z = "19"),
            "https://mapy.com/en/zakladni?source=firm&id=13362491&x=14.4336590&y=50.0992553&z=19",
        )

        // Short URI
        testUri(
            Position("50.0831498", "14.4549515", z = "17"),
            "https://mapy.com/s/jakuhelasu",
        )
        testUri(
            Position("50.0858554", "14.4624724", z = "17"),
            "https://mapy.cz/s/jetucaputu",
        )
    }
}

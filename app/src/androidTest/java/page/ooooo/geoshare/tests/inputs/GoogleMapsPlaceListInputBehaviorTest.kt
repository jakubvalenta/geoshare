package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.configureConnectionPermissionPreference
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.testUriFails
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class GoogleMapsPlaceListInputBehaviorTest : InputBehaviorTest {
    @Test
    fun googleMapsPlaceListInput_online() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()
        configureConnectionPermissionPreference(Permission.ALWAYS)

        if (htmlParsingSupported) {
            testUri(
                persistentListOf(
                    WGS84Point(59.1293656, 11.4585672, source = Source.JAVASCRIPT),
                    WGS84Point(59.4154007, 11.659710599999999, source = Source.JAVASCRIPT),
                    WGS84Point(59.3443991, 11.672637, source = Source.JAVASCRIPT),
                    WGS84Point(59.2557409, 11.5857853, source = Source.JAVASCRIPT),
                    WGS84Point(59.1579458, 11.7337507, source = Source.JAVASCRIPT),
                    WGS84Point(59.229344899999994, 11.6892173, source = Source.JAVASCRIPT),
                    WGS84Point(59.2999243, 11.6587237, source = Source.JAVASCRIPT),
                    WGS84Point(59.147731699999994, 11.550661199999999, source = Source.JAVASCRIPT),
                ),
                "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg",
            )
        } else {
            testUriFails(
                setOf(
                    "Place lists are not supported",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                    "Les listes de lieux ne sont pas prises en charge",
                ),
                "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg",
            )
        }
    }
}

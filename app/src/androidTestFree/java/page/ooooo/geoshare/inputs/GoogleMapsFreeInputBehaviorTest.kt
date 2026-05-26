package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class GoogleMapsFreeInputBehaviorTest : InputBehaviorTest {
    @Test
    fun googleMapsHtmlFree() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        setUserPreferenceConnectionPermissionToAlways()

        // Search
        testUri(
            WGS84Point(
                51.0657922, 13.7555827,
                name = @Suppress("SpellCheckingInspection") "Louisenstraße 60, 01099 Dresden",
                source = Source.URI,
            ),
            "https://www.google.com/maps/search/?api=1&query=Louisenstra%C3%9Fe%2060,%2001099%20Dresden",
        )

        // Short link with coordinates in HTML
        testUri(
            WGS84Point(
                51.1982447, 6.4389493,
                name = @Suppress("SpellCheckingInspection") "Konditorei+Heinemann",
                source = Source.URI,
            ),
            "https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8",
        )

        // Place
        testUri(
            WGS84Point(52.5200066, 13.404954, name = "Berlin", source = Source.URI),
            "https://www.google.com/maps/place/Berlin,+Germany/",
        )

        // Directions address
        testUri(
            WGS84Point(43.7481, -79.6332, name = "2088 Albion Rd @43.7481,-79.6332", source = Source.HTML),
            @Suppress("SpellCheckingInspection") "https://maps.google.com/maps?f=d&daddr=2088%20Albion%20Rd+@43.7481,-79.6332&doflg=ptm&navigate=yes",
        )

        // Directions with geocode parameter, which can get stuck at intermediate URI with zero coordinates during web parsing
        testUri(
            WGS84Point(
                40.6400537, 22.9589055,
                z = 6.0,
                name = @Suppress("SpellCheckingInspection") "Box+now+Ακροπόλεως+65,+Akropoleos+65,+Thessaloniki+546+34,+Greece",
                source = Source.URI,
            ),
            "https://maps.google.com/maps?oe=utf-8&client=firefox-b&um=1&ie=UTF-8&fb=1&gl=fr&sa=X&geocode=KWmqxjsAOagUMaSMgMRdOas1&daddr=Akropoleos+65,+Thessaloniki+546+34,+Gr%C3%A8ce",
            fallbackPoint = WGS84Point(
                40.6400537, 22.9589055,
                z = 6.0,
                name = @Suppress("SpellCheckingInspection") "Box+now+Ακροπόλεως+65,+Akropoleos+65,+Thessaloniki+546+34,+Grèce",
                source = Source.URI,
            ),
        )

        // Place list
        testUri(
            persistentListOf(
                GCJ02Point(59.1293656, 11.4585672, source = Source.JAVASCRIPT),
                GCJ02Point(59.4154007, 11.659710599999999, source = Source.JAVASCRIPT),
                GCJ02Point(59.3443991, 11.672637, source = Source.JAVASCRIPT),
                GCJ02Point(59.2557409, 11.5857853, source = Source.JAVASCRIPT),
                GCJ02Point(59.1579458, 11.7337507, source = Source.JAVASCRIPT),
                GCJ02Point(59.229344899999994, 11.6892173, source = Source.JAVASCRIPT),
                GCJ02Point(59.2999243, 11.6587237, source = Source.JAVASCRIPT),
                GCJ02Point(59.147731699999994, 11.550661199999999, source = Source.JAVASCRIPT),
            ),
            "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg",
        )
    }
}

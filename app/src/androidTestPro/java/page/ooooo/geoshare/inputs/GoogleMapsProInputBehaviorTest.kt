package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class GoogleMapsProInputBehaviorTest : InputBehaviorTest {
    @Test
    fun googleMapsHtmlPro() = uiAutomator {
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
                name = @Suppress("SpellCheckingInspection") "Louisenstraße 60, 01099 Dresden",
                source = Source.URI,
            ),
            "https://www.google.com/maps/search/?api=1&query=Louisenstra%C3%9Fe%2060,%2001099%20Dresden",
        )

        // Short links with coordinates in HTML
        testUri(
            WGS84Point(
                name = @Suppress("SpellCheckingInspection") "Café Heinemann, Bismarckstraße 91, 41061 Mönchengladbach",
                source = Source.URI,
            ),
            "https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8",
        )
        testUri(
            WGS84Point(
                name = @Suppress("SpellCheckingInspection") "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                source = Source.URI,
            ),
            "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
        )
        testUri(
            WGS84Point(
                name = @Suppress("SpellCheckingInspection") "Volkspark Hasenheide, Columbiadamm 160, 12049 Berlin, Germany",
                source = Source.URI,
            ),
            "https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6",
        )

        // Place
        testUri(
            WGS84Point(
                name = @Suppress("SpellCheckingInspection") "Hermannstr. 20, Berlin",
                source = Source.URI,
            ),
            "https://www.google.com/maps/place/Hermannstr.+20,+Berlin/"
        )

        // Directions address
        testUri(
            WGS84Point(name = "2088 Albion Rd @43.7481,-79.6332", source = Source.URI),
            @Suppress("SpellCheckingInspection") "https://maps.google.com/maps?f=d&daddr=2088%20Albion%20Rd+@43.7481,-79.6332&doflg=ptm&navigate=yes",
        )

        // Directions with geocode parameter, which can get stuck at intermediate URI with zero coordinates during web parsing
        testUri(
            WGS84Point(
                name = @Suppress("SpellCheckingInspection") "Akropoleos 65, Thessaloniki 546 34, Greece",
                source = Source.URI,
            ),
            "https://maps.google.com/maps?oe=utf-8&client=firefox-b&um=1&ie=UTF-8&fb=1&gl=fr&sa=X&geocode=KWmqxjsAOagUMaSMgMRdOas1&daddr=Akropoleos+65,+Thessaloniki+546+34,+Gr%C3%A8ce",
            fallbackPoint = WGS84Point(
                name = @Suppress("SpellCheckingInspection") "Akropoleos 65, Thessaloniki 546 34, Grèce",
                source = Source.URI,
            ),
        )

        // Place list
        testUriFailed("https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg")
    }

    private fun UiAutomatorTestScope.testUriFailed(unsafeUriString: String, timeoutMs: Long = NETWORK_TIMEOUT) {
        // Go to main form
        goToMainForm()

        // Share URI and confirm permission dialog
        shareUri(unsafeUriString)
        confirmDialogIfVisible()

        assertConversionFailed()
    }
}

package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.assumeNotEmulator
import page.ooooo.geoshare.tests.closeIntro
import page.ooooo.geoshare.tests.configureConnectionPermissionPreference
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.testUriFails
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class GoogleMapsHtmlInputBehaviorTest : InputBehaviorTest {
    @Test
    fun googleMapsHtmlInput_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)

        if (htmlParsingSupported) {
            testUri(
                persistentListOf(
                    WGS84Point(
                        30.0598138, 31.2144658,
                        z = 17.0,
                        name = "The National Circus in Giza",
                        source = Source.URI,
                    ),
                ),
                "https://maps.google.com/maps?client=firefox-b-m&um=1&ie=UTF-8&fb=1&gl=eg&sa=X&ftid=0x1458411f8f55adbb:0xbc693e01227e010d",
                fallbackNames = setOf("السيرك القومى بالجيزة\u202D"),
            )
        } else {
            testUriFails(
                setOf(
                    "This link is not supported",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Ce lien n’est pas pris en charge",
                ),
                "https://maps.google.com/maps?client=firefox-b-m&um=1&ie=UTF-8&fb=1&gl=eg&sa=X&ftid=0x1458411f8f55adbb:0xbc693e01227e010d",
            )
        }
    }

    @Test
    fun googleMapsHtmlInput_online_googleSearch() = uiAutomator {
        assumeNotEmulator()
        assumeTrue(
            "This test currently fails, because Google returns a captcha, even though we only run the test on a real device",
            false,
        )

        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)

        // Google Search
        testUri(
            WGS84Point(27.765028, -15.600889, source = Source.JAVASCRIPT),
            "https://g.co/kgs/91UYXud",
        )
    }
}

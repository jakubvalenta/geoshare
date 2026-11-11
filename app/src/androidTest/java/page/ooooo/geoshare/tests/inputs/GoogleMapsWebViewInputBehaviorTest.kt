package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.testUriFails

class GoogleMapsWebViewInputBehaviorTest : InputBehaviorTest {
    @Test
    fun googleMapsWebViewInput_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // CID only
        if (htmlParsingSupported) {
            testUri(
                WGS84Point(42.7032765, -71.154348, name = "New Balance Factory Store Lawrence", source = Source.URI),
                "https://maps.google.com/maps?cid=6827948439861580867",
                grantConnectionPermission = true,
            )
        } else {
            testUriFails(
                setOf(
                    "This link is not supported",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Ce lien n’est pas pris en charge",
                ),
                "https://maps.google.com/maps?cid=6827948439861580867",
            )
        }

        // FTID only
        if (htmlParsingSupported) {
            // Fails on physical Nexus 5 for some reason
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
                grantConnectionPermission = true,
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

}

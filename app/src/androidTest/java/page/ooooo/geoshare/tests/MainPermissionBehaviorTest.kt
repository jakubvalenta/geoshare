package page.ooooo.geoshare.tests

import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MainPermissionBehaviorTest {
    @Test
    fun whenShortLinkIsSharedAndPermissionIsGrantedWithoutDoNotAsk_showsPointAndShowsDialogAgain() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.apple")
        }

        // Share a short link with the app
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Grant connection permission
        onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()

        // Shows precise location
        assertConversionSucceeds(
            WGS84Point(52.4737758, 13.4373898, source = Source.HTML),
        )

        // Share a short link with the app again
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")
        waitForStableInActiveWindow()

        // Connection permission dialog is visible again
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }
    }

    @Test
    fun whenShortLinkIsSharedAndPermissionIsGrantedWithDoNotAsk_showsPointAndDoesNotShowDialogAgain() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.apple")
        }

        // Share a short link with the app
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Grant connection permission and check "Don't ask me again"
        onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
            toggleDoNotAsk()
            confirmDialog()
        }

        // Shows precise location
        assertConversionSucceeds(
            WGS84Point(52.4737758, 13.4373898, source = Source.HTML),
        )

        // Share a short link with the app again
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")
        quickWaitForStableInActiveWindow()

        // Shows precise location again
        assertConversionSucceeds(
            WGS84Point(52.4737758, 13.4373898, source = Source.HTML),
        )
    }

    @Test
    fun whenShortLinkIsSharedAndPermissionIsDeniedWithoutDoNotAsk_closesDialogAndShowsDialogAgain() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.apple")
        }

        // Share a Google Maps short link with the app
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Deny connection permission
        onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.dismissDialog()

        // Shows permission denied error
        assertPermissionDenied()

        // Share the Google Maps short link with the app again
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Connection permission dialog is visible again
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }
    }

    @Test
    fun whenShortLinkIsSharedAndPermissionIsDeniedWithDoNotAsk_closesDialogAndDoesNotShowDialogAgain() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.apple")
        }

        // Share a Google Maps short link with the app
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Deny connection permission
        onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
            toggleDoNotAsk()
            dismissDialog()
        }

        // Shows permission denied error
        assertPermissionDenied()

        // Share the Google Maps short link with the app again
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Shows permission denied error
        assertPermissionDenied()
    }

    @Test
    fun whenNonexistentShortLinkIsSharedAndPermissionIsDenied_closesDialogAndDoesNothing() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.app.goo.gl")
        }

        // Share a Google Maps short non-existent link with the app
        shareUri("https://maps.app.goo.gl/spam")

        // Grant connection permission
        onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()

        // Error is visible
        assertConversionFails(
            setOf(
                "Response error 404",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Erreur de réponse 404",
            ),
        )
    }

    @Test
    fun whenLinkRequiringHtmlParsingIsSharedAndPermissionIsGrantedWithoutDoNotAsk_showsPointAndShowsDialogAgain() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.apple.com")
            }

            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I7BA098CC17989C16&_provider=9902")

            // Grant connection permission
            onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()

            // Shows precise location
            assertConversionSucceeds(WGS84Point(52.4697882, 13.4257989, source = Source.HTML))

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I849C144AAC7A794F&_provider=9902")
            quickWaitForStableInActiveWindow()

            // Connection permission dialog is visible again
            onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }
        }

    @Test
    fun whenLinkRequiringHtmlParsingIsSharedAndPermissionIsGrantedWithDoNotAsk_showsPointAndDoesNotShowDialogAgain() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.apple.com")
            }

            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I1CBDEBCF5A275CB2&_provider=9902")

            // Grant connection permission and check "Don't ask me again"
            onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
                toggleDoNotAsk()
                confirmDialog()
            }

            // Shows precise location
            assertConversionSucceeds(WGS84Point(52.4778665, 13.426398, source = Source.HTML))

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I6E0F00362159B5EC&_provider=9902")
            quickWaitForStableInActiveWindow()

            // Shows precise location again
            assertConversionSucceeds(WGS84Point(52.4820815, 13.4338421, source = Source.HTML))
        }

    @Test
    fun whenLinkRequiringHtmlParsingIsSharedAndPermissionIsDeniedWithoutDoNotAsk_closesDialogAndShowsDialogAgain() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.apple.com")
            }

            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I8D204FAB527CE0EB&_provider=9902")

            // Deny connection permission
            onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.dismissDialog()

            // Shows permission denied error
            assertPermissionDenied()

            // Share the Apple Maps place link with the app again
            shareUri("https://maps.apple.com/place?place-id=IE08A4F5FAA2F0502&_provider=9902")

            // Connection permission dialog is visible again
            onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }
        }

    @Test
    fun whenLinkRequiringHtmlParsingIsSharedAndPermissionIsDeniedWithDoNotAsk_closesDialogAndDoesNotShowDialogAgain() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.apple.com")
            }

            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I5ECF0E5A2703FCD1&_provider=9902")

            // Deny connection permission
            onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
                toggleDoNotAsk()
                dismissDialog()
            }

            // Shows permission denied error
            assertPermissionDenied()

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I263680A7B546CF16&_provider=9902")

            // Shows permission denied error
            assertPermissionDenied()
        }

    private fun UiAutomatorTestScope.assertPermissionDenied() {
        onElement {
            viewIdResourceName == "geoShareConversionErrorMessage" && textAsString() in setOf(
                "This link is not supported without connecting to the map service",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "Ce lien n’est pas pris en charge sans connexion au service de cartographie",
            )
        }
    }
}

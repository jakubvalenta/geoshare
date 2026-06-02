package page.ooooo.geoshare

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source

class ConversionFreeBehaviorTest {
    @Test
    fun whenLinkWithNameOnlyIsSharedAndPermissionIsGrantedWithoutDoNotAsk_showsPointAndShowsDialogAgain() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")
            }

            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+10,+Berlin/")

            // Grant connection permission
            onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()

            // Shows precise location
            assertConversionSucceeded(
                GCJ02Point(
                    52.4848232, 13.4240791,
                    name = @Suppress("SpellCheckingInspection") "Hermannstraße 10, 12049 Berlin",
                    source = Source.URI,
                )
            )

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+11,+Berlin/")
            quickWaitForStableInActiveWindow()

            // Connection permission dialog is visible again
            onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }
        }

    @Test
    fun whenLinkWithNameOnlyIsSharedAndPermissionIsGrantedWithDoNotAsk_showsPointAndDoesNotShowDialogAgain() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")
            }

            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")

            // Grant connection permission and check "Don't ask me again"
            onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
                toggleDoNotAsk()
                confirmDialog()
            }

            // Shows precise location
            assertConversionSucceeded(
                GCJ02Point(
                    52.4834254, 13.4245399,
                    name = @Suppress("SpellCheckingInspection") "Hermannstraße 20, 12049 Berlin",
                    source = Source.URI,
                ),
            )

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+21,+Berlin/")
            quickWaitForStableInActiveWindow()

            // Shows precise location again
            assertConversionSucceeded(
                GCJ02Point(
                    52.4832988, 13.4245179,
                    name = @Suppress("SpellCheckingInspection") "Hermannstraße 21, 12049 Berlin",
                    source = Source.URI,
                )
            )
        }

    @Test
    fun whenLinkWithNameOnlyIsSharedAndPermissionIsDeniedWithoutDoNotAsk_closesDialogAndShowsDialogAgain() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")
            }

            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+30,+Berlin/")

            // Deny connection permission
            onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.dismissDialog()

            // Shows location search
            assertConversionSucceeded(
                GCJ02Point(
                    name = @Suppress("SpellCheckingInspection") "Hermannstr. 30, Berlin",
                    source = Source.URI,
                )
            )

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+31,+Berlin/")

            // Connection permission dialog is visible again
            onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }
        }

    @Test
    fun whenLinkWithNameOnlyIsSharedAndPermissionIsDeniedWithDoNotAsk_closesDialogAndDoesNotShowDialogAgain() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")
            }

            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+40,+Berlin/")

            // Deny connection permission
            onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
                toggleDoNotAsk()
                dismissDialog()
            }

            // Shows location search
            assertConversionSucceeded(
                GCJ02Point(
                    name = @Suppress("SpellCheckingInspection") "Hermannstr. 40, Berlin",
                    source = Source.URI,
                )
            )

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+41,+Berlin/")
            quickWaitForStableInActiveWindow()

            // Shows location search
            assertConversionSucceeded(
                GCJ02Point(
                    name = @Suppress("SpellCheckingInspection") "Hermannstr. 41, Berlin",
                    source = Source.URI,
                )
            )
        }
}

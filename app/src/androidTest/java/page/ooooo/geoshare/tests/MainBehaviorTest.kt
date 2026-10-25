package page.ooooo.geoshare.tests

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Test
import page.ooooo.geoshare.data.android.MimeType
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.TextActivity
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.android.UriScheme
import page.ooooo.geoshare.data.formatters.CoordinateFormatter
import page.ooooo.geoshare.data.geo.GCJ02Point
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.outputs.OpenUnknownUriOutput
import page.ooooo.geoshare.data.outputs.SendStringOutput
import page.ooooo.geoshare.data.preferences.HelpMessage
import kotlin.time.Duration.Companion.seconds

class MainBehaviorTest {
    @Test
    fun whenTextIsEntered_showsPointAndAllowsOpeningGoogleMaps() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Enter text in the main form and submit it
        val point = WGS84Point(NaivePoint.genRandomPoint().copy(source = Source.URI))
        setMainInput(point)
        submitMainForm()

        // Tap the Google Maps icon
        scrollToAppIcon(PackageNames.GOOGLE_MAPS).clickAppIcon()

        // Wait for Google Maps
        waitAndAssertGoogleMapsContainsElement { true }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()

        // Shows result screen, because the app didn't finish
        assertConversionSucceeds(point)
    }

    @Test
    fun whenLinkIsShared_showsCoordinatesAndAllowsOpeningGoogleMapsAndFinishes() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows precise location
        assertConversionSucceeds(WGS84Point(52.5067296, 13.2599309, z = 11.0, source = Source.MAP_CENTER))

        // Tap the Google Maps icon
        scrollToAppIcon(PackageNames.GOOGLE_MAPS).clickAppIcon()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement { textAsString() in setOf("Westend", "Berlin-Westend") }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()

        // Shows main screen instead of result screen, because the app finished
        onElement { viewIdResourceName == "geoShareMainSourceTextField" }
    }

    @Test
    fun whenLinkWithCoordinatesWithinMainlandChinaIsShared_showsPointAndAllowsOpeningGoogleMapsInGCJ02() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@31.22850685422705,121.47552456472106,11z")

        // Shows precise location
        val expectedPoint = GCJ02Point(
            31.22850685422705, 121.47552456472106,
            z = 11.0,
            source = Source.MAP_CENTER,
        )
        assertConversionSucceeds(expectedPoint)

        // Tap the Google Maps icon
        scrollToAppIcon(PackageNames.GOOGLE_MAPS).clickAppIcon()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement {
            textAsString() in setOf(
                "Ming&Qing Dynasties Furniture Hall",
                """31°13'42.6"N 121°28'31.9"E""", // Sometimes shown on Nexus 5 instead of place name
            )
        }
    }

    @Test
    fun whenLinkIsShared_allowsOpeningTextSource() = uiAutomator {
        val messagingAppPackageName = PackageNames.CONVERSATIONS
        assumeAppInstalled(messagingAppPackageName)

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Enter text in the main form and submit it
        setMainInput()
        submitMainForm()

        // Open the source sheet and tap an app
        onElement { viewIdResourceName == "geoShareMainSourceButton" }.click()
        val output = SendStringOutput(TextActivity(messagingAppPackageName, MimeType.TEXT_PLAIN))
        onElement { viewIdResourceName == "geoShareConversionUriSheet" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareResultSheetItem_${output.id}" }
            .click()

        // Opens the messaging app
        onElement { packageName == messagingAppPackageName }
    }

    @Test
    fun whenLinkIsShared_allowsOpeningUriSource() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Park+am+Gleisdreieck/@52.4911357,13.3764779,15z/data=!4m6!3m5!1s0x47a850301366c17b:0x5c368c54cfd1eb6!8m2!3d52.4945256!4d13.3765945!16s")

        // Wait for the conversion to succeed
        assertConversionSucceeds(
            WGS84Point(
                52.4945256, 13.3765945,
                z = 15.0,
                name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Park am Gleisdreieck",
                source = Source.URI,
            )
        )

        // Open the source sheet and tap an app
        onElement { viewIdResourceName == "geoShareMainSourceButton" }.click()
        val output = OpenUnknownUriOutput(UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.UNKNOWN))
        onElement { viewIdResourceName == "geoShareConversionUriSheet" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareResultSheetItem_${output.id}" }
            .click()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement {
            textAsString() == @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Park am Gleisdreieck"
        }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()

        // Shows main screen instead of result screen, because the app finished
        onElement { viewIdResourceName == "geoShareMainSourceTextField" }
    }

    @Test
    fun whenLinkIsShared_allowsOpeningUriSourceAndDoesNotCancelPendingConversion() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+10,+Berlin/")

        // Grant connection permission
        grantConnectionPermission()

        // Open the source sheet
        onElement { viewIdResourceName == "geoShareMainSourceButton" }.click()
        val output = OpenUnknownUriOutput(UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.UNKNOWN))
        onElement { viewIdResourceName == "geoShareConversionUriSheet" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareResultSheetItem_${output.id}" }
            // Tap an app while the conversion is still running. This is an imperfect test scenario, because there's a
            // small chance that the conversion finishes before we tap the app.
            .click()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement {
            textAsString() == @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Hermannstraße 10"
        }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()

        // Assert that the conversion wasn't canceled
        assertConversionSucceeds(
            GCJ02Point(
                52.4848232, 13.4240791,
                name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "Hermannstraße 10, 12049 Berlin",
                source = Source.URI,
            ),
            fallbackNames = setOf(
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "Hermannstraße 10, 12049 Berlin-Bezirk Neukölln",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "Hermannstraße 10, 12049 Berlin-Bezirk Neukölln, Allemagne",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "Hermannstraße 10, 12049 Berlin-Bezirk Neukölln, Germany",
            ),
        )
    }

    @Test
    fun whenLinkIsShared_allowsOpeningMessagingApp() = uiAutomator {
        val messagingAppPackageName = PackageNames.CONVERSATIONS
        assumeAppInstalled(messagingAppPackageName)

        // Share a URI with the app
        shareUri()

        // Tap the messaging app icon
        scrollToAppIcons()
        scrollToAppIcon(messagingAppPackageName).clickAppIcon()

        // Opens the messaging app
        onElement { packageName == messagingAppPackageName }
    }

    @Test
    fun whenLinkIsShared_allowsOpeningGoogleMapsSearchLink() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Enter a point in the main form and submit it. Use the form instead of sharing the point, so that the app
        // doesn't finish, which causes clicking on a non-clickable element on Redmi 8
        val query = "foo"
        setMainInput(WGS84Point(name = query, source = Source.GENERATED))
        submitMainForm()

        // Click the link
        scrollToLinkIcons()
        scrollToLinkIcon("Google Maps").longClick()
        onElement {
            viewIdResourceName == "geoShareAppOutput" && textAsString()?.contains("Google Maps search") == true
        }.click()

        // Google Maps shows the search query
        clickSystemShareMenuIfShown("Maps")
        waitAndAssertGoogleMapsContainsElement { textAsString() == query }
    }

    @Test
    fun whenLinkIsShared_allowsLaunchingNavigationInTomTom() = uiAutomator {
        runBlocking {
            assumeAppInstalled(PackageNames.TOMTOM)
            assumeDomainResolvable("tomtom.com")
            assumeTrue(
                "This test currently fails, because the current version of the TomTom app doesn't seem to support GPX anymore",
                false,
            )

            // Share a URI with the app
            shareUri()

            // Launch navigation in TomTom
            scrollToAppIcon(PackageNames.TOMTOM).longClick()
            launchNavigationInApp()

            // Dismiss the location rationale dialog
            onElement(20_000) { viewIdResourceName == "geoShareLocationRationaleDialog" }.dismissDialog()

            // Launch navigation in TomTom again
            scrollToAppIcon(PackageNames.TOMTOM).longClick()
            launchNavigationInApp()

            // Confirm location rationale
            onElement(20_000) { viewIdResourceName == "geoShareLocationRationaleDialog" }.confirmDialog()

            // Deny location permission
            denySystemPermission()

            // Launch navigation in TomTom again
            scrollToAppIcon(PackageNames.TOMTOM).longClick()
            launchNavigationInApp()

            // Confirm location rationale
            onElement(20_000) { viewIdResourceName == "geoShareLocationRationaleDialog" }.confirmDialog()

            // Grant location permission
            waitForStableInActiveWindow() // Wait, otherwise tapping the location permission grant button does nothing
            grantSystemPermission()

            mockLocation {
                // Set location
                launch(Dispatchers.IO) {
                    delay(3.seconds)
                    setLocation(52.474160, 13.455457)
                }

                // TomTom starts navigation
                waitAndAssertTomTomContainsElement { textAsString() in setOf("Drive", "Aller") }
            }
        }
    }

    @Test
    fun whenPointIsShared_allowsSavingGpxRoute() = uiAutomator {
        // Share a URI with the app
        shareUri("https://mapy.com/en/turisticka?planovani-trasy&rc=9eJ.Ex5-GG9eY14x5Ls1jBHe8t9emdXcI5&rs=osm&rs=osm&rs=osm&rs=pubt&ri=20227705&ri=120789104&ri=81032078&ri=28872262&mrp=%7B%22c%22%3A121%2C%22dt%22%3A%22%22%2C%22d%22%3Atrue%7D&xc=%5B%5D&rbf=alf&x=13.4308032&y=52.4684951&z=18")

        // Open sheet and tap Save GPX
        onElement { viewIdResourceName == "geoShareResultSavePoints" }.click()
        onElement { viewIdResourceName == "geoShareResultSheet" }.apply {
            expandSheet()
            onElement {
                viewIdResourceName == "geoShareSheetListItemHeadline" && textAsString() in setOf(
                    "Save GPX route",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Enregistrer l’itinéraire GPX",
                )
            }
                .click()
        }

        // Choose file
        chooseFile()

        // Shows success message
        onElement(pollIntervalMs = 50) {
            textAsString() in setOf(
                "Saved GPX file",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Fichier GPX enregistré",
            )
        }
    }

    @Test
    fun whenPointIsShared_allowsSavingPointToContact() = uiAutomator {
        // Share a random point with the app
        val point = WGS84Point(NaivePoint.genRandomPoint().copy(source = Source.URI))
        shareUri(point)

        // Open sheet and tap Save to contact
        onElement { viewIdResourceName == "geoShareResultSave" }.click()
        onElement { viewIdResourceName == "geoShareResultSheet" }.apply {
            expandSheet()
            onElement {
                viewIdResourceName == "geoShareSheetListItemHeadline" && textAsString() in setOf(
                    "Save to contact",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Enregistrer dans les contacts",
                )
            }
                .click()
        }

        // Insert or edit the test contact
        insertOrEditContact()

        // Open the test contact
        openContact()

        // The test contact contains coordinates of the point
        assertContactContainsText(CoordinateFormatter.formatDecCoords(point))
    }

    @Test
    fun whenLinkIsSharedAndCurrentScreenIsNotMainScreen_navigatesToMainScreen() = uiAutomator {
        // Share a URI with the app
        val firstPoint = WGS84Point(NaivePoint.genRandomPoint().copy(source = Source.URI))
        shareUri(firstPoint)

        // Shows result
        assertConversionSucceeds(firstPoint)

        // Go to about screen
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement { viewIdResourceName == "geoShareMainMenuAbout" }.click()

        // Share another URI with the app
        val secondPoint = WGS84Point(NaivePoint.genRandomPoint().copy(source = Source.URI))
        shareUri(secondPoint)

        // Shows result
        assertConversionSucceeds(secondPoint)
    }

    @Test
    fun whenLaunched_showsHelpMessagesAndAllowsDismissingThem() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Dismiss help message WELCOME
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.WELCOME}" }
            .onElement { viewIdResourceName == "geoShareHelpMessageDismiss" }
            .click()
        waitForHelpMessageToDisappear()
        assertNull(
            onElementOrNull(500) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.WELCOME}"
            }
        )

        // Share a URI with the app
        shareUri()
        quickWaitForStableInActiveWindow() // Wait to prevent clicking a non-clickable element

        // Help message SHARE_SOURCE is not visible
        assertNull(
            onElementOrNull(500) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}"
            }
        )

        // Dismiss help message OPEN_BY_DEFAULT is visible
        onMainScrollablePane().scrollToBottom()
        quickWaitForStableInActiveWindow() // Wait to prevent clicking a non-clickable element
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}" }
            .onElement { viewIdResourceName == "geoShareHelpMessageDismiss" }
            .click()
        waitForHelpMessageToDisappear()
        assertNull(
            onElementOrNull(500) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}"
            }
        )

        // Enter text in the main form and submit it
        goBackToMainForm()
        setMainInput()
        submitMainForm()
        quickWaitForStableInActiveWindow() // Wait to prevent clicking a non-clickable element

        // Help message OPEN_BY_DEFAULT is not visible
        assertNull(
            onElementOrNull(500) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}"
            }
        )

        // Dismiss help message SHARE_SOURCE
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}" }
            .onElement { viewIdResourceName == "geoShareHelpMessageDismiss" }
            .click()
        waitForHelpMessageToDisappear()
        assertNull(
            onElementOrNull(500) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}"
            }
        )
    }
}

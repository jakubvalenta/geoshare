package page.ooooo.geoshare.tests

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import kotlin.time.Duration.Companion.seconds

class MainBehaviorTest {
    @Test
    fun whenTextIsEntered_showsPointAndAllowsOpeningGoogleMaps() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Enter text in the main form and submit it
        testText(
            WGS84Point(45.4786785, 9.2473799, source = Source.URI),
            "geo:45.4786785,9.2473799",
        )

        // Tap the Google Maps icon
        clickAppIcon(PackageNames.GOOGLE_MAPS)

        // Wait for Google Maps
        onElement(20_000) { packageName == PackageNames.GOOGLE_MAPS }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()

        // Shows result screen, because the app didn't finish
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }
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
        clickAppIcon(PackageNames.GOOGLE_MAPS)

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
        clickAppIcon(PackageNames.GOOGLE_MAPS)

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
        onElement { viewIdResourceName == "geoShareConversionUriSheet" }
            .scrollToElement(Direction.DOWN) {
                viewIdResourceName == "geoShareConversionUriSheetItem_$messagingAppPackageName" // FIXME
            }
            .click()

        // Opens the messaging app
        onElement { packageName == messagingAppPackageName }
    }

    @Test
    fun whenLinkIsShared_allowsOpeningUriSource() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Park+am+Gleisdreieck/@52.4911357,13.3764779,15z/data=!4m6!3m5!1s0x47a850301366c17b:0x5c368c54cfd1eb6!8m2!3d52.4945256!4d13.3765945!16s")

        // Open the source sheet and tap an app
        onElement { viewIdResourceName == "geoShareMainSourceButton" }.click()
        onElement { viewIdResourceName == "geoShareConversionUriSheet" }
            .scrollToElement(Direction.DOWN) {
                viewIdResourceName == "geoShareConversionUriSheetItem_${PackageNames.GOOGLE_MAPS}" // FIXME
            }
            .click()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement {
            textAsString() == @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Park am Gleisdreieck"
        }
    }

    @Test
    fun whenLinkIsShared_allowsOpeningMessagingApp() = uiAutomator {
        val messagingAppPackageName = PackageNames.CONVERSATIONS
        assumeAppInstalled(messagingAppPackageName)

        // Share a URI with the app
        shareUri()

        // Tap the messaging app icon
        scrollToAppIcons()
        clickAppIcon(messagingAppPackageName)

        // Opens the messaging app
        onElement { packageName == messagingAppPackageName }
    }

    @Test
    fun whenLinkIsShared_allowsOpeningGoogleMapsSearchLink() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Share a point with name with the app
        val query = "foo"
        shareUri(WGS84Point(name = query, source = Source.GENERATED))

        // Click the link
        scrollToLinkIcons()
        onElement { viewIdResourceName == "geoShareLink_Google Maps" }.longClick()
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

            // Share a URI with the app
            shareUri()

            // Launch navigation in TomTom
            scrollToAppIcons()
            launchNavigationInApp(PackageNames.TOMTOM)

            // Dismiss the location rationale dialog
            onElement(20_000) { viewIdResourceName == "geoShareLocationRationaleDialog" }.dismissDialog()

            // Launch navigation in TomTom again
            launchNavigationInApp(PackageNames.TOMTOM)

            // Confirm location rationale
            onElement(20_000) { viewIdResourceName == "geoShareLocationRationaleDialog" }.confirmDialog()

            // Deny location permission
            denySystemPermission()

            // Launch navigation in TomTom again
            launchNavigationInApp(PackageNames.TOMTOM)

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
        shareUri()

        // Open sheet and tap Save GPX
        onElement { viewIdResourceName == "geoShareResultLastPointMenu" }.click()
        onElement { viewIdResourceName == "geoShareResultSheet" }.run {
            expandSheet()
            longScrollSheet() // Speed up scrolling to the item, which is at the bottom of the sheet
            scrollToElement(Direction.DOWN) {
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
        val point = WGS84Point(NaivePoint.genRandomPoint())
        shareUri(point)

        // Open sheet and tap Save to contact
        onElement { viewIdResourceName == "geoShareResultLastPointMenu" }.click()
        onElement { viewIdResourceName == "geoShareResultSheet" }.run {
            expandSheet()
            longScrollSheet() // Speed up scrolling to the item, which is at the bottom of the sheet
            scrollToElement(Direction.DOWN) {
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
    fun whenLaunched_showsHelpMessagesAndAllowsDismissingThem() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Help message WELCOME is visible
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.WELCOME}" }

        // Dismiss help message WELCOME
        onElement { viewIdResourceName == "geoShareHelpMessageDismiss_${HelpMessage.WELCOME}" }.click()
        quickWaitForStableInActiveWindow() // Wait for help message exit animation
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.WELCOME}"
            }
        )

        // Share a URI with the app
        shareUri()

        // Help message OPEN_BY_DEFAULT is visible
        onMainScrollablePane()
            // Scroll by percents not to element, because it's more reliable due to the lazy list loading
            .scroll(Direction.DOWN, 3f)
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}" }

        // Help message SHARE_SOURCE is not visible
        onMainScrollablePane().scroll(Direction.UP, 3f) // Scroll up to see the message
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}"
            }
        )

        // Dismiss help message OPEN_BY_DEFAULT
        onMainScrollablePane()
            // Scroll by percents not to element, because it's more reliable due to the lazy list loading
            .scroll(Direction.DOWN, 3f)
        onElement { viewIdResourceName == "geoShareHelpMessageDismiss_${HelpMessage.OPEN_BY_DEFAULT}" }.click()
        quickWaitForStableInActiveWindow() // Wait for help message exit animation
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}"
            }
        )

        // Enter text in the main form and submit it
        goBackToMainForm()
        setMainInput()
        submitMainForm()

        // Help message SHARE_SOURCE is visible
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}" }

        // Help message OPEN_BY_DEFAULT is not visible
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}"
            }
        )

        // Dismiss help message SHARE_SOURCE
        onElement { viewIdResourceName == "geoShareHelpMessageDismiss_${HelpMessage.SHARE_SOURCE}" }.click()
        quickWaitForStableInActiveWindow() // Wait for help message exit animation
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}"
            }
        )
    }
}

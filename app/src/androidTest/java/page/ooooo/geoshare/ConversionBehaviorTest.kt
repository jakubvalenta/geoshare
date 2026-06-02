package page.ooooo.geoshare

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import kotlin.time.Duration.Companion.seconds

class ConversionBehaviorTest {
    @Test
    fun whenFullUriIsShared_showsPointAndAllowsOpeningGoogleMaps() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows precise location
        assertConversionSucceeded(WGS84Point(52.5067296, 13.2599309, z = 11.0, source = Source.MAP_CENTER))

        // Tap the Google Maps icon
        onElement { viewIdResourceName == "geoShareApp_${PackageNames.GOOGLE_MAPS}" }.click()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement { textAsString() in setOf("Westend", "Berlin-Westend") }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

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
        assertConversionSucceeded(expectedPoint)

        // Tap the Google Maps icon
        onElement { viewIdResourceName == "geoShareApp_${PackageNames.GOOGLE_MAPS}" }.click()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement { textAsString() == "Ming&Qing Dynasties Furniture Hall" }
    }

    @Test
    fun whenShortLinkIsSharedAndPermissionIsGrantedWithoutDoNotAsk_showsPointAndShowsDialogAgain() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Share a short link with the app
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Grant connection permission
        onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()

        // Shows precise location
        assertConversionSucceeded(
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
            assumeDomainResolvable("maps.apple.com")
        }

        // Share a short link with the app
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Grant connection permission and check "Don't ask me again"
        onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
            toggleDoNotAsk()
            confirmDialog()
        }

        // Shows precise location
        assertConversionSucceeded(
            WGS84Point(52.4737758, 13.4373898, source = Source.HTML),
        )

        // Share a short link with the app again
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")
        quickWaitForStableInActiveWindow()

        // Shows precise location again
        assertConversionSucceeded(
            WGS84Point(52.4737758, 13.4373898, source = Source.HTML),
        )
    }

    @Test
    fun whenShortLinkIsSharedAndPermissionIsDeniedWithoutDoNotAsk_closesDialogAndShowsDialogAgain() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.apple.com")
        }

        // Share a Google Maps short link with the app
        shareUri("https://maps.apple/p/7E-Brjrk_THN14")

        // Deny connection permission
        onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.dismissDialog()

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
        onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
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
            assumeDomainResolvable("maps.google.com")
        }

        // Share a Google Maps short non-existent link with the app
        shareUri("https://maps.app.goo.gl/spam")

        // Grant connection permission
        onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()

        // Error is visible
        assertConversionFails(
            setOf(
                "Response error 404",
                @Suppress("SpellCheckingInspection") "Erreur de réponse 404",
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
            onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()

            // Shows precise location
            assertConversionSucceeded(WGS84Point(52.4697882, 13.4257989, source = Source.HTML))

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
            onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
                toggleDoNotAsk()
                confirmDialog()
            }

            // Shows precise location
            assertConversionSucceeded(WGS84Point(52.4778665, 13.426398, source = Source.HTML))

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I6E0F00362159B5EC&_provider=9902")
            quickWaitForStableInActiveWindow()

            // Shows precise location again
            assertConversionSucceeded(WGS84Point(52.4820815, 13.4338421, source = Source.HTML))
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
            onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.dismissDialog()

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
            onElement(20_000L) { viewIdResourceName == "geoShareConnectionPermissionDialog" }.run {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun opensMessagingApp() = uiAutomator {
        runBlocking {
            val messagingAppPackageName = PackageNames.CONVERSATIONS
            assumeAppInstalled(messagingAppPackageName)

            // Share a geo: URI with the app
            shareUri("geo:52.47254,13.4345")

            // Tap the messaging app icon
            onMainScrollablePane()
                // Scroll by percents, because it's more reliable than scrolling to the app icon
                .scroll(Direction.DOWN, 2f)
            onElement { viewIdResourceName == "geoShareApp_${messagingAppPackageName}" }.click()

            // Opens the messaging app
            onElement { packageName == messagingAppPackageName }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun launchesNavigationInTomTom() = uiAutomator {
        runBlocking {
            assumeAppInstalled(PackageNames.TOMTOM)
            assumeDomainResolvable("tomtom.com")

            // Share a geo: URI with the app
            shareUri("geo:52.47254,13.4345")

            // Launch navigation in TomTom
            onMainScrollablePane()
                // Scroll by percents, because it's more reliable than scrolling to the app icon
                .scroll(Direction.DOWN, 2f)
            launchNavigationInApp(PackageNames.TOMTOM)

            // Dismiss the location rationale dialog
            onElement(20_000L) { viewIdResourceName == "geoShareLocationRationaleDialog" }.dismissDialog()

            // Launch navigation in TomTom again
            launchNavigationInApp(PackageNames.TOMTOM)

            // Confirm location rationale
            onElement(20_000L) { viewIdResourceName == "geoShareLocationRationaleDialog" }.confirmDialog()

            // Deny location permission
            denySystemPermission()

            // Launch navigation in TomTom again
            launchNavigationInApp(PackageNames.TOMTOM)

            // Confirm location rationale
            onElement(20_000L) { viewIdResourceName == "geoShareLocationRationaleDialog" }.confirmDialog()

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
    fun savesGpxRoute() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Share a geo: URI with the app
        shareUri("geo:52.47254,13.4345")

        // Open copy menu
        onElement { viewIdResourceName == "geoShareResultSuccessLastPointMenu" }.click()

        onElement { viewIdResourceName == "geoShareResultSuccessSheet" }.apply {
            // Expand the sheet
            swipe(Direction.UP, 1f)

            // Tap Save GPX
            onElement { viewIdResourceName == "geoShareResultSuccessSheet" }
                .scroll(Direction.DOWN, 10f)
            onElement { viewIdResourceName == "geoShareResultSuccessSheet" }
                // Scroll again, because only now can the lazy column pane scroll all the way to the bottom
                .scrollToElement(Direction.DOWN) {
                    viewIdResourceName == "geoShareResultSuccessSheetItemHeadline" && textAsString() in setOf(
                        "Save GPX route",
                        @Suppress("SpellCheckingInspection") "Enregistrer l’itinéraire GPX",
                    )
                }
                .click()
        }

        // Choose file
        chooseFile()

        // Shows success message
        onElement {
            textAsString() in setOf(
                "Saved GPX file",
                @Suppress("SpellCheckingInspection") "Fichier GPX enregistré",
            )
        }
    }

    @Test
    fun savesPointToContact() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Share a geo: URI with the app
        val point = WGS84Point(NaivePoint.genRandomPoint())
        shareUri(GeoUriFormatter.formatGeoUriString(point))

        // Open copy menu
        onElement { viewIdResourceName == "geoShareResultSuccessLastPointMenu" }.click()

        onElement { viewIdResourceName == "geoShareResultSuccessSheet" }.apply {
            // Expand the sheet
            swipe(Direction.UP, 1f)

            // Tap Save to contact
            onElement { viewIdResourceName == "geoShareResultSuccessSheet" }
                .scroll(Direction.DOWN, 10f)
            onElement { viewIdResourceName == "geoShareResultSuccessSheet" }
                // Scroll again, because only now can the lazy column pane scroll all the way to the bottom
                .scrollToElement(Direction.DOWN) {
                    viewIdResourceName == "geoShareResultSuccessSheetItemHeadline" && textAsString() in setOf(
                        "Save to contact",
                        @Suppress("SpellCheckingInspection") "Enregistrer dans les contacts",
                    )
                }
                .click()
        }

        // Insert or edit the test contact
        insertOrEditContact()

        // Open the test contact
        openContact()

        // The test contact contains coordinates
        val expectedCoordinates = CoordinateFormatter.formatDecCoords(point)
        onElement { textAsString() == expectedCoordinates }
    }

    private fun UiAutomatorTestScope.assertPermissionDenied() {
        onElement {
            viewIdResourceName == "geoShareConversionErrorMessage" && textAsString() in setOf(
                "This link is not supported without connecting to the map service",
                @Suppress("SpellCheckingInspection") "Ce lien n’est pas pris en charge sans connexion au service de cartographie",
            )
        }
    }

    private fun UiAutomatorTestScope.launchNavigationInApp(@Suppress("SameParameterValue") packageName: String) {
        onElement { viewIdResourceName == "geoShareApp_$packageName" }.longClick()
        onElement {
            viewIdResourceName == "geoShareAppOutput" && textAsString() in setOf(
                "Navigate",
                @Suppress("SpellCheckingInspection") "Naviguer"
            )
        }.click()
    }
}

package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class ConversionBehaviorTest : BehaviorTest {

    @Test
    fun whenFullUriIsShared_showsPointAndAllowsOpeningGoogleMaps() = uiAutomator {
        assumeAppInstalled(GOOGLE_MAPS_PACKAGE_NAME)

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows precise location
        assertConversionSucceeded(GCJ02Point(52.5067296, 13.2599309, z = 11.0, source = Source.MAP_CENTER))

        // Tap the Google Maps icon
        onElement { viewIdResourceName == "geoShareApp_${GOOGLE_MAPS_PACKAGE_NAME}" }.click()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement { textAsString() in setOf("Westend", "Berlin-Westend") }
    }

    @Test
    fun whenLinkWithCoordinatesInChinaIsShared_showsPointAndAllowsOpeningGoogleMapsInGCJ02() =
        uiAutomator {
            runBlocking {
                assumeAppInstalled(GOOGLE_MAPS_PACKAGE_NAME)
                assumeDomainResolvable("maps.google.com")

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
                onElement { viewIdResourceName == "geoShareApp_${GOOGLE_MAPS_PACKAGE_NAME}" }.click()

                // Google Maps shows precise location
                waitAndAssertGoogleMapsContainsElement { textAsString() == "Ming&Qing Dynasties Furniture Hall" }
            }
        }

    @Test
    fun whenShortUriIsSharedAndPermissionIsGrantedWithoutDoNotAsk_showsPointAndShowsDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps short link with the app
                shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

                // Grant unshorten permission
                onDialog("geoShareUnshortenPermissionDialog") {
                    confirm()
                }

                // Shows precise location
                assertConversionSucceeded(
                    GCJ02Point(
                        52.4842015, 13.4167277,
                        name = @Suppress("SpellCheckingInspection") "Columbiadamm 160, 12049 Berlin",
                        source = Source.URI,
                    )
                )

                // Share another Google Maps short link with the app
                shareUri("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
                quickWaitForStableInActiveWindow()

                // Unshorten permission dialog is visible again
                onElement { viewIdResourceName == "geoShareUnshortenPermissionDialog" }
            }
        }

    @Test
    fun whenShortUriIsSharedAndPermissionIsGrantedWithDoNotAsk_showsPointAndDoesNotShowDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps short link with the app
                shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

                // Grant unshorten permission and check "Don't ask me again"
                onDialog("geoShareUnshortenPermissionDialog") {
                    toggleDoNotAsk()
                    confirm()
                }

                // Shows precise location
                assertConversionSucceeded(
                    GCJ02Point(
                        52.4842015, 13.4167277,
                        name = @Suppress("SpellCheckingInspection") "Columbiadamm 160, 12049 Berlin",
                        source = Source.URI,
                    )
                )

                // Share another Google Maps short link with the app
                shareUri("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
                quickWaitForStableInActiveWindow()

                // Shows precise location again
                assertConversionSucceeded(
                    GCJ02Point(
                        44.4490541, 26.0888398,
                        name = @Suppress("SpellCheckingInspection") "RAI - Romantic & Intimate, Calea Victoriei 202 București",
                        source = Source.URI,
                    )
                )
            }
        }

    @Test
    fun whenShortUriIsSharedAndPermissionIsDeniedWithoutDoNotAsk_closesDialogAndShowsDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps short link with the app
                shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

                // Deny unshorten permission
                onDialog("geoShareUnshortenPermissionDialog") {
                    dismiss()
                }

                // Error is visible
                onElement { viewIdResourceName == "geoShareConversionError" }

                // Close the window (this is necessary, for some reason)
                onElement { viewIdResourceName == "geoShareMainBackButton" }.click()

                // Share the Google Maps short link with the app again
                shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

                // Unshorten permission dialog is visible again
                onElement { viewIdResourceName == "geoShareUnshortenPermissionDialog" }
            }
        }

    @Test
    fun whenShortUriIsSharedAndPermissionIsDeniedWithDoNotAsk_closesDialogAndDoesNotShowDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps short link with the app
                shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

                // Deny unshorten permission
                onDialog("geoShareUnshortenPermissionDialog") {
                    toggleDoNotAsk()
                    dismiss()
                }

                // Error is visible
                onElement { viewIdResourceName == "geoShareConversionError" }

                // Close the window (this is necessary, for some reason)
                onElement { viewIdResourceName == "geoShareMainBackButton" }.click()

                // Share the Google Maps short link with the app again
                shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

                // Error is visible again
                onElement { viewIdResourceName == "geoShareConversionError" }
            }
        }

    @Test
    fun whenNonexistentShortUriIsSharedAndPermissionIsDenied_closesDialogAndDoesNothing() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps short non-existent link with the app
                shareUri("https://maps.app.goo.gl/spam")

                // Grant unshorten permission
                onDialog("geoShareUnshortenPermissionDialog") {
                    confirm()
                }

                // Error is visible
                onElement { viewIdResourceName == "geoShareConversionError" }
            }
        }

    @Test
    fun whenLinkRequiringHtmlParsingIsSharedAndPermissionIsGrantedWithoutDoNotAsk_showsPointAndShowsDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.apple.com")

                // Share an Apple Maps place link with the app
                shareUri("https://maps.apple.com/place?place-id=I7BA098CC17989C16&_provider=9902")

                // Grant parse HTML permission
                onDialog("geoShareParseHtmlPermissionDialog") {
                    confirm()
                }

                // Shows precise location
                assertConversionSucceeded(WGS84Point(52.4697882, 13.4257989, source = Source.HTML))

                // Share another Apple Maps place link with the app
                shareUri("https://maps.apple.com/place?place-id=I849C144AAC7A794F&_provider=9902")
                quickWaitForStableInActiveWindow()

                // Parse HTML permission dialog is visible again
                onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
            }
        }

    @Test
    fun whenLinkRequiringHtmlParsingIsSharedAndPermissionIsGrantedWithDoNotAsk_showsPointAndDoesNotShowDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.apple.com")

                // Share an Apple Maps place link with the app
                shareUri("https://maps.apple.com/place?place-id=I1CBDEBCF5A275CB2&_provider=9902")

                // Grant parse HTML permission and check "Don't ask me again"
                onDialog("geoShareParseHtmlPermissionDialog") {
                    toggleDoNotAsk()
                    confirm()
                }

                // Shows precise location
                assertConversionSucceeded(WGS84Point(52.4778665, 13.426398, source = Source.HTML))

                // Share another Apple Maps place link with the app
                shareUri("https://maps.apple.com/place?place-id=I6E0F00362159B5EC&_provider=9902")
                quickWaitForStableInActiveWindow()

                // Shows precise location again
                assertConversionSucceeded(WGS84Point(52.4820815, 13.4338421, source = Source.HTML))
            }
        }

    @Test
    fun whenLinkRequiringHtmlParsingIsSharedAndPermissionIsDeniedWithoutDoNotAsk_closesDialogAndShowsDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.apple.com")

                // Share an Apple Maps place link with the app
                shareUri("https://maps.apple.com/place?place-id=I8D204FAB527CE0EB&_provider=9902")

                // Deny parse HTML permission
                onDialog("geoShareParseHtmlPermissionDialog") {
                    dismiss()
                }

                // Error is visible
                onElement { viewIdResourceName == "geoShareConversionError" }

                // Close the window (this is necessary, for some reason)
                onElement { viewIdResourceName == "geoShareMainBackButton" }.click()

                // Share the Apple Maps place link with the app again
                shareUri("https://maps.apple.com/place?place-id=IE08A4F5FAA2F0502&_provider=9902")

                // Parse HTML permission dialog is visible again
                onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
            }
        }

    @Test
    fun whenLinkRequiringHtmlParsingIsSharedAndPermissionIsDeniedWithDoNotAsk_closesDialogAndDoesNotShowDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.apple.com")

                // Share an Apple Maps place link with the app
                shareUri("https://maps.apple.com/place?place-id=I5ECF0E5A2703FCD1&_provider=9902")

                // Deny parse HTML permission
                onDialog("geoShareParseHtmlPermissionDialog") {
                    toggleDoNotAsk()
                    dismiss()
                }

                // Error is visible
                onElement { viewIdResourceName == "geoShareConversionError" }

                // Close the window (this is necessary, for some reason)
                onElement { viewIdResourceName == "geoShareMainBackButton" }.click()

                // Share another Apple Maps place link with the app
                shareUri("https://maps.apple.com/place?place-id=I263680A7B546CF16&_provider=9902")

                // Error is visible
                onElement { viewIdResourceName == "geoShareConversionError" }
            }
        }

    @Test
    fun whenLinkWithPlaceOnlyIsSharedAndPermissionIsGrantedWithoutDoNotAsk_showsPointAndShowsDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps place link with the app
                shareUri("https://www.google.com/maps/place/Hermannstr.+10,+Berlin/")

                // Grant parse HTML permission
                onDialog("geoShareParseHtmlPermissionDialog") {
                    confirm()
                }

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

                // Parse HTML permission dialog is visible again
                onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
            }
        }

    @Test
    fun whenLinkWithPlaceOnlyIsSharedAndPermissionIsGrantedWithDoNotAsk_showsPointAndDoesNotShowDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps place link with the app
                shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")

                // Grant parse HTML permission and check "Don't ask me again"
                onDialog("geoShareParseHtmlPermissionDialog") {
                    toggleDoNotAsk()
                    confirm()
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
        }

    @Test
    fun whenLinkWithPlaceOnlyIsSharedAndPermissionIsDeniedWithoutDoNotAsk_closesDialogAndShowsDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps place link with the app
                shareUri("https://www.google.com/maps/place/Hermannstr.+30,+Berlin/")

                // Deny parse HTML permission
                onDialog("geoShareParseHtmlPermissionDialog") {
                    dismiss()
                }

                // Shows location search
                assertConversionSucceeded(
                    GCJ02Point(
                        name = @Suppress("SpellCheckingInspection") "Hermannstr. 30, Berlin",
                        source = Source.URI,
                    )
                )

                // Share another Google Maps place link with the app
                shareUri("https://www.google.com/maps/place/Hermannstr.+31,+Berlin/")

                // Parse HTML permission dialog is visible again
                onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
            }
        }

    @Test
    fun whenLinkWithPlaceOnlyIsSharedAndPermissionIsDeniedWithDoNotAsk_closesDialogAndDoesNotShowDialogSecondTime() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps place link with the app
                shareUri("https://www.google.com/maps/place/Hermannstr.+40,+Berlin/")

                // Deny parse HTML permission
                onDialog("geoShareParseHtmlPermissionDialog") {
                    toggleDoNotAsk()
                    dismiss()
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

    @Test
    fun whenShortUriWithCoordinatesInHtmlIsSharedAndPermissionIsGranted_doesNotAskForPermission() =
        uiAutomator {
            runBlocking {
                assumeDomainResolvable("maps.google.com")

                // Share a Google Maps short link with the app
                shareUri("https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8")

                // Grant unshorten permission
                onDialog("geoShareUnshortenPermissionDialog") {
                    confirm()
                }

                // Shows precise location
                assertConversionSucceeded(
                    GCJ02Point(
                        51.1982447, 6.4389493,
                        name = @Suppress("SpellCheckingInspection") "Heinemann, Bismarckstraße 91, 41061 Mönchengladbach",
                        source = Source.URI,
                    )
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun allowsOpeningTomTom() = uiAutomator {
        runBlocking {
            assumeAppInstalled(TOMTOM_PACKAGE_NAME)
            assumeDomainResolvable("tomtom.com")

            // Share a geo: URI with the app
            shareUri("geo:52.47254,13.4345")

            // Tap the TomTom icon
            onMainScrollablePane()
                // Scroll by percents, because it's more reliable than scrolling to the app icon
                .scroll(Direction.DOWN, 2f)
            onElement { viewIdResourceName == "geoShareApp_${TOMTOM_PACKAGE_NAME}" }.click()

            // Dismiss the location rationale dialog
            onDialog("geoShareLocationRationaleDialog", timeoutMs = 20_000L) {
                dismiss()
            }

            // Tap the TomTom icon again
            onElement { viewIdResourceName == "geoShareApp_${TOMTOM_PACKAGE_NAME}" }.click()

            // Confirm location rationale
            onDialog("geoShareLocationRationaleDialog", timeoutMs = 20_000L) {
                confirm()
            }

            // Deny location permission
            denyLocationPermission()

            // Tap the TomTom icon again
            onElement { viewIdResourceName == "geoShareApp_${TOMTOM_PACKAGE_NAME}" }.click()

            // Confirm location rationale
            onDialog("geoShareLocationRationaleDialog", timeoutMs = 20_000L) {
                confirm()
            }

            // Grant location permission
            waitForStableInActiveWindow() // Wait, otherwise tapping the location permission grant button does nothing
            grantLocationPermission()

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
    fun allowsSavingGpx() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Wait for the conversion to succeed
        assertConversionSucceeded(
            persistentListOf(
                WGS84Point(
                    52.5067296, 13.2599309,
                    11.0,
                    source = Source.MAP_CENTER,
                )
            )
        )

        // Open copy menu
        onElement { viewIdResourceName == "geoShareResultSuccessLastPointMenu" }.click()

        onElement { viewIdResourceName == "geoShareResultSuccessSheet" }.apply {
            // Expand the sheet
            swipe(Direction.UP, 1f)

            // Tap "Save GPX"
            onElement { viewIdResourceName == "geoShareResultSuccessSheet" }
                .scroll(Direction.DOWN, 10f)
            onElement { viewIdResourceName == "geoShareResultSuccessSheet" }
                // Scroll again, because only now can the lazy column pane scroll all the way to the bottom
                .scrollToElement(Direction.DOWN) {
                    viewIdResourceName == "geoShareResultSuccessSheetItemHeadline" &&
                        textAsString() in
                        setOf("Save GPX route", @Suppress("SpellCheckingInspection") "Enregistrer l’itinéraire GPX")
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
}

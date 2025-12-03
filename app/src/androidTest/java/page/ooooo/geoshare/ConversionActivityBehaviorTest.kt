package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.outputs.GpxOutput
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

@RunWith(AndroidJUnit4::class)
class ConversionActivityBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun conversionScreen_whenFullUriIsShared_showsPositionAndAllowsOpeningGoogleMaps() = uiAutomator {
        assertAppInstalled(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME)

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows precise location
        waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.5067296, 13.2599309, z = 11.0))

        // Tap the Google Maps icon
        onElement { viewIdResourceName == "geoShareResultCardApp_${AndroidTools.GOOGLE_MAPS_PACKAGE_NAME}" }.click()

        // Google Maps shows precise location
        waitAndAssertGoogleMapsContainsElement {
            textAsString() in listOf("Westend", "Berlin-Westend")
        }
    }

    @Test
    fun conversionScreen_whenLinkWithCoordinatesInChinaIsShared_showsPositionAndAllowsOpeningGoogleMapsInGCJ02() =
        uiAutomator {
            assertAppInstalled(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME)

            // Share a Google Maps coordinates link with the app
            shareUri("https://www.google.com/maps/@31.22850685422705,121.47552456472106,11z")

            // Shows precise location in WGS 84
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 31.23044166868017, 121.47099209401793, z = 11.0))

            // Shows copy link in GCJ-02
            onElement { viewIdResourceName == "geoShareConversionSuccessPositionMenuButton" }.click()
            onElement {
                viewIdResourceName == "geoShareConversionSuccessSheetItemDescription" && textAsString()?.startsWith("https://www.google.com/maps?q=31.2285069,121.4") == true
            }
            pressBack()

            // Tap the Google Maps icon
            onElement { viewIdResourceName == "geoShareResultCardApp_${AndroidTools.GOOGLE_MAPS_PACKAGE_NAME}" }.click()

            // Google Maps shows precise location
            waitAndAssertGoogleMapsContainsElement { textAsString() == "Ming&Qing Dynasties Furniture Hall" }
        }

    @Test
    fun conversionScreen_whenFullUriIsSharedAndAutomationIsConfiguredToCopyCoordsDec_copiesCoords() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomationCopyCoordsDec" }.click()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows automation success message
        onElement(pollIntervalMs = 50L) { viewIdResourceName == "geoShareConversionSuccessAutomationSuccess" }

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareConversionSuccessAutomationPreferencesButton" }
    }

    @Test
    fun conversionScreen_whenFullUriIsSharedAndAutomationIsConfiguredToOpenAnInstalledApp_opensApp() = uiAutomator {
        assertAppInstalled(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME)

        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomationOpenApp_${AndroidTools.GOOGLE_MAPS_PACKAGE_NAME}" }.click()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareConversionSuccessAutomationCounter" }

        // Google Maps doesn't open while the counter is running
        assertNull(onElementOrNull(3_000L) { packageName == AndroidTools.GOOGLE_MAPS_PACKAGE_NAME })

        // Google Maps opens
        onElement { packageName == AndroidTools.GOOGLE_MAPS_PACKAGE_NAME }

        // Go back to Geo Share
        launchApplication()

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareConversionSuccessAutomationPreferencesButton" }
    }

    @Test
    fun conversionScreen_whenShortUriIsSharedAndUnshortenPermissionDialogIsConfirmedWithoutDoNotAsk_showsPositionAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

            // Grant unshorten permission
            onDialog("geoShareUnshortenPermissionDialog") {
                confirm()
            }

            // Shows precise location
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.4842015, 13.4167277))

            // Share another Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
            waitForStableInActiveWindow()

            // Unshorten permission dialog is visible again
            onElement { viewIdResourceName == "geoShareUnshortenPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenShortUriIsSharedAndUnshortenPermissionIsConfirmedWithDoNotAsk_showsPositionAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

            // Grant unshorten permission and check "Don't ask me again"
            onDialog("geoShareUnshortenPermissionDialog") {
                toggleDoNotAsk()
                confirm()
            }

            // Shows precise location
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.4842015, 13.4167277))

            // Share another Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
            waitForStableInActiveWindow()

            // Shows precise location again
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 44.4490541, 26.0888398))
        }

    @Test
    fun conversionScreen_whenShortUriIsSharedAndUnshortenPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

            // Deny unshorten permission
            onDialog("geoShareUnshortenPermissionDialog") {
                dismiss()
            }

            // Error is visible
            onElement { viewIdResourceName == "geoShareConversionError" }

            // Close the window (this is necessary, for some reason)
            onElement { viewIdResourceName == "geoShareConversionBackButton" }.click()

            // Share the Google Maps short link with the app again
            shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

            // Unshorten permission dialog is visible again
            onElement { viewIdResourceName == "geoShareUnshortenPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenShortUriIsSharedAndUnshortenPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
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
            onElement { viewIdResourceName == "geoShareConversionBackButton" }.click()

            // Share the Google Maps short link with the app again
            shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

            // Error is visible again
            onElement { viewIdResourceName == "geoShareConversionError" }
        }

    @Test
    fun conversionScreen_whenNonexistentShortUriIsSharedAndUnshortenPermissionIsDismissed_closesTheDialogAndDoesNothing() =
        uiAutomator {
            // Share a Google Maps short non-existent link with the app
            shareUri("https://maps.app.goo.gl/spam")

            // Grant unshorten permission
            onDialog("geoShareUnshortenPermissionDialog") {
                confirm()
            }

            // Error is visible
            onElement { viewIdResourceName == "geoShareConversionError" }
        }

    @Test
    fun conversionScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionDialogIsConfirmedWithoutDoNotAsk_opensAppleMapsAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I7BA098CC17989C16&_provider=9902")

            // Grant parse HTML permission
            onDialog("geoShareParseHtmlPermissionDialog") {
                confirm()
            }

            // Shows precise location
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.4697882, 13.4257989))

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I849C144AAC7A794F&_provider=9902")
            waitForStableInActiveWindow()

            // Parse HTML permission dialog is visible again
            onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionIsConfirmedWithDoNotAsk_opensAppleMapsAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I1CBDEBCF5A275CB2&_provider=9902")

            // Grant parse HTML permission and check "Don't ask me again"
            onDialog("geoShareParseHtmlPermissionDialog") {
                toggleDoNotAsk()
                confirm()
            }

            // Shows precise location
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.4778665, 13.426398))

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I6E0F00362159B5EC&_provider=9902")
            waitForStableInActiveWindow()

            // Shows precise location again
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.4820815, 13.4338421))
        }

    @Test
    fun conversionScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I8D204FAB527CE0EB&_provider=9902")

            // Deny parse HTML permission
            onDialog("geoShareParseHtmlPermissionDialog") {
                dismiss()
            }

            // Error is visible
            onElement { viewIdResourceName == "geoShareConversionError" }

            // Close the window (this is necessary, for some reason)
            onElement { viewIdResourceName == "geoShareConversionBackButton" }.click()

            // Share the Apple Maps place link with the app again
            shareUri("https://maps.apple.com/place?place-id=IE08A4F5FAA2F0502&_provider=9902")

            // Parse HTML permission dialog is visible again
            onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
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
            onElement { viewIdResourceName == "geoShareConversionBackButton" }.click()

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I263680A7B546CF16&_provider=9902")

            // Error is visible
            onElement { viewIdResourceName == "geoShareConversionError" }
        }

    @Test
    fun conversionScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionDialogIsConfirmedWithoutDoNotAsk_showsPositionAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+10,+Berlin/")

            // Grant parse HTML permission
            onDialog("geoShareParseHtmlPermissionDialog") {
                confirm()
            }

            // Shows precise location
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.4848232, 13.4240791))

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+11,+Berlin/")
            waitForStableInActiveWindow()

            // Parse HTML permission dialog is visible again
            onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionIsConfirmedWithDoNotAsk_showsPositionAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")

            // Grant parse HTML permission and check "Don't ask me again"
            onDialog("geoShareParseHtmlPermissionDialog") {
                toggleDoNotAsk()
                confirm()
            }

            // Shows precise location
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.4834254, 13.4245399))

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+21,+Berlin/")
            waitForStableInActiveWindow()

            // Shows precise location again
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 52.4832988, 13.4245179))
        }

    @Test
    fun conversionScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+30,+Berlin/")

            // Deny parse HTML permission
            onDialog("geoShareParseHtmlPermissionDialog") {
                dismiss()
            }

            // Shows location search
            @Suppress("SpellCheckingInspection") waitAndAssertPositionIsVisible(Position(q = "Hermannstr. 30, Berlin"))

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+31,+Berlin/")

            // Parse HTML permission dialog is visible again
            onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+40,+Berlin/")

            // Deny parse HTML permission
            onDialog("geoShareParseHtmlPermissionDialog") {
                toggleDoNotAsk()
                dismiss()
            }

            // Shows location search
            @Suppress("SpellCheckingInspection") waitAndAssertPositionIsVisible(Position(q = "Hermannstr. 40, Berlin"))

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+41,+Berlin/")

            // Shows location search
            @Suppress("SpellCheckingInspection") waitAndAssertPositionIsVisible(Position(q = "Hermannstr. 41, Berlin"))
        }

    @Test
    fun conversionScreen_whenShortUriWithCoordinatesInHtmlIsSharedAndUnshortenPermissionDialogIsConfirmed_doesNotAskForParseHtmlPermission() =
        uiAutomator {
            // Share a Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8")

            // Grant unshorten permission
            onDialog("geoShareUnshortenPermissionDialog") {
                confirm()
            }

            // Shows precise location
            waitAndAssertPositionIsVisible(Position(Srs.WGS84, 51.1982447, 6.4389493))
        }

    @Test
    fun conversionScreen_whenGpxRouteIsShared_allowsOpeningTomTom() = uiAutomator {
        assertAppInstalled(GpxOutput.TOMTOM_PACKAGE_NAME)

        // Share a geo: URI with the app
        shareUri("geo:52.47254,13.4345")

        // Tap the TomTom icon
        onElement { viewIdResourceName == "geoShareResultCardApp_${GpxOutput.TOMTOM_PACKAGE_NAME}" }.click()

        // Dismiss the location rationale dialog
        onDialog("geoShareLocationRationaleDialog") {
            dismiss()
        }

        // Tap the TomTom icon again
        onElement { viewIdResourceName == "geoShareResultCardApp_${GpxOutput.TOMTOM_PACKAGE_NAME}" }.click()

        // Confirm location rationale
        onDialog("geoShareLocationRationaleDialog") {
            confirm()
        }

        // Deny location permission
        onElement {
            textAsString() in listOf(
                "Don't allow",
                @Suppress("SpellCheckingInspection") "Ne pas autoriser",
            )
        }.click()

        // Tap the TomTom icon again
        onElement { viewIdResourceName == "geoShareResultCardApp_${GpxOutput.TOMTOM_PACKAGE_NAME}" }.click()

        // Confirm location rationale
        onDialog("geoShareLocationRationaleDialog") {
            confirm()
        }

        // Grant location permission
        onElement {
            textAsString() in listOf(
                "Only this time",
                @Suppress("SpellCheckingInspection") "Uniquement cette fois-ci",
            )
        }.click()

        // TomTom starts navigation
        waitAndAssertTomTomContainsElement { textAsString() in listOf("Drive", "Aller") }
    }

    @Test
    fun conversionScreen_whenGpxRouteIsSharedAndAutomationIsConfigured_opensTomTom() = uiAutomator {
        assertAppInstalled(GpxOutput.TOMTOM_PACKAGE_NAME)

        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomationShareGpxWithApp" }.click()

        // Share a geo: URI with the app
        shareUri("geo:52.47254,13.4345")

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareConversionSuccessAutomationCounter" }

        // Confirm location rationale
        onDialog("geoShareLocationRationaleDialog", timeoutMs = 20_000L) {
            confirm()
        }

        // Grant location permission
        onElement {
            textAsString() in listOf(
                "Only this time",
                @Suppress("SpellCheckingInspection") "Uniquement cette fois-ci",
            )
        }.click()

        // TomTom starts navigation
        waitAndAssertTomTomContainsElement { textAsString() in listOf("Drive", "Aller") }
    }
}

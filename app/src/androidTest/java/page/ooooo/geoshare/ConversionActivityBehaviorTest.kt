package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.lib.Position

@RunWith(AndroidJUnit4::class)
class ConversionActivityBehaviorTest : BaseActivityBehaviorTest() {
    companion object {
        const val GOOGLE_MAPS_PACKAGE_NAME = "com.google.android.apps.maps"
    }

    private fun waitAndConfirmDialogAndAssertNewWindowIsOpen(
        doNotAsk: Boolean = false,
        resourceName: String,
    ) = uiAutomator {
        onElement { viewIdResourceName == resourceName }
        toggleDialogDoNotAsk(doNotAsk)
        onElement { viewIdResourceName == "geoShareConfirmationDialogConfirmButton" }
            .clickAndWait(Until.newWindow(), TIMEOUT)
    }

    private fun waitAndDismissDialogAndAssertItIsClosed(
        doNotAsk: Boolean = false,
        resourceName: String,
    ) = uiAutomator {
        onElement { viewIdResourceName == resourceName }
        toggleDialogDoNotAsk(doNotAsk)
        onElement { viewIdResourceName == "geoShareConfirmationDialogDismissButton" }.click()
        device.wait(Until.gone(By.res(resourceName)), TIMEOUT)
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == resourceName })
    }

    private fun toggleDialogDoNotAsk(doNotAsk: Boolean) = uiAutomator {
        if (doNotAsk) {
            onElement { viewIdResourceName == "geoShareConfirmationDialogDoNotAskSwitch" }.click()
        }
    }

    @Test
    fun conversionScreen_whenFullUriIsShared_showsPositionAndAllowsOpeningGoogleMaps() = uiAutomator {
        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("52.5067296", "13.2599309", z = "11"))

        // Open the coordinates with Google Maps
        onElement { viewIdResourceName == "geoShareResultCardApp_$GOOGLE_MAPS_PACKAGE_NAME" }.click()

        // Wait for Google Maps
        onElement { packageName == GOOGLE_MAPS_PACKAGE_NAME }

        // If there is a Google Maps sign in screen, skip it
        onElementOrNull(3_000L) { packageName == GOOGLE_MAPS_PACKAGE_NAME && textAsString() == "Make it your map" }?.also {
            onElement { packageName == GOOGLE_MAPS_PACKAGE_NAME && textAsString()?.lowercase() == "skip" }.click()
        }

        // Verify Google Maps content
        onElement { packageName == GOOGLE_MAPS_PACKAGE_NAME && textAsString() == "Westend" }
    }

    @Test
    fun conversionScreen_whenFullUriIsSharedAndAutomationIsConfiguredToCopyCoordsDec_showsPositionAndCopiesCoords() =
        uiAutomator {
            // Launch application and close intro
            launchApplication()
            pressBack()

            // Configure automation
            goToUserPreferencesDetailAutomationScreen()
            onElement { viewIdResourceName == "geoShareUserPreferenceAutomationCopyCoordsDec" }.click()

            // Share a Google Maps coordinates link with the app
            shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("52.5067296", "13.2599309", z = "11"))

            // Shows automation success message
            onElement { viewIdResourceName == "geoShareConversionSuccessAutomationSuccess" }

            // Shows automation preferences button
            onElement { viewIdResourceName == "geoShareConversionSuccessAutomationPreferencesButton" }
        }

    @Test
    fun conversionScreen_whenFullUriIsSharedAndAutomationIsConfiguredToOpenAnInstalledApp_showsPositionAndOpensTheInstalledAppAutomatically() =
        uiAutomator {
            // Launch application and close intro
            launchApplication()
            pressBack()

            // Configure automation
            goToUserPreferencesDetailAutomationScreen()
            onElement { viewIdResourceName == "geoShareUserPreferenceAutomationOpenApp_$GOOGLE_MAPS_PACKAGE_NAME" }.click()

            // Share a Google Maps coordinates link with the app
            shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("52.5067296", "13.2599309", z = "11"))

            // Shows automation counter
            onElement { viewIdResourceName == "geoShareConversionSuccessAutomationCounter" }

            // Google Maps doesn't open while the counter is running
            assertNull(onElementOrNull(4_000L) { packageName == GOOGLE_MAPS_PACKAGE_NAME })

            // Google Maps opens
            onElement { packageName == GOOGLE_MAPS_PACKAGE_NAME }

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
            waitAndConfirmDialogAndAssertNewWindowIsOpen(doNotAsk = false, "geoShareUnshortenPermissionDialog")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("52.4842015", "13.4167277"))

            // Return to the home screen
            pressHome()

            // Share another Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")

            // Unshorten permission dialog is visible again
            onElement { viewIdResourceName == "geoShareUnshortenPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenShortUriIsSharedAndUnshortenPermissionIsConfirmedWithDoNotAsk_showsPositionAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

            // Grant unshorten permission and check "Don't ask me again"
            waitAndConfirmDialogAndAssertNewWindowIsOpen(doNotAsk = true, "geoShareUnshortenPermissionDialog")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("52.4842015", "13.4167277"))

            // Return to the home screen
            pressHome()

            // Share another Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")

            // Shows precise location again
            waitAndAssertPositionIsVisible(Position("44.4490541", "26.0888398"))
        }

    @Test
    fun conversionScreen_whenShortUriIsSharedAndUnshortenPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

            // Deny unshorten permission
            waitAndDismissDialogAndAssertItIsClosed(doNotAsk = false, "geoShareUnshortenPermissionDialog")

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
            waitAndDismissDialogAndAssertItIsClosed(doNotAsk = true, "geoShareUnshortenPermissionDialog")

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
            waitAndConfirmDialogAndAssertNewWindowIsOpen(doNotAsk = false, "geoShareUnshortenPermissionDialog")

            // Error is visible
            onElement { viewIdResourceName == "geoShareConversionError" }
        }

    @Test
    fun conversionScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionDialogIsConfirmedWithoutDoNotAsk_opensAppleMapsAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I7BA098CC17989C16&_provider=9902")

            // Grant parse HTML permission
            waitAndConfirmDialogAndAssertNewWindowIsOpen(doNotAsk = false, "geoShareParseHtmlPermissionDialog")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("52.4697882", "13.4257989"))

            // Return to the home screen
            pressHome()

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I849C144AAC7A794F&_provider=9902")

            // Parse HTML permission dialog is visible again
            onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionIsConfirmedWithDoNotAsk_opensAppleMapsAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I1CBDEBCF5A275CB2&_provider=9902")

            // Grant parse HTML permission and check "Don't ask me again"
            waitAndConfirmDialogAndAssertNewWindowIsOpen(doNotAsk = true, "geoShareParseHtmlPermissionDialog")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("52.4778665", "13.426398"))

            // Return to the home screen
            pressHome()

            // Share another Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I6E0F00362159B5EC&_provider=9902")

            // Shows precise location again
            waitAndAssertPositionIsVisible(Position("52.4820815", "13.4338421"))
        }

    @Test
    fun conversionScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share an Apple Maps place link with the app
            shareUri("https://maps.apple.com/place?place-id=I8D204FAB527CE0EB&_provider=9902")

            // Deny parse HTML permission
            waitAndDismissDialogAndAssertItIsClosed(doNotAsk = false, "geoShareParseHtmlPermissionDialog")

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
            waitAndDismissDialogAndAssertItIsClosed(doNotAsk = true, "geoShareParseHtmlPermissionDialog")

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
            waitAndConfirmDialogAndAssertNewWindowIsOpen(doNotAsk = false, "geoShareParseHtmlPermissionDialog")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("52.4848232", "13.4240791"))

            // Return to the home screen
            pressHome()

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+11,+Berlin/")

            // Parse HTML permission dialog is visible again
            onElement { viewIdResourceName == "geoShareParseHtmlPermissionDialog" }
        }

    @Test
    fun conversionScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionIsConfirmedWithDoNotAsk_showsPositionAndDoesNotShowTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")

            // Grant parse HTML permission and check "Don't ask me again"
            waitAndConfirmDialogAndAssertNewWindowIsOpen(doNotAsk = true, "geoShareParseHtmlPermissionDialog")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("52.4834254", "13.4245399"))

            // Return to the home screen
            pressHome()

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+21,+Berlin/")

            // Shows precise location again
            waitAndAssertPositionIsVisible(Position("52.4832988", "13.4245179"))
        }

    @Test
    fun conversionScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() =
        uiAutomator {
            // Share a Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+30,+Berlin/")

            // Deny parse HTML permission
            waitAndDismissDialogAndAssertItIsClosed(doNotAsk = false, "geoShareParseHtmlPermissionDialog")

            // Shows location search
            @Suppress("SpellCheckingInspection")
            waitAndAssertPositionIsVisible(Position(q = "Hermannstr. 30, Berlin"))

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
            waitAndDismissDialogAndAssertItIsClosed(doNotAsk = true, "geoShareParseHtmlPermissionDialog")

            // Shows location search
            @Suppress("SpellCheckingInspection")
            waitAndAssertPositionIsVisible(Position(q = "Hermannstr. 40, Berlin"))

            // Share another Google Maps place link with the app
            shareUri("https://www.google.com/maps/place/Hermannstr.+41,+Berlin/")

            // Shows location search
            @Suppress("SpellCheckingInspection")
            waitAndAssertPositionIsVisible(Position(q = "Hermannstr. 41, Berlin"))
        }

    @Test
    fun conversionScreen_whenShortUriWithCoordinatesInHtmlIsSharedAndUnshortenPermissionDialogIsConfirmed_doesNotAskForParseHtmlPermission() =
        uiAutomator {
            // Share a Google Maps short link with the app
            shareUri("https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8")

            // Grant unshorten permission
            waitAndConfirmDialogAndAssertNewWindowIsOpen(doNotAsk = false, "geoShareUnshortenPermissionDialog")

            // Shows precise location
            waitAndAssertPositionIsVisible(Position("51.1982447", "6.4389493"))
        }
}

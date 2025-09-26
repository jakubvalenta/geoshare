package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.lib.Position

@RunWith(AndroidJUnit4::class)
class ConversionActivityBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun shareScreen_whenFullUriIsShared_showsPositionAndOpensGoogleMaps() {
        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("52.5067296", "13.2599309", z = "11"))

        // Google Maps shows precise location
        clickMapApp(googleMapsPackageName)
        assertGoogleMapsHasText("Westend".toPattern())
    }

    @Test
    fun shareScreen_whenShortUriIsSharedAndUnshortenPermissionDialogIsConfirmedWithoutDoNotAsk_showsPositionAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Grant unshorten permission
        val unshortenPermissionDialogSelector = By.res("geoShareUnshortenPermissionDialog")
        waitAndConfirmDialogAndAssertNewWindowIsOpen(unshortenPermissionDialogSelector)

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("52.4842015", "13.4167277"))

        // Return to the home screen
        device.pressHome()

        // Share another Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")

        // Unshorten permission dialog is visible again
        waitAndAssertObjectExists(unshortenPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenShortUriIsSharedAndUnshortenPermissionIsConfirmedWithDoNotAsk_showsPositionAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Grant unshorten permission and check "Don't ask me again"
        waitAndConfirmDialogAndAssertNewWindowIsOpen(By.res("geoShareUnshortenPermissionDialog"), doNotAsk = true)

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("52.4842015", "13.4167277"))

        // Return to the home screen
        device.pressHome()

        // Share another Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")

        // Shows precise location again
        waitAndAssertPositionIsVisible(Position("44.4490541", "26.0888398"))
    }

    @Test
    fun shareScreen_whenShortUriIsSharedAndUnshortenPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Deny unshorten permission
        val unshortenPermissionDialogSelector = By.res("geoShareUnshortenPermissionDialog")
        waitAndDismissDialogAndAssertItIsClosed(unshortenPermissionDialogSelector)

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))

        // Close the window (this is necessary, for some reason)
        clickObject(By.res("geoShareConversionDoneButton"))

        // Share the Google Maps short link with the app again
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Unshorten permission dialog is visible again
        waitAndAssertObjectExists(unshortenPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenShortUriIsSharedAndUnshortenPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Deny unshorten permission
        waitAndDismissDialogAndAssertItIsClosed(By.res("geoShareUnshortenPermissionDialog"), doNotAsk = true)

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))

        // Close the window (this is necessary, for some reason)
        clickObject(By.res("geoShareConversionDoneButton"))

        // Share the Google Maps short link with the app again
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Error is visible again
        waitAndAssertObjectExists(By.res("geoShareConversionError"))
    }

    @Test
    fun shareScreen_whenNonexistentShortUriIsSharedAndUnshortenPermissionIsDismissed_closesTheDialogAndDoesNothing() {
        // Share a Google Maps short non-existent link with the app
        shareUri("https://maps.app.goo.gl/spam")

        // Grant unshorten permission
        waitAndConfirmDialogAndAssertNewWindowIsOpen(By.res("geoShareUnshortenPermissionDialog"))

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))
    }

    @Test
    fun shareScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionDialogIsConfirmedWithoutDoNotAsk_opensAppleMapsAndShowsTheDialogTheSecondTime() {
        // Share an Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I7BA098CC17989C16&_provider=9902")

        // Grant parse HTML permission
        val parseHtmlPermissionDialogSelector = By.res("geoShareParseHtmlPermissionDialog")
        waitAndConfirmDialogAndAssertNewWindowIsOpen(parseHtmlPermissionDialogSelector)

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("52.4697882", "13.4257989"))

        // Return to the home screen
        device.pressHome()

        // Share another Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I849C144AAC7A794F&_provider=9902")

        // Parse HTML permission dialog is visible again
        waitAndAssertObjectExists(parseHtmlPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionIsConfirmedWithDoNotAsk_opensAppleMapsAndDoesNotShowTheDialogTheSecondTime() {
        // Share an Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I1CBDEBCF5A275CB2&_provider=9902")

        // Grant parse HTML permission and check "Don't ask me again"
        val parseHtmlPermissionDialogSelector = By.res("geoShareParseHtmlPermissionDialog")
        waitAndConfirmDialogAndAssertNewWindowIsOpen(parseHtmlPermissionDialogSelector, doNotAsk = true)

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("52.4778665", "13.426398"))

        // Return to the home screen
        device.pressHome()

        // Share another Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I6E0F00362159B5EC&_provider=9902")

        // Shows precise location again
        waitAndAssertPositionIsVisible(Position("52.4820815", "13.4338421"))
    }

    @Test
    fun shareScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() {
        // Share an Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I8D204FAB527CE0EB&_provider=9902")

        // Deny parse HTML permission
        val parseHtmlPermissionDialogSelector = By.res("geoShareParseHtmlPermissionDialog")
        waitAndDismissDialogAndAssertItIsClosed(parseHtmlPermissionDialogSelector)

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))

        // Close the window (this is necessary, for some reason)
        clickObject(By.res("geoShareConversionDoneButton"))

        // Share the Apple Maps place link with the app again
        shareUri("https://maps.apple.com/place?place-id=IE08A4F5FAA2F0502&_provider=9902")

        // Parse HTML permission dialog is visible again
        waitAndAssertObjectExists(parseHtmlPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenLinkThatRequiresHtmlParsingIsSharedAndParseHtmlPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() {
        // Share an Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I5ECF0E5A2703FCD1&_provider=9902")

        // Deny parse HTML permission
        val parseHtmlPermissionDialogSelector = By.res("geoShareParseHtmlPermissionDialog")
        waitAndDismissDialogAndAssertItIsClosed(parseHtmlPermissionDialogSelector, doNotAsk = true)

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))

        // Close the window (this is necessary, for some reason)
        clickObject(By.res("geoShareConversionDoneButton"))

        // Share another Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I263680A7B546CF16&_provider=9902")

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))
    }

    @Test
    fun shareScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionDialogIsConfirmedWithoutDoNotAsk_showsPositionAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+10,+Berlin/")

        // Grant parse HTML permission
        val parseHtmlPermissionDialogSelector = By.res("geoShareParseHtmlPermissionDialog")
        waitAndConfirmDialogAndAssertNewWindowIsOpen(parseHtmlPermissionDialogSelector)

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("52.4848232", "13.4240791"))

        // Return to the home screen
        device.pressHome()

        // Share another Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+11,+Berlin/")

        // Parse HTML permission dialog is visible again
        waitAndAssertObjectExists(parseHtmlPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionIsConfirmedWithDoNotAsk_showsPositionAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")

        // Grant parse HTML permission and check "Don't ask me again"
        waitAndConfirmDialogAndAssertNewWindowIsOpen(
            By.res("geoShareParseHtmlPermissionDialog"),
            doNotAsk = true,
        )

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("52.4834254", "13.4245399"))

        // Return to the home screen
        device.pressHome()

        // Share another Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+21,+Berlin/")

        // Shows precise location again
        waitAndAssertPositionIsVisible(Position("52.4832988", "13.4245179"))
    }

    @Test
    fun shareScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+30,+Berlin/")

        // Deny parse HTML permission
        val parseHtmlPermissionDialogSelector = By.res("geoShareParseHtmlPermissionDialog")
        waitAndDismissDialogAndAssertItIsClosed(parseHtmlPermissionDialogSelector)

        // Shows location search
        @Suppress("SpellCheckingInspection")
        waitAndAssertPositionIsVisible(Position(q = "Hermannstr. 30, Berlin"))

        // Share another Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+31,+Berlin/")

        // Parse HTML permission dialog is visible again
        waitAndAssertObjectExists(parseHtmlPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenLinkWithPlaceOnlyIsSharedAndParseHtmlPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+40,+Berlin/")

        // Deny parse HTML permission
        waitAndDismissDialogAndAssertItIsClosed(By.res("geoShareParseHtmlPermissionDialog"), doNotAsk = true)

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
    fun shareScreen_whenShortUriWithCoordinatesInHtmlIsSharedAndUnshortenPermissionDialogIsConfirmed_doesNotAskForParseHtmlPermission() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8")

        // Grant unshorten permission
        waitAndConfirmDialogAndAssertNewWindowIsOpen(By.res("geoShareUnshortenPermissionDialog"))

        // Shows precise location
        waitAndAssertPositionIsVisible(Position("51.1982447", "6.4389493"))
    }
}

package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversionActivityBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun shareScreen_whenLongLinkIsShared_opensGoogleMaps() {
        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())
    }

    @Test
    fun shareScreen_whenShortLinkIsSharedAndUnshortenPermissionDialogIsConfirmedWithoutDoNotAsk_opensGoogleMapsAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Grant unshorten permission
        val unshortenPermissionDialogSelector = By.res("geoShareUnshortenPermissionDialog")
        waitAndConfirmDialogAndAssertNewWindowIsOpen(unshortenPermissionDialogSelector)

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share the Google Maps short link with the app again
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Unshorten permission dialog is visible again
        waitAndAssertObjectExists(unshortenPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenShortLinkIsSharedAndUnshortenPermissionIsConfirmedWithDoNotAsk_opensGoogleMapsAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Grant unshorten permission and check "Don't ask me again"
        waitAndConfirmDialogAndAssertNewWindowIsOpen(By.res("geoShareUnshortenPermissionDialog"), doNotAsk = true)

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share the Google Maps short link with the app again
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Google Maps shows precise location again
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs|52.484201,13.416727".toPattern())
    }

    @Test
    fun shareScreen_whenShortLinkIsSharedAndUnshortenPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Deny unshorten permission
        val unshortenPermissionDialogSelector = By.res("geoShareUnshortenPermissionDialog")
        waitAndDismissDialogAndAssertItIsClosed(unshortenPermissionDialogSelector)

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))

        // Close the window (this is necessary, for some reason)
        clickObject(By.res("geoShareConversionErrorDoneButton"))

        // Share the Google Maps short link with the app again
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Unshorten permission dialog is visible again
        waitAndAssertObjectExists(unshortenPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenShortLinkIsSharedAndUnshortenPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Deny unshorten permission
        waitAndDismissDialogAndAssertItIsClosed(By.res("geoShareUnshortenPermissionDialog"), doNotAsk = true)

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))

        // Close the window (this is necessary, for some reason)
        clickObject(By.res("geoShareConversionErrorDoneButton"))

        // Share the Google Maps short link with the app again
        shareUri("https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6")

        // Error is visible again
        waitAndAssertObjectExists(By.res("geoShareConversionError"))
    }

    @Test
    fun shareScreen_whenNonexistentShortLinkIsSharedAndUnshortenPermissionIsDismissed_closesTheDialogAndDoesNothing() {
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

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())

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

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share another Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I6E0F00362159B5EC&_provider=9902")

        // Google Maps shows precise location again
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())
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
        clickObject(By.res("geoShareConversionErrorDoneButton"))

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
        clickObject(By.res("geoShareConversionErrorDoneButton"))

        // Share another Apple Maps place link with the app
        shareUri("https://maps.apple.com/place?place-id=I263680A7B546CF16&_provider=9902")

        // Error is visible
        waitAndAssertObjectExists(By.res("geoShareConversionError"))
    }

    @Test
    fun shareScreen_whenLinkThatAllowsHtmlParsingToGetCoordsIsSharedAndParseHtmlPermissionDialogIsConfirmedWithoutDoNotAsk_opensGoogleMapsAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+10,+Berlin/")

        // Grant parse HTML permission
        val parseHtmlPermissionDialogSelector = By.res("geoShareParseHtmlToGetCoordsPermissionDialog")
        waitAndConfirmDialogAndAssertNewWindowIsOpen(parseHtmlPermissionDialogSelector)

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share another Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+11,+Berlin/")

        // Parse HTML permission dialog is visible again
        waitAndAssertObjectExists(parseHtmlPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenLinkThatAllowsHtmlParsingToGetCoordsIsSharedAndParseHtmlPermissionIsConfirmedWithDoNotAsk_opensGoogleMapsAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")

        // Grant parse HTML permission and check "Don't ask me again"
        waitAndConfirmDialogAndAssertNewWindowIsOpen(
            By.res("geoShareParseHtmlToGetCoordsPermissionDialog"),
            doNotAsk = true,
        )

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share another Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+21,+Berlin/")

        // Google Maps shows precise location again
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())
    }

    @Test
    fun shareScreen_whenLinkThatAllowsHtmlParsingToGetCoordsIsSharedAndParseHtmlPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+30,+Berlin/")

        // Deny parse HTML permission
        val parseHtmlPermissionDialogSelector = By.res("geoShareParseHtmlToGetCoordsPermissionDialog")
        waitAndDismissDialogAndAssertItIsClosed(parseHtmlPermissionDialogSelector)

        // Google Maps shows location search
        clickGoogleMapsAndAssertItHasText("""Hermannstr\. 30, Berlin""".toPattern())

        // Share another Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+31,+Berlin/")

        // Parse HTML permission dialog is visible again
        waitAndAssertObjectExists(parseHtmlPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenLinkThatAllowsHtmlParsingToGetCoordsIsSharedAndParseHtmlPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+40,+Berlin/")

        // Deny parse HTML permission
        waitAndDismissDialogAndAssertItIsClosed(By.res("geoShareParseHtmlToGetCoordsPermissionDialog"), doNotAsk = true)

        // Google Maps shows location search
        clickGoogleMapsAndAssertItHasText("""Hermannstr\. 40, Berlin""".toPattern())

        // Share another Google Maps place link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+41,+Berlin/")

        // Google Maps shows location search
        clickGoogleMapsAndAssertItHasText("""Hermannstr\. 41, Berlin""".toPattern())
    }

    @Test
    fun shareScreen_whenShortLinkWithCoordinatesInHtmlIsSharedAndUnshortenPermissionDialogIsConfirmed_doesNotAskForParseHtmlPermission() {
        // Share a Google Maps short link with the app
        shareUri("https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8")

        // Grant unshorten permission
        waitAndConfirmDialogAndAssertNewWindowIsOpen(By.res("geoShareUnshortenPermissionDialog"))

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())
    }

    @Test
    fun shareScreen_whenGoogleSearchLinkIsShared_downloadsFullHtmlFromGoogleThanksToCorrectUserAgent() {
        // Share a Google Search link with the app
        shareUri("https://www.google.com/search?sca_esv=14988c4722c11c49&hl=de&gl=de&output=search&kgmid=/g/11w7ktq4x8&q=ALDI&shndl=30&shem=uaasie&source=sh/x/loc/uni/m1/2&kgs=988b2ac8a0d6f02b")

        // Deny parse HTML permission
        waitAndDismissDialogAndAssertItIsClosed(By.res("geoShareParseHtmlToGetCoordsPermissionDialog"))

        // Google Maps shows precise location
        clickGoogleMapsAndAssertItHasText("Search here|Try gas stations, ATMs".toPattern())
    }

    private fun shareUri(unsafeUriString: String) {
        // Use shell command instead of startActivity() to support Xiaomi
        executeShellCommand(
            "am start -a android.intent.action.VIEW -d $unsafeUriString -n $packageName/page.ooooo.geoshare.ConversionActivity $packageName"
        )
    }
}

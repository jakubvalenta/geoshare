package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareActivityBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun shareScreen_whenLongLinkIsShared_opensGoogleMaps() {
        // Share a Google Maps link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Google Maps shows precise location
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())
    }

    @Test
    fun shareScreen_whenShortLinkIsSharedAndUnshortenPermissionDialogIsConfirmedWithoutDoNotAsk_opensGoogleMapsAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps link with the app
        shareUri("https://maps.app.goo.gl/eukZjpeYrrvX3tDw6")

        // Grant unshorten permission
        val unshortenPermissionDialogSelector =
            By.res("geoShareUnshortenPermissionDialog")
        waitAndConfirmDialogAndAssertNewWindowIsOpen(
            unshortenPermissionDialogSelector
        )

        // Google Maps shows precise location
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share a Google Maps link with the app again
        shareUri("https://maps.app.goo.gl/eukZjpeYrrvX3tDw6")

        // Unshorten permission dialog is visible again
        waitAndAssertObjectExists(unshortenPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenShortLinkIsSharedAndUnshortenPermissionIsConfirmedWithDoNotAsk_opensGoogleMapsAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps link with the app
        shareUri("https://maps.app.goo.gl/eukZjpeYrrvX3tDw6")

        // Grant unshorten permission and check "Don't ask me again"
        waitAndConfirmDialogAndAssertNewWindowIsOpen(
            By.res("geoShareUnshortenPermissionDialog"),
            doNotAsk = true
        )

        // Google Maps shows precise location
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share a Google Maps link with the app again
        shareUri("https://maps.app.goo.gl/eukZjpeYrrvX3tDw6")

        // Google Maps shows precise location again
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())
    }

    @Test
    fun shareScreen_whenShortLinkIsSharedAndUnshortenPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps link with the app
        shareUri("https://maps.app.goo.gl/eukZjpeYrrvX3tDw6")

        // Deny unshorten permission
        val unshortenPermissionDialogSelector =
            By.res("geoShareUnshortenPermissionDialog")
        waitAndDismissDialogAndAssertItIsClosed(
            unshortenPermissionDialogSelector
        )

        // Share a Google Maps link with the app again
        shareUri("https://maps.app.goo.gl/eukZjpeYrrvX3tDw6")

        // Unshorten permission dialog is visible again
        waitAndAssertObjectExists(unshortenPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenShortLinkIsSharedAndUnshortenPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps link with the app
        shareUri("https://maps.app.goo.gl/eukZjpeYrrvX3tDw6")

        // Deny unshorten permission
        waitAndDismissDialogAndAssertItIsClosed(
            By.res("geoShareUnshortenPermissionDialog"),
            doNotAsk = true
        )

        // Share a Google Maps link with the app again
        shareUri("https://maps.app.goo.gl/eukZjpeYrrvX3tDw6")

        // Google Maps does not open
        assertGoogleMapsDoesNotOpenWithinTimeout()
    }

    @Test
    fun shareScreen_whenNonexistentShortLinkIsSharedAndUnshortenPermissionIsDismissed_closesTheDialogAndDoesNothing() {
        // Share a Google Maps link with the app
        shareUri("https://maps.app.goo.gl/spam")

        // Grant unshorten permission
        waitAndConfirmDialogAndAssertNewWindowIsOpen(By.res("geoShareUnshortenPermissionDialog"))
    }

    @Test
    fun shareScreen_whenLinkWithCoordinatesInHtmlIsSharedAndParseHtmlPermissionDialogIsConfirmedWithoutDoNotAsk_opensGoogleMapsAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+10,+Berlin/")

        // Grant parse HTML permission
        val parseHtmlPermissionDialogSelector =
            By.res("geoShareParseHtmlToGetCoordsPermissionDialog")
        waitAndConfirmDialogAndAssertNewWindowIsOpen(
            parseHtmlPermissionDialogSelector
        )

        // Google Maps shows precise location
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share a Google Maps link with the app again
        shareUri("https://www.google.com/maps/place/Hermannstr.+11,+Berlin/")

        // Parse HTML permission dialog is visible again
        waitAndAssertObjectExists(parseHtmlPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenLinkWithCoordinatesInHtmlIsSharedAndParseHtmlPermissionIsConfirmedWithDoNotAsk_opensGoogleMapsAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")

        // Grant parse HTML permission and check "Don't ask me again"
        waitAndConfirmDialogAndAssertNewWindowIsOpen(
            By.res("geoShareParseHtmlToGetCoordsPermissionDialog"),
            doNotAsk = true
        )

        // Google Maps shows precise location
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())

        // Return to the home screen
        device.pressHome()

        // Share a Google Maps link with the app again
        shareUri("https://www.google.com/maps/place/Hermannstr.+21,+Berlin/")

        // Google Maps shows precise location again
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())
    }

    @Test
    fun shareScreen_whenLinkWithCoordinatesInHtmlIsSharedAndParseHtmlPermissionDialogIsDismissedWithoutDoNotAsk_closesTheDialogAndShowsTheDialogTheSecondTime() {
        // Share a Google Maps link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+30,+Berlin/")

        // Deny parse HTML permission
        val parseHtmlPermissionDialogSelector =
            By.res("geoShareParseHtmlToGetCoordsPermissionDialog")
        waitAndDismissDialogAndAssertItIsClosed(
            parseHtmlPermissionDialogSelector
        )

        // Google Maps shows location search
        waitAndAssertGoogleMapsTextExists("""Hermannstr\. 30, Berlin""".toPattern())

        // Share a Google Maps link with the app again
        shareUri("https://www.google.com/maps/place/Hermannstr.+31,+Berlin/")

        // Parse HTML permission dialog is visible again
        waitAndAssertObjectExists(parseHtmlPermissionDialogSelector)
    }

    @Test
    fun shareScreen_whenLinkWithCoordinatesInHtmlIsSharedAndParseHtmlPermissionIsDismissedWithDoNotAsk_closesTheDialogAndDoesNotShowTheDialogTheSecondTime() {
        // Share a Google Maps link with the app
        shareUri("https://www.google.com/maps/place/Hermannstr.+40,+Berlin/")

        // Deny parse HTML permission
        waitAndDismissDialogAndAssertItIsClosed(
            By.res("geoShareParseHtmlToGetCoordsPermissionDialog"),
            doNotAsk = true
        )

        // Google Maps shows location search
        waitAndAssertGoogleMapsTextExists("""Hermannstr\. 40, Berlin""".toPattern())

        // Share a Google Maps link with the app again
        shareUri("https://www.google.com/maps/place/Hermannstr.+41,+Berlin/")

        // Google Maps shows location search
        waitAndAssertGoogleMapsTextExists("""Hermannstr\. 41, Berlin""".toPattern())
    }

    @Test
    fun shareScreen_whenShortLinkWithCoordinatesInHtmlIsSharedAndUnshortenPermissionDialogIsConfirmed_doesNotAskForParseHtmlPermission() {
        // Share a Google Maps link with the app
        shareUri("https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8")

        // Grant unshorten permission
        waitAndConfirmDialogAndAssertNewWindowIsOpen(
            By.res("geoShareUnshortenPermissionDialog")
        )

        // Google Maps shows precise location
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())
    }

    @Test
    fun shareScreen_whenGoogleSearchLinkIsShared_downloadsFullHtmlFromGoogleThanksToCorrectUserAgent() {
        // Share a Google Maps link with the app
        shareUri("https://www.google.com/search?sca_esv=14988c4722c11c49&hl=de&gl=de&output=search&kgmid=/g/11w7ktq4x8&q=ALDI&shndl=30&shem=uaasie&source=sh/x/loc/uni/m1/2&kgs=988b2ac8a0d6f02b")

        // Deny parse HTML permission
        waitAndDismissDialogAndAssertItIsClosed(
            By.res("geoShareParseHtmlToGetCoordsPermissionDialog")
        )

        // Google Maps shows precise location
        waitAndAssertGoogleMapsTextExists("Search here|Try gas stations, ATMs".toPattern())
    }

    private fun shareUri(unsafeUriString: String) {
        // Use shell command instead of startActivity() to support Xiaomi
        executeShellCommand(
            "am start -a android.intent.action.VIEW -d $unsafeUriString " +
                    "-n $packageName/page.ooooo.geoshare.ShareActivity $packageName"
        )
    }
}

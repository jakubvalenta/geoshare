package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.testUriFails
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class GoogleSearchUriInputBehaviorTest : InputBehaviorTest {
    @Test
    fun googleSearchUriInput_offline() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Google Search short link
        testUriFails(
            setOf(
                "Google Search links are not supported",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "Les liens vers la recherche Google ne sont pas pris en charge",
            ),
            "https://share.google/diIxnYa8dIA6dZfpy",
        )

        // Google Search full URL
        testUriFails(
            setOf(
                "Google Search links are not supported",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "Les liens vers la recherche Google ne sont pas pris en charge",
            ),
            "https://www.google.com/search?q=Caf%C3%A9+Paari",
        )
    }
}

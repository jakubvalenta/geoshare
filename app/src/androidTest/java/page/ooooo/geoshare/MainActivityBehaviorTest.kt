package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
open class MainActivityBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun introScreen_whenAppIsOpenTwice_isVisibleOnlyFirstTime() {
        // Launch app
        launchApplication()

        // Go to the second intro page
        waitAndAssertObjectExists(By.res("geoShareIntroPage0HeadingText"))
        clickObject(By.res("geoShareIntroScreenNextButton"))

        // Relaunch app
        closeApplication()
        launchApplication()

        // Intro is still visible; go through all intro pages
        for (page in 0..1) {
            waitAndAssertObjectExists(By.res("geoShareIntroPage${page}HeadingText"))
            clickObject(By.res("geoShareIntroScreenNextButton"))
        }

        // Main screen is visible
        val uriStringSelector = By.res("geoShareMainInputUriStringTextField")
        waitAndAssertObjectExists(uriStringSelector)

        // Relaunch app
        closeApplication()
        launchApplication()

        // Main screen is visible again
        waitAndAssertObjectExists(uriStringSelector)
    }
}

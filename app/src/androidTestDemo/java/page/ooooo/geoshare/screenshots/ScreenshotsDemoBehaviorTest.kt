package page.ooooo.geoshare.screenshots

import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.Offer
import page.ooooo.geoshare.tests.assumeAppInstalled
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.closeIntro
import page.ooooo.geoshare.tests.disableSystemUIDemoMode
import page.ooooo.geoshare.tests.enableDarkMode
import page.ooooo.geoshare.tests.enableSystemUIDemoMode
import page.ooooo.geoshare.tests.goBackToMainForm
import page.ooooo.geoshare.tests.goToUserPreferencesDetail
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.quickWaitForStableInActiveWindow
import page.ooooo.geoshare.tests.saveScreenshot
import page.ooooo.geoshare.tests.setAppLocales
import page.ooooo.geoshare.tests.waitForAppToBeVisible
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import kotlin.time.Duration.Companion.seconds

class ScreenshotsDemoBehaviorTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() = uiAutomator {
            enableSystemUIDemoMode()
            enableDarkMode()
            setAppLocales("en-US")
        }

        @AfterClass
        @JvmStatic
        fun teardown() = uiAutomator {
            disableSystemUIDemoMode()
        }
    }

    /**
     * Take all screenshots in one big test method.
     *
     * See `ScreenshotsFreeBehaviorTest`.
     */
    @Test
    fun screenshots() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        quickWaitForStableInActiveWindow() // Wait for the intro to close

        // Test all screens in alphabetical order
        testBilling()
        testPreferences()
    }

    fun testBilling() = uiAutomator {
        // Billing - Not purchased
        onElement { viewIdResourceName == "geoShareMainBillingIcon" }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/billing_not_purchased")

        // Billing - Message - Pending
        onElement { viewIdResourceName == "geoShareBillingOffer_${Offer.Period.ONE_TIME}" }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" && isEnabled }.click()
        onElement { textAsString() == "Example error" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("pro_strings/billing_message_error")
        onElement { viewIdResourceName == "geoShareBillingStatusPending" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/billing_message_pending")

        // Billing - Purchased - One time
        onElement { viewIdResourceName == "geoShareBillingStatusPurchased" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/billing_purchased_one_time")
        onElement { viewIdResourceName == "geoShareBillingManageButtonOneTime" }.click() // Refund product

        // Billing - Purchased - Subscription
        onElement { viewIdResourceName == "geoShareBillingOffer_${Offer.Period.MONTHLY}" }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" && isEnabled }.click()
        onElement { textAsString() == "Example error" }
        runBlocking {
            delay(3.seconds) // Wait for the error message to disappear
        }
        onElement { viewIdResourceName == "geoShareBillingStatusPurchased" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/billing_purchased_subscription")

        // Billing - Message - Expiration
        onElement { viewIdResourceName == "geoShareBillingManageButtonSubscription" }.click() // Expire subscription
        onElement { viewIdResourceName == "geoShareBillingStatusExpired" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/billing_message_expiration")
        onElement { viewIdResourceName == "geoShareBillingManageButtonSubscription" }.click() // Cancel subscription

        goBackToMainForm()
    }

    fun testPreferences() = uiAutomator {
        // Preferences - Automation - Billing badge
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement { viewIdResourceName == "geoShareMainMenuUserPreferences" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_automation_billing_badge")

        // Preferences - Automation - Billing wall
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_automation_billing_wall")

        goBackToMainForm()
    }
}

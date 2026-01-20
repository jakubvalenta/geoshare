package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoBillingScreenBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun allowsPurchasingAndRefundingOneTimeProduct() {
        purchaseAndRefund(
            offerResourceName = "geoShareBillingOfferOneTime",
            manageButtonResourceName = "geoShareBillingManageButtonOneTime",
        )
    }

    @Test
    fun allowsPurchasingAndRefundingSubscription() {
        purchaseAndRefund(
            offerResourceName = "geoShareBillingOfferMonthly",
            manageButtonResourceName = "geoShareBillingManageButtonSubscription",
        )
    }

    private fun purchaseAndRefund(offerResourceName: String, manageButtonResourceName: String) = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Shows Geo Share headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "Geo Share" }

        // Go to automation preferences
        goToUserPreferencesDetailAutomationScreen()

        // Shows automation paywall
        onElement { viewIdResourceName == "geoShareFeatureBadgeLarge" }

        // Go to main screen
        pressBack()
        waitForStableInActiveWindow()
        pressBack()

        // Go to billing screen using billing icon
        onElement { viewIdResourceName == "geoShareFeatureBadgeSmall" }.click()

        // Purchase one time offer
        onElement { viewIdResourceName == offerResourceName }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" && isEnabled }.click()

        // Shows success message
        onElement { viewIdResourceName == "geoShareBillingMessageSuccess" }

        // Go to automation preferences
        pressBack()
        goToUserPreferencesDetailAutomationScreen()

        // Does not show automation paywall
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareFeatureBadgeLarge" })

        // Go to main screen
        pressBack()
        waitForStableInActiveWindow()
        pressBack()

        // Shows Geo Share Pro headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "Geo Share Pro" }

        // Does not show billing icon
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareFeatureBadgeSmall" })

        // Go to billing screen using main menu
        goToBillingScreen()

        // Refund product
        onElement { viewIdResourceName == manageButtonResourceName }.click()

        // Go to main screen
        pressBack()

        // Shows Geo Share headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "Geo Share" }

        // Go to automation preferences
        goToUserPreferencesDetailAutomationScreen()

        // Shows automation paywall
        onElement { viewIdResourceName == "geoShareFeatureBadgeLarge" }
    }
}

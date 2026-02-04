package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoBillingScreenBehaviorTest : BaseActivityBehaviorTest() {

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
        onElement { viewIdResourceName == "geoShareMainBillingIcon" }.click()

        // Purchase an offer
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
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareMainBillingIcon" })

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

    @Test
    fun allowsUsingAutomationAfterPurchase() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows billing icon
        onElement { viewIdResourceName == "geoShareResultAutomationBadge" }

        // Go to automation preferences using the button
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }.click()

        // Go to billing screen using the automation paywall
        onElement { viewIdResourceName == "geoShareFeatureBadgeLarge" }.click()

        // Purchase an offer
        onElement { viewIdResourceName == "geoShareBillingOfferOneTime" }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" && isEnabled }.click()

        // Go to automation preferences
        pressBack()

        // Configure automation
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomationCopyCoordsDec" }.click()

        // Go to the result screen
        pressBack()

        // Does not show billing icon
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareResultAutomationBadge" })

        // Go to the main screen
        pressBack()

        // Share the link again
        onElement { viewIdResourceName == "geoShareMainSubmitButton" }.click()

        // Shows automation success message
        onElement(pollIntervalMs = 50L) { viewIdResourceName == "geoShareResultSuccessMessage" }
    }

    private fun goToBillingScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuBilling" }
    }

    private fun goToUserPreferencesDetailAutomationScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_AUTOMATION" }.click()
    }
}

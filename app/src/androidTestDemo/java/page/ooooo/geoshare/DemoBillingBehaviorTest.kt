package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.serialization.json.Json
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.BehaviorTest.Companion.ELEMENT_DOES_NOT_EXIST_TIMEOUT
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.lib.billing.Offer
import page.ooooo.geoshare.ui.UserPreferencesGroupId

@RunWith(AndroidJUnit4::class)
class DemoBillingBehaviorTest : BehaviorTest {

    @Test
    fun allowsPurchasingAndRefundingOneTimeProduct() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Shows free headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare" }

        // Go to automation preferences
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)

        // Shows feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }

        // Go to main screen
        pressBack()
        quickWaitForStableInActiveWindow()
        pressBack()

        // Go to billing screen using feature badge
        onElement { viewIdResourceName == "geoShareMainBillingIcon" }.click()

        // Purchase an offer
        onElement { viewIdResourceName == "geoShareBillingOffer_${Offer.Period.ONE_TIME}" }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" && isEnabled }.click()

        // Shows status pending
        onElement { viewIdResourceName == "geoShareBillingStatusPending" }

        // Shows status purchased
        onElement { viewIdResourceName == "geoShareBillingStatusPurchased" }

        // Go to automation preferences
        pressBack()
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)

        // Does not show feature wall
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareAutomationFeatureWall" })

        // Go to main screen
        pressBack()
        quickWaitForStableInActiveWindow()
        pressBack()

        // Shows pro headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare Pro" }

        // Does not show feature badge
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareMainBillingIcon" })

        // Go to billing screen using main menu
        goToBillingScreen()

        // Refund product
        onElement { viewIdResourceName == "geoShareBillingManageButtonOneTime" }.click()

        // Go to main screen
        pressBack()

        // Shows free headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare" }

        // Go to automation preferences
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)

        // Shows feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }
    }

    @Test
    fun allowsPurchasingAndRefundingSubscription() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Shows free headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare" }

        // Go to automation preferences
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)

        // Shows feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }

        // Go to main screen
        pressBack()
        quickWaitForStableInActiveWindow()
        pressBack()

        // Go to billing screen using feature badge
        onElement { viewIdResourceName == "geoShareMainBillingIcon" }.click()

        // Purchase an offer
        onElement { viewIdResourceName == "geoShareBillingOffer_${Offer.Period.MONTHLY}" }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" && isEnabled }.click()

        // Shows status pending
        onElement { viewIdResourceName == "geoShareBillingStatusPending" }

        // Shows status purchased
        onElement { viewIdResourceName == "geoShareBillingStatusPurchased" }

        // Go to automation preferences
        pressBack()
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)

        // Does not show feature wall
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareAutomationFeatureWall" })

        // Go to main screen
        pressBack()
        quickWaitForStableInActiveWindow()
        pressBack()

        // Shows pro headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare Pro" }

        // Does not show feature badge
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareMainBillingIcon" })

        // Go to billing screen using main menu
        goToBillingScreen()

        // Expire subscription
        onElement { viewIdResourceName == "geoShareBillingManageButtonSubscription" }.click()

        // Shows expired message
        onElement { viewIdResourceName == "geoShareBillingStatusExpired" }

        // Cancel subscription
        onElement { viewIdResourceName == "geoShareBillingManageButtonSubscription" }.click()

        // Go to main screen
        pressBack()

        // Shows free headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare" }

        // Go to automation preferences
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)

        // Shows feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }
    }

    @Test
    fun allowsUsingAutomationAfterPurchase() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows feature badge
        onElement { viewIdResourceName == "geoShareAutomationFeatureBadge" }

        // Go to automation preferences using the button
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }.click()

        // Go to billing screen using the feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }.click()

        // Purchase an offer
        onElement { viewIdResourceName == "geoShareBillingOffer_${Offer.Period.ONE_TIME}" }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" && isEnabled }.click()

        // Shows status purchased
        onElement { viewIdResourceName == "geoShareBillingStatusPurchased" }

        // Go to automation preferences
        pressBack()

        // Configure automation
        val automation = CopyCoordsDecAutomation
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomation_${Json.encodeToString<Automation>(automation)}" }.click()

        // Go to the result screen
        pressBack()

        // Does not show feature badge
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareAutomationFeatureBadge" })

        // Go to the main screen
        pressBack()

        // Share the link again
        onElement { viewIdResourceName == "geoShareMainSubmitButton" }.click()

        // Shows automation success message
        onElement(pollIntervalMs = 50L) { viewIdResourceName == "geoShareResultSuccessMessage" }
    }

    @Test
    fun allowsUsingCustomLinkAfterPurchase() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Go to link list
        goToUserPreferencesList()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.LINKS}" }.click()

        // Shows feature badge
        onElement { viewIdResourceName == "geoShareCustomLinkFeatureBadge" }

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinksListItem_a5092c63-cf5c-4225-9059-e888ae12e215" }.click()

        // Go to billing screen using the feature wall
        onElement { viewIdResourceName == "geoShareCustomLinkFeatureWall" }.click()

        // Purchase an offer
        onElement { viewIdResourceName == "geoShareBillingOffer_${Offer.Period.ONE_TIME}" }.click()
        onElement { viewIdResourceName == "geoShareBillingPurchaseButton" && isEnabled }.click()

        // Shows status purchased
        onElement { viewIdResourceName == "geoShareBillingStatusPurchased" }

        // Go back to link detail
        pressBack()

        // Does not show feature wall
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareCustomLinkFeatureWall" })

        // Update link
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Apple Maps navigation" }.apply {
            setText("$text edited")
        }
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSave" }.click()
        }

        // Shows updated link
        onElement { viewIdResourceName == "geoShareLinksListItem_a5092c63-cf5c-4225-9059-e888ae12e215" }
        onElement { textAsString() == "Apple Maps navigation edited" }

        // Does not show feature badge
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareCustomLinkFeatureBadge" })
    }

    private fun goToBillingScreen() = uiAutomator {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuBilling" }
    }
}

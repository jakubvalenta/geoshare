package page.ooooo.geoshare.tests

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.serialization.json.Json
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.lib.billing.Offer
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class BillingDemoBehaviorTest {
    @Test
    fun allowsPurchasingAndRefundingOneTimeProduct() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Shows free headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare" }

        // Go to automation preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)

        // Shows feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }

        // Go to main screen
        goBackToMainForm()

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
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)

        // Does not show feature wall
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareAutomationFeatureWall" })

        // Go to main screen
        goBackToMainForm()

        // Shows pro headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare Pro" }

        // Does not show feature badge
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareMainBillingIcon" })

        // Go to billing screen using main menu
        goToBillingScreen()

        // Refund product
        onElement { viewIdResourceName == "geoShareBillingManageButtonOneTime" }.click()

        // Go to main screen
        goBackToMainForm()

        // Shows free headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare" }

        // Go to automation preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)

        // Shows feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }
    }

    @Test
    fun allowsPurchasingAndRefundingSubscription() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Shows free headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare" }

        // Go to automation preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)

        // Shows feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }

        // Go to main screen
        goBackToMainForm()

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
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)

        // Does not show feature wall
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareAutomationFeatureWall" })

        // Go to main screen
        goBackToMainForm()

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
        goBackToMainForm()

        // Shows free headline
        onElement { viewIdResourceName == "geoShareAppHeadlineText" && textAsString() == "GeoShare" }

        // Go to automation preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)

        // Shows feature wall
        onElement { viewIdResourceName == "geoShareAutomationFeatureWall" }
    }

    @Test
    fun allowsUsingAutomationAfterPurchase() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Share a URI with the app
        shareUri()

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

        // Go to main screen
        goBackToMainForm()

        // Share the link again
        onElement { viewIdResourceName == "geoShareMainSubmitButton" }.click()

        // Shows automation success message
        onElement(pollIntervalMs = 50) { viewIdResourceName == "geoShareResultMessageSuccess" }
    }

    @Test
    fun allowsUsingCustomLinkAfterPurchase() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Go to link list
        goToUserPreferencesDetail(UserPreferenceGroupId.LINKS)

        // Shows feature badge
        onElement { viewIdResourceName == "geoShareCustomLinkFeatureBadge" }

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()

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
        onElement { viewIdResourceName == "geoShareLinkListItem_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }
        onElement { textAsString() == "Apple Maps navigation edited" }

        // Does not show feature badge
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareCustomLinkFeatureBadge" })
    }

    private fun goToBillingScreen() = uiAutomator {
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement { viewIdResourceName == "geoShareMainMenuBilling" }.click()
    }
}

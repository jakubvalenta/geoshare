package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.ProductDetails
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog

fun ProductDetails.getOffers(log: ILog = DefaultLog): List<Offer> {
    oneTimePurchaseOfferDetailsList?.takeIf { it.isNotEmpty() }?.let { oneTimeOfferDetailsList ->
        return oneTimeOfferDetailsList.mapNotNull { oneTimeOfferDetails ->
            val offerToken = oneTimeOfferDetails.offerToken  // Copy to variable to support mocking
            if (offerToken != null) {
                Offer(
                    token = offerToken,
                    formattedPrice = oneTimeOfferDetails.formattedPrice,
                    period = Offer.Period.ONE_TIME,
                    productId = productId,
                )
            } else {
                null
            }
        }
    }
    oneTimePurchaseOfferDetails?.let { oneTimeOfferDetails ->
        val offerToken = oneTimeOfferDetails.offerToken  // Copy to variable to support mocking
        return if (offerToken != null) {
            listOf(
                Offer(
                    token = offerToken,
                    formattedPrice = oneTimeOfferDetails.formattedPrice,
                    period = Offer.Period.ONE_TIME,
                    productId = productId,
                )
            )
        } else {
            emptyList()
        }
    }
    subscriptionOfferDetails?.let { subscriptionOfferDetailsList ->
        return subscriptionOfferDetailsList.flatMap { subscriptionOfferDetails ->
            subscriptionOfferDetails.pricingPhases.pricingPhaseList.mapNotNull { pricingPhase ->
                if (pricingPhase.billingPeriod != "P1M") {
                    log.w("Billing", "Unsupported offer billing period ${pricingPhase.billingPeriod}")
                    null
                } else if (pricingPhase.recurrenceMode != ProductDetails.RecurrenceMode.INFINITE_RECURRING) {
                    log.w("Billing", "Unsupported offer recurrence mode ${pricingPhase.recurrenceMode}")
                    null
                } else {
                    Offer(
                        token = subscriptionOfferDetails.offerToken,
                        formattedPrice = pricingPhase.formattedPrice,
                        period = Offer.Period.MONTHLY,
                        productId = productId,
                    )
                }
            }
        }
    }
    return emptyList()
}

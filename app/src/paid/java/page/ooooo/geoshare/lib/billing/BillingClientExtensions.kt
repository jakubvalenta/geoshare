package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.ProductDetails

fun ProductDetails.getOffers(): List<Offer> {
    oneTimePurchaseOfferDetailsList?.let { oneTimeOfferDetailsList ->
        return oneTimeOfferDetailsList.mapNotNull { oneTimeOfferDetails ->
            oneTimeOfferDetails.offerToken?.let { offerToken ->
                Offer(
                    token = offerToken,
                    formattedPrice = oneTimeOfferDetails.formattedPrice,
                    period = Offer.Period.ONE_TIME,
                    productId = productId,
                )
            }
        }
    }
    oneTimePurchaseOfferDetails?.let { oneTimeOfferDetails ->
        return listOfNotNull(
            oneTimeOfferDetails.offerToken?.let { offerToken ->
                Offer(
                    token = offerToken,
                    formattedPrice = oneTimeOfferDetails.formattedPrice,
                    period = Offer.Period.ONE_TIME,
                    productId = productId,
                )
            }
        )
    }
    subscriptionOfferDetails?.let { subscriptionOfferDetailsList ->
        return subscriptionOfferDetailsList.flatMap { subscriptionOfferDetails ->
            subscriptionOfferDetails.pricingPhases.pricingPhaseList.filter { pricingPhase ->
                pricingPhase.billingPeriod == "P1M" &&
                    pricingPhase.recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING
            }.map { pricingPhase ->
                Offer(
                    token = subscriptionOfferDetails.offerToken,
                    formattedPrice = pricingPhase.formattedPrice,
                    period = Offer.Period.MONTHLY,
                    productId = productId,
                )
            }
        }
    }
    return emptyList()
}

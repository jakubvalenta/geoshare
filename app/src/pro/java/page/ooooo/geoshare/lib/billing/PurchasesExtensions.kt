package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import kotlin.time.Duration

/**
 * Finds a [Purchase] that contains one of known [products] and returns a [BillingStatus] based on it. If no [Purchase]
 * with a known product is found, it returns [BillingStatus.NotPurchased].
 */
fun List<Purchase>.getBillingStatus(products: List<BillingProduct>, refundableDuration: Duration): BillingStatus {
    for (purchase in this) {
        when (purchase.purchaseState) {
            PurchaseState.PURCHASED -> {
                val product = purchase.products.firstNotNullOfOrNull { productId ->
                    products.firstOrNull { billingProduct -> billingProduct.id == productId }
                }
                if (product != null) {
                    return BillingStatus.Purchased(
                        product = product,
                        expired = product.type == BillingProduct.Type.SUBSCRIPTION && !purchase.isAutoRenewing,
                        refundable = purchase.purchaseTime > System.currentTimeMillis() - refundableDuration.inWholeMilliseconds,
                        token = purchase.purchaseToken,
                    )
                }
            }

            PurchaseState.PENDING -> {
                return BillingStatus.Pending()
            }
        }
    }
    return BillingStatus.NotPurchased()
}

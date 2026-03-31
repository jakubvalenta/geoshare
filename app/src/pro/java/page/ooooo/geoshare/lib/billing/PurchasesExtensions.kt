package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import kotlin.time.Duration

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
                    )
                }
            }

            PurchaseState.PENDING -> {
                return BillingStatus.NotPurchased(pending = true)
            }
        }
    }
    return BillingStatus.NotPurchased(pending = false)
}

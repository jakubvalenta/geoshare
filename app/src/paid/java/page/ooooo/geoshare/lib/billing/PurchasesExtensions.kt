package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import kotlin.time.Duration

fun List<Purchase>.getBillingStatus(products: List<BillingProduct>, refundableDuration: Duration): BillingStatus {
    for (purchase in this) {
        if (purchase.purchaseState == PurchaseState.PURCHASED) {
            val product = purchase.products.firstNotNullOfOrNull { productId ->
                products.firstOrNull { billingProduct -> billingProduct.id == productId }
            }
            if (product != null) {
                return BillingStatus.Purchased(
                    product = product,
                    refundable = purchase.purchaseTime > System.currentTimeMillis() - refundableDuration.inWholeMilliseconds,
                )
            }
        }
    }
    return BillingStatus.NotPurchased()
}

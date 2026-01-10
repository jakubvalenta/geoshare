package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.Purchase

fun List<Purchase>.findProductId(plan: Plan): String? {
    for (purchase in this) {
        for (productId in purchase.products) {
            if (plan.hasProductId(productId)) {
                return productId
            }
        }
    }
    return null
}

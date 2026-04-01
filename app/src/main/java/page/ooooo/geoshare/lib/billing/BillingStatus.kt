package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

sealed interface BillingStatus {
    class Loading : BillingStatus

    data class NotPurchased(
        val pending: Boolean,
    ) : BillingStatus

    @Immutable
    data class Purchased(
        val product: BillingProduct,
        val expired: Boolean,
        val refundable: Boolean,
    ) : BillingStatus
}

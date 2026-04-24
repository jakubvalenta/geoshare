package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

sealed interface BillingStatus {
    class Loading : BillingStatus

    class Pending : BillingStatus

    class NotPurchased : BillingStatus

    @Immutable
    data class Purchased(
        val product: BillingProduct,
        val expired: Boolean,
        val refundable: Boolean,
        val token: String,
    ) : BillingStatus
}

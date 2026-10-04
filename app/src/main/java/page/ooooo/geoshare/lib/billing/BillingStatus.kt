package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

sealed interface BillingStatus {
    object Loading : BillingStatus

    object Pending : BillingStatus

    object NotPurchased : BillingStatus

    @Immutable
    data class Purchased(
        val product: BillingProduct,
        val expired: Boolean,
        val refundable: Boolean,
        val token: String,
    ) : BillingStatus
}

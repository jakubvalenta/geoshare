package page.ooooo.geoshare.data.billing

import androidx.compose.runtime.Immutable

sealed interface BillingStatus {
    data object Loading : BillingStatus

    data object Pending : BillingStatus

    data object NotPurchased : BillingStatus

    @Immutable
    data class Purchased(
        val product: BillingProduct,
        val expired: Boolean,
        val refundable: Boolean,
        val token: String,
    ) : BillingStatus
}

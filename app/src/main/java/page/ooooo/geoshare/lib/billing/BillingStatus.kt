package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

sealed interface BillingStatus {
    class Loading : BillingStatus
    class NotPurchased : BillingStatus

    @Immutable
    data class Purchased(val product: BillingProduct) : BillingStatus
}

package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

sealed interface BillingOffers {
    object Loading : BillingOffers

    @Immutable
    data class Done(val offers: ImmutableList<Offer>) : BillingOffers
}

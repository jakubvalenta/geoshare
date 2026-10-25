package page.ooooo.geoshare.data.billing

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

sealed interface BillingOffers {
    data object Loading : BillingOffers

    @Immutable
    data class Done(val offers: ImmutableList<Offer>) : BillingOffers
}

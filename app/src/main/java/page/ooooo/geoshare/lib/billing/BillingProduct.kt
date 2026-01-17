package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

@Immutable
data class BillingProduct(val id: String, val type: Type) {
    enum class Type { DONATION, ONE_TIME, SUBSCRIPTION }
}

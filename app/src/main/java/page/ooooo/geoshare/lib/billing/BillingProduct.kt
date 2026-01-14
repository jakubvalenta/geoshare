package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

@Immutable
data class BillingProduct(val id: String, val type: Type) {
    enum class Type { ONE_TIME, SUBSCRIPTION }
}

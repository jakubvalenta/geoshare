package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

sealed interface BillingStatus {

    fun getFeatureStatus(feature: Feature): FeatureStatus = FeatureStatus.LOADING

    fun getFirstProductId(): String? = null

    object Loading : BillingStatus

    @Immutable
    data class Done(val plan: Plan?) : BillingStatus {
        override fun getFeatureStatus(feature: Feature) = if (plan?.features?.contains(feature) == true) {
            FeatureStatus.AVAILABLE
        } else {
            FeatureStatus.NOT_AVAILABLE
        }

        override fun getFirstProductId() = plan?.products?.firstOrNull()?.id
    }
}

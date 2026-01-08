package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

sealed interface BillingStatus {

    fun getFeatureStatus(feature: Feature): FeatureStatus

    object Loading : BillingStatus {
        override fun getFeatureStatus(feature: Feature) = FeatureStatus.LOADING
    }

    @Immutable
    data class Done(val product: Product, val validatedAt: Long = System.currentTimeMillis()) : BillingStatus {
        override fun getFeatureStatus(feature: Feature) = if (product.features.contains(feature)) {
            FeatureStatus.AVAILABLE
        } else {
            FeatureStatus.NOT_AVAILABLE
        }
    }
}

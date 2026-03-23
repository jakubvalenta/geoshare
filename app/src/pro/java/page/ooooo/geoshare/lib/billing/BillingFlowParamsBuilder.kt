package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.BillingFlowParams

interface BillingFlowParamsBuilder {
    fun build(): BillingFlowParams
    fun setProductDetailsParamsList(productDetailsParamsList: List<BillingFlowParams.ProductDetailsParams>): BillingFlowParamsBuilder
}

class DefaultBillingFlowParamsBuilder() : BillingFlowParamsBuilder {
    private val builder = BillingFlowParams.newBuilder()

    override fun build(): BillingFlowParams = builder.build()

    override fun setProductDetailsParamsList(productDetailsParamsList: List<BillingFlowParams.ProductDetailsParams>): BillingFlowParamsBuilder {
        builder.setProductDetailsParamsList(productDetailsParamsList)
        return this
    }
}

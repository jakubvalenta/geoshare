package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails

interface ProductDetailsParamsBuilder {
    fun build(): BillingFlowParams.ProductDetailsParams
    fun setOfferToken(offerToken: String): ProductDetailsParamsBuilder
    fun setProductDetails(productDetails: ProductDetails): ProductDetailsParamsBuilder
}

class DefaultProductDetailsParamsBuilder() : ProductDetailsParamsBuilder {
    private val builder = BillingFlowParams.ProductDetailsParams.newBuilder()

    override fun build(): BillingFlowParams.ProductDetailsParams = builder.build()

    override fun setOfferToken(offerToken: String): ProductDetailsParamsBuilder {
        builder.setOfferToken(offerToken)
        return this
    }

    override fun setProductDetails(productDetails: ProductDetails): ProductDetailsParamsBuilder {
        builder.setProductDetails(productDetails)
        return this
    }
}

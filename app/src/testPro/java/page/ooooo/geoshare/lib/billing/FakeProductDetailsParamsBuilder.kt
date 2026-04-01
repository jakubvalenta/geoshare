package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails

class FakeProductDetailsParamsBuilder : ProductDetailsParamsBuilder {
    private val builder = BillingFlowParams.ProductDetailsParams.newBuilder()

    override fun build(): BillingFlowParams.ProductDetailsParams =
        builder.build()

    override fun setOfferToken(offerToken: String): ProductDetailsParamsBuilder {
        // Don't call Builder.setOfferToken(), because it crashes due to TextUtils not mocked
        return this
    }

    override fun setProductDetails(productDetails: ProductDetails): ProductDetailsParamsBuilder {
        builder.setProductDetails(productDetails)
        return this
    }
}

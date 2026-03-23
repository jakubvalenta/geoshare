package page.ooooo.geoshare.lib.billing

import com.android.billingclient.api.BillingFlowParams
import org.mockito.kotlin.mock

class FakeBillingFlowParamsBuilder() : BillingFlowParamsBuilder {
    override fun build(): BillingFlowParams {
        // Don't call BillingFlowParams.Builder.build(), because it crashes due to TextUtils not mocked
        return mock()
    }

    override fun setProductDetailsParamsList(productDetailsParamsList: List<BillingFlowParams.ProductDetailsParams>): BillingFlowParamsBuilder =
        this
}

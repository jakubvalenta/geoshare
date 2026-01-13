package page.ooooo.geoshare.lib.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener

interface BillingClientBuilder {
    fun setListener(listener: PurchasesUpdatedListener): BillingClientBuilder

    fun enableAutoServiceReconnection(): BillingClientBuilder

    fun build(): BillingClient
}

class DefaultBillingClientBuilder(val context: Context) : BillingClientBuilder {
    private val builder = BillingClient.newBuilder(context)

    override fun setListener(listener: PurchasesUpdatedListener): BillingClientBuilder {
        builder.setListener(listener)
        return this
    }

    override fun enableAutoServiceReconnection(): BillingClientBuilder {
        builder.enableAutoServiceReconnection()
        return this
    }

    override fun build(): BillingClient {
        return builder.build()
    }
}

package page.ooooo.geoshare.lib.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener

interface BillingClientBuilder {
    fun build(): BillingClient
    fun enableAutoServiceReconnection(): BillingClientBuilder
    fun enablePendingPurchases(pendingPurchasesParams: PendingPurchasesParams): BillingClientBuilder
    fun setListener(listener: PurchasesUpdatedListener): BillingClientBuilder
}

class DefaultBillingClientBuilder(val context: Context) : BillingClientBuilder {
    private val builder = BillingClient.newBuilder(context)

    override fun build(): BillingClient {
        return builder.build()
    }

    override fun enableAutoServiceReconnection(): BillingClientBuilder {
        builder.enableAutoServiceReconnection()
        return this
    }

    override fun enablePendingPurchases(pendingPurchasesParams: PendingPurchasesParams): BillingClientBuilder {
        builder.enablePendingPurchases(pendingPurchasesParams)
        return this
    }

    override fun setListener(listener: PurchasesUpdatedListener): BillingClientBuilder {
        builder.setListener(listener)
        return this
    }
}

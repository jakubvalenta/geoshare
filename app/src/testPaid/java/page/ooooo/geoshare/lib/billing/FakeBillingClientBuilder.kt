@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package page.ooooo.geoshare.lib.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.AlternativeBillingOnlyAvailabilityListener
import com.android.billingclient.api.AlternativeBillingOnlyInformationDialogListener
import com.android.billingclient.api.AlternativeBillingOnlyReportingDetailsListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingConfigResponseListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingProgramAvailabilityListener
import com.android.billingclient.api.BillingProgramReportingDetailsListener
import com.android.billingclient.api.BillingProgramReportingDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ExternalOfferAvailabilityListener
import com.android.billingclient.api.ExternalOfferInformationDialogListener
import com.android.billingclient.api.ExternalOfferReportingDetailsListener
import com.android.billingclient.api.GetBillingConfigParams
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResponseListener
import com.android.billingclient.api.LaunchExternalLinkParams
import com.android.billingclient.api.LaunchExternalLinkResponseListener
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

open class FakeBillingClient : BillingClient() {
    var purchasesUpdatedListener: PurchasesUpdatedListener? = null

    override fun getConnectionState(): Int {
        throw NotImplementedError()
    }

    override fun isFeatureSupported(p0: String): BillingResult {
        throw NotImplementedError()
    }

    override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
        throw NotImplementedError()
    }

    override fun showAlternativeBillingOnlyInformationDialog(
        p0: Activity,
        p1: AlternativeBillingOnlyInformationDialogListener,
    ): BillingResult {
        throw NotImplementedError()
    }

    override fun showExternalOfferInformationDialog(
        p0: Activity,
        p1: ExternalOfferInformationDialogListener,
    ): BillingResult {
        throw NotImplementedError()
    }

    override fun showInAppMessages(
        p0: Activity,
        p1: InAppMessageParams,
        p2: InAppMessageResponseListener,
    ): BillingResult {
        throw NotImplementedError()
    }

    override fun acknowledgePurchase(p0: AcknowledgePurchaseParams, p1: AcknowledgePurchaseResponseListener) {
        throw NotImplementedError()
    }

    override fun consumeAsync(p0: ConsumeParams, p1: ConsumeResponseListener) {
        throw NotImplementedError()
    }

    override fun createAlternativeBillingOnlyReportingDetailsAsync(p0: AlternativeBillingOnlyReportingDetailsListener) {
        throw NotImplementedError()
    }

    override fun createBillingProgramReportingDetailsAsync(
        p0: BillingProgramReportingDetailsParams,
        p1: BillingProgramReportingDetailsListener,
    ) {
        throw NotImplementedError()
    }

    override fun createExternalOfferReportingDetailsAsync(p0: ExternalOfferReportingDetailsListener) {
        throw NotImplementedError()
    }

    override fun endConnection() {
        throw NotImplementedError()
    }

    override fun getBillingConfigAsync(p0: GetBillingConfigParams, p1: BillingConfigResponseListener) {
        throw NotImplementedError()
    }

    override fun isAlternativeBillingOnlyAvailableAsync(p0: AlternativeBillingOnlyAvailabilityListener) {
        throw NotImplementedError()
    }

    override fun isBillingProgramAvailableAsync(p0: Int, p1: BillingProgramAvailabilityListener) {
        throw NotImplementedError()
    }

    @Deprecated("Deprecated in Java")
    override fun isExternalOfferAvailableAsync(p0: ExternalOfferAvailabilityListener) {
        throw NotImplementedError()
    }

    override fun launchExternalLink(
        p0: Activity,
        p1: LaunchExternalLinkParams,
        p2: LaunchExternalLinkResponseListener,
    ) {
        throw NotImplementedError()
    }

    override fun queryProductDetailsAsync(p0: QueryProductDetailsParams, p1: ProductDetailsResponseListener) {
        throw NotImplementedError()
    }

    override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
        throw NotImplementedError()
    }

    override fun startConnection(p0: BillingClientStateListener) {
        throw NotImplementedError()
    }

    override fun isReady(): Boolean {
        throw NotImplementedError()
    }
}

class FakeBillingClientBuilder(
    val billingClient: FakeBillingClient = FakeBillingClient(),
) : BillingClientBuilder {
    private lateinit var purchasesUpdatedListener: PurchasesUpdatedListener

    override fun setListener(listener: PurchasesUpdatedListener): BillingClientBuilder {
        this.purchasesUpdatedListener = listener
        return this
    }

    override fun enableAutoServiceReconnection(): BillingClientBuilder {
        return this
    }

    override fun build(): BillingClient {
        billingClient.purchasesUpdatedListener = purchasesUpdatedListener
        return billingClient
    }
}

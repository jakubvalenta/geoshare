package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toUri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResponseListener
import com.android.billingclient.api.InAppMessageResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Message
import kotlin.time.Duration.Companion.hours

class BillingImpl(
    context: Context,
    billingClientBuilder: BillingClientBuilder = DefaultBillingClientBuilder(context),
    override val products: ImmutableList<BillingProduct> = persistentListOf(
        BillingProduct("pro_one_time", BillingProduct.Type.ONE_TIME),
        BillingProduct("pro_subscription", BillingProduct.Type.SUBSCRIPTION),
    ),
    private val productDetailsParamsBuilder: () -> ProductDetailsParamsBuilder = { DefaultProductDetailsParamsBuilder() },
    private val billingFlowParamsBuilder: () -> BillingFlowParamsBuilder = { DefaultBillingFlowParamsBuilder() },
    private val log: ILog = DefaultLog,
) : Billing(context), AcknowledgePurchaseResponseListener, BillingClientStateListener, InAppMessageResponseListener,
    PurchasesResponseListener, PurchasesUpdatedListener {

    companion object {
        const val TAG = "Billing"
    }

    @StringRes
    override val appNameResId = R.string.app_name_pro
    override val features = persistentListOf(AutomationFeature)
    override val refundableDuration = 48.hours

    private val billingClient: BillingClient = billingClientBuilder.setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection().build()

    private val _status: MutableStateFlow<BillingStatus> = MutableStateFlow(BillingStatus.Loading())
    override val status: StateFlow<BillingStatus> = _status

    private val _message: MutableStateFlow<Message?> = MutableStateFlow(null)
    override val message: StateFlow<Message?> = _message

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                log.i(TAG, "Billing setup: ok")
                queryPurchases()
            }

            else -> {
                log.e(TAG, "Billing setup: error ${billingResult.debugMessage}")
                _message.value = Message(context.getString(R.string.billing_setup_error_unknown), isError = true)
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        log.i(TAG, "Disconnected")
        // Let's hope it's fine to do nothing here, since we use automatic reconnection
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK if purchases != null -> {
                for (purchase in purchases) {
                    if (purchase.purchaseState == PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        // Notice that we don't support consumable products.
                        val acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
                    }
                }
                val newBillingStatus = purchases.getBillingStatus(products, refundableDuration)
                if (newBillingStatus is BillingStatus.Purchased) {
                    log.i(TAG, "Purchase update: purchased")
                    _status.value = newBillingStatus
                    _message.value = Message(
                        context.getString(
                            R.string.billing_purchase_success, context.getString(appNameResId)
                        )
                    )
                } else {
                    log.i(TAG, "Purchase update: not purchased")
                    _status.value = newBillingStatus
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                log.i(TAG, "Purchase update: cancelled")
                _message.value = Message(context.getString(R.string.billing_purchase_error_cancelled), isError = true)
            }

            else -> {
                log.e(TAG, "Purchase update: error ${billingResult.debugMessage}")
                _message.value = Message(context.getString(R.string.billing_purchase_error_unknown), isError = true)
            }
        }
    }

    override fun onQueryPurchasesResponse(billingResult: BillingResult, purchases: List<Purchase>) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val newBillingStatus = purchases.getBillingStatus(products, refundableDuration)
                if (newBillingStatus is BillingStatus.Purchased) {
                    log.i(TAG, "Purchase query: purchased")
                    _status.value = newBillingStatus
                } else if (_status.value is BillingStatus.Loading) {
                    log.i(
                        TAG, "Purchase query: not purchased; setting status, because the previous status was loading"
                    )
                    _status.value = newBillingStatus
                } else {
                    log.i(
                        TAG,
                        "Purchase query: not purchased; not setting status, because the previous status contains another purchased product"
                    )
                }
            }

            else -> {
                log.e(TAG, "Purchases query: error ${billingResult.debugMessage}")
                _message.value = Message(context.getString(R.string.billing_purchase_error_unknown), isError = true)
            }
        }
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                log.i(TAG, "Purchase acknowledgement: ok")
            }

            else -> {
                log.e(TAG, "Purchase acknowledgement: error ${billingResult.debugMessage}")
            }
        }
    }

    override fun onInAppMessageResponse(inAppMessageResult: InAppMessageResult) {
        when (inAppMessageResult.responseCode) {
            InAppMessageResult.InAppMessageResponseCode.NO_ACTION_NEEDED -> {
                log.i(TAG, "In-app messaging: no action needed")
            }

            InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED -> {
                log.i(TAG, "In-app messaging: subscription status updated")
                queryPurchases()
            }
        }
    }

    override fun startConnection() {
        _message.value = null
        billingClient.startConnection(this)
    }

    override fun endConnection() {
        billingClient.endConnection()
    }

    override suspend fun queryOffers(): List<Offer> =
        queryProductDetailsAndOffers().map { (_, offer) -> offer }.toList()

    override suspend fun launchBillingFlow(activity: Activity, offerToken: String) {
        _message.value = null
        val (productDetails) = try {
            queryProductDetailsAndOffers().first { (_, offer) -> offer.token == offerToken }
        } catch (_: NoSuchElementException) {
            log.e(TAG, "Offer token not found: $offerToken")
            _message.value = Message(context.getString(R.string.billing_purchase_error_unknown), isError = true)
            return
        }

        val productDetailsParamsList = listOf(
            productDetailsParamsBuilder().setProductDetails(productDetails).setOfferToken(offerToken).build()
        )
        val billingFlowParams = billingFlowParamsBuilder().setProductDetailsParamsList(productDetailsParamsList).build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                log.i(TAG, "Billing flow: ok")
            }

            else -> {
                log.e(TAG, "Billing flow: error ${billingResult.debugMessage}")
                _message.value = Message(context.getString(R.string.billing_purchase_error_unknown), isError = true)
            }
        }
    }

    override fun manageProduct(product: BillingProduct) {
        _message.value = null
        try {
            when (product.type) {
                BillingProduct.Type.DONATION -> {}

                BillingProduct.Type.ONE_TIME -> {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/account/orderhistory".toUri(),
                        )
                    )
                }

                BillingProduct.Type.SUBSCRIPTION -> {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/account/subscriptions?sku=%s&package=page.ooooo.geoshare".format(
                                Uri.encode(product.id)
                            ).toUri(),
                        )
                    )
                }
            }
        } catch (_: ActivityNotFoundException) {
            _message.value = Message(context.getString(R.string.billing_manage_error), isError = true)
        }
    }

    override suspend fun showInAppMessages(activity: Activity) {
        // Wait for connection
        status.first { it !is BillingStatus.Loading }

        val inAppMessageParams = InAppMessageParams.newBuilder()
            .addInAppMessageCategoryToShow(InAppMessageParams.InAppMessageCategoryId.TRANSACTIONAL).build()
        billingClient.showInAppMessages(activity, inAppMessageParams, this)
    }

    private fun queryProductDetailsAndOffers(): Flow<Pair<ProductDetails, Offer>> = flow {
        products.groupBy { it.type }.forEach { (billingProductType, billingProducts) ->
            val productType = when (billingProductType) {
                BillingProduct.Type.DONATION -> return@forEach
                BillingProduct.Type.ONE_TIME -> ProductType.INAPP
                BillingProduct.Type.SUBSCRIPTION -> ProductType.SUBS
            }
            val productList = billingProducts.map { billingProduct ->
                QueryProductDetailsParams.Product.newBuilder().setProductId(billingProduct.id)
                    .setProductType(productType).build()
            }
            val params = QueryProductDetailsParams.newBuilder()
            params.setProductList(productList)

            val productDetailsResult = try {
                withContext(Dispatchers.IO) {
                    billingClient.queryProductDetails(params.build())
                }
            } catch (e: Exception) {
                log.e(TAG, "Product details query: error", e)
                return@flow
            }
            log.i(TAG, "Product details query: ok")
            productDetailsResult.productDetailsList?.forEach { productDetails ->
                for (offer in productDetails.getOffers(log)) {
                    emit(productDetails to offer)
                }
            }
        }
    }

    private fun queryPurchases() {
        for (productType in listOf(ProductType.INAPP, ProductType.SUBS)) {
            val queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(productType).build()
            billingClient.queryPurchasesAsync(queryPurchasesParams, this)
        }
    }
}

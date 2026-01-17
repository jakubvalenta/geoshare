package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
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
import kotlin.time.Duration.Companion.hours

class BillingImpl(
    context: Context,
    billingClientBuilder: BillingClientBuilder = DefaultBillingClientBuilder(context),
    private val log: ILog = DefaultLog,
) : Billing(context), BillingClientStateListener, PurchasesResponseListener, PurchasesUpdatedListener {

    @StringRes
    override val appNameResId = R.string.app_name_pro
    override val features = persistentListOf(AutomationFeature)
    override val products = persistentListOf(
        BillingProduct("pro_one_time", BillingProduct.Type.ONE_TIME),
        BillingProduct("pro_subscription", BillingProduct.Type.SUBSCRIPTION),
    )
    override val refundableDuration = 48.hours

    private val billingClient: BillingClient = billingClientBuilder
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    private val _status: MutableStateFlow<BillingStatus> = MutableStateFlow(BillingStatus.Loading())
    override val status: StateFlow<BillingStatus> = _status

    override val offers = flow {
        emit(
            queryProductDetailsAndOffers().map { (_, offer) -> offer }.toList()
        )
    }

    private val _errorMessageResId: MutableStateFlow<Int?> = MutableStateFlow(null)
    override val errorMessageResId: StateFlow<Int?> = _errorMessageResId

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            log.i("Billing", "Billing setup: ok")
            for (productType in listOf(ProductType.INAPP, ProductType.SUBS)) {
                val queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(productType).build()
                billingClient.queryPurchasesAsync(queryPurchasesParams, this)
            }
        } else {
            log.e("Billing", "Billing setup: error ${billingResult.debugMessage}")
            _errorMessageResId.value = R.string.billing_setup_error_unknown
        }
    }

    override fun onBillingServiceDisconnected() {
        log.i("Billing", "Disconnected")
        // Let's hope it's fine to do nothing here, since we use automatic reconnection
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK if purchases != null -> {
                // TODO Acknowledge the purchase
                val newBillingStatus = purchases.getBillingStatus(products, refundableDuration)
                if (newBillingStatus is BillingStatus.Purchased) {
                    log.i("Billing", "Purchase update: purchased")
                } else {
                    log.i("Billing", "Purchase update: not purchased")
                }
                _status.value = newBillingStatus
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                log.i("Billing", "Purchase update: cancelled")
                _errorMessageResId.value = R.string.billing_purchase_error_cancelled
            }

            else -> {
                log.e("Billing", "Purchase update: error ${billingResult.debugMessage}")
                _errorMessageResId.value = R.string.billing_purchase_error_unknown
            }
        }
    }

    override fun onQueryPurchasesResponse(billingResult: BillingResult, purchases: List<Purchase>) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val newBillingStatus = purchases.getBillingStatus(products, refundableDuration)
                if (newBillingStatus is BillingStatus.Purchased) {
                    log.i("Billing", "Purchase query: purchased")
                    _status.value = newBillingStatus
                } else if (_status.value is BillingStatus.Loading) {
                    log.i(
                        "Billing",
                        "Purchase query: not purchased; setting status, because the previous status was loading"
                    )
                    _status.value = newBillingStatus
                } else {
                    log.i(
                        "Billing",
                        "Purchase query: not purchased; not setting status, because the previous status contains another purchased product"
                    )
                }
            }

            else -> {
                log.e("Billing", "Purchases query: error ${billingResult.debugMessage}")
                _errorMessageResId.value = R.string.billing_purchase_error_unknown
            }
        }
    }

    override fun startConnection() {
        billingClient.startConnection(this)
    }

    override fun endConnection() {
        billingClient.endConnection()
    }

    override suspend fun launchBillingFlow(activity: Activity, offerToken: String) {
        val (productDetails) = try {
            queryProductDetailsAndOffers().first { (_, offer) -> offer.token == offerToken }
        } catch (_: NoSuchElementException) {
            log.e("Billing", "Offer token not found: $offerToken")
            _errorMessageResId.value = R.string.billing_purchase_error_unknown
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails)
                .setOfferToken(offerToken).build()
        )
        val billingFlowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                log.i("Billing", "Billing flow: ok")
            }

            else -> {
                log.e("Billing", "Billing flow: error ${billingResult.debugMessage}")
                _errorMessageResId.value = R.string.billing_purchase_error_unknown
            }
        }
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
                log.e("Billing", "Product details query: error", e)
                return@flow
            }
            log.i("Billing", "Product details query: ok")
            productDetailsResult.productDetailsList?.forEach { productDetails ->
                for (offer in productDetails.getOffers(log)) {
                    emit(productDetails to offer)
                }
            }
        }
    }
}

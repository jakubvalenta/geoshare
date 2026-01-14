package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog

class BillingImpl(
    context: Context,
    billingClientBuilder: BillingClientBuilder = DefaultBillingClientBuilder(context),
    private val log: ILog = DefaultLog,
) :
    Billing(context),
    BillingClientStateListener,
    PurchasesResponseListener,
    PurchasesUpdatedListener {

    override val availablePlans = listOf(
        object : Plan {
            @StringRes
            override val appNameResId = R.string.app_name_pro
            override val oneTimeProductId = "pro_one_time"
            override val subscriptionProductId = "pro_subscription"
            override val features = persistentListOf(AutomationFeature)
        },
    )

    private val billingClient: BillingClient = billingClientBuilder
        .setListener(this)
        .enableAutoServiceReconnection()
        .build()

    private val _status: MutableStateFlow<BillingStatus> = MutableStateFlow(BillingStatus.Loading)
    override val status: StateFlow<BillingStatus> = _status

    override val offers: StateFlow<List<Offer>> = flow<List<Offer>> {
        queryProductDetailsAndOffers()
            .map { (_, offer) -> offer }
            .toList()
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    private val _errorMessageResId: MutableStateFlow<Int?> = MutableStateFlow(null)
    override val errorMessageResId: StateFlow<Int?> = _errorMessageResId

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            log.i("Billing", "Billing setup: ok")
            for (productType in listOf(ProductType.INAPP, ProductType.SUBS)) {
                val queryPurchasesParams = QueryPurchasesParams.newBuilder()
                    .setProductType(productType)
                    .build()
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
                log.i("Billing", "Purchase update: ok")
                val plan = purchases.firstNotNullOfOrNull { purchase ->
                    purchase.takeIf { it.purchaseState == PurchaseState.PURCHASED }
                        ?.products?.firstNotNullOfOrNull { productId ->
                            availablePlans.firstOrNull { it.hasProductId(productId) }
                        }
                }
                if (plan != null) {
                    _status.value = BillingStatus.Done(plan)
                }
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
                log.i("Billing", "Purchases query: ok")
                val plan = purchases.firstNotNullOfOrNull { purchase ->
                    purchase.takeIf { it.purchaseState == PurchaseState.PURCHASED }
                        ?.products?.firstNotNullOfOrNull { productId ->
                            availablePlans.firstOrNull { it.hasProductId(productId) }
                        }
                }
                if (plan != null) {
                    _status.value = BillingStatus.Done(plan)
                } else if (_status.value is BillingStatus.Loading) {
                    _status.value = BillingStatus.Done(null)
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
            queryProductDetailsAndOffers()
                .first { (_, offer) -> offer.token == offerToken }
        } catch (_: NoSuchElementException) {
            log.e("Billing", "Offer token not found: $offerToken")
            _errorMessageResId.value = R.string.billing_purchase_error_unknown
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

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
        val productList = availablePlans.flatMap { plan ->
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(plan.oneTimeProductId)
                    .setProductType(ProductType.INAPP)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(plan.subscriptionProductId)
                    .setProductType(ProductType.SUBS)
                    .build(),
            )
        }
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }
        productDetailsResult.productDetailsList?.forEach { productDetails ->
            for (offer in productDetails.getOffers()) {
                emit(productDetails to offer)
            }
        }
    }
}

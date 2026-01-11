package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.UserPreferencesRepository

class BillingImpl(
    coroutineScope: CoroutineScope,
    userPreferencesRepository: UserPreferencesRepository,
) :
    Billing(coroutineScope, userPreferencesRepository) {

    private val plan = object : Plan {
        @StringRes
        override val appNameResId = R.string.app_name_pro
        override val oneTimeProductId = "pro_one_time"
        override val subscriptionProductId = "pro_subscription"
        override val features = persistentListOf(AutomationFeature)
    }

    private val _status: MutableStateFlow<BillingStatus> = MutableStateFlow(BillingStatus.Loading)
    override val status: StateFlow<BillingStatus> = _status

    override val offers: StateFlow<List<Offer>> = flow<List<Offer>> {
        queryProductDetailsAndOffers()
            .map { (_, offer) -> offer }
            .toList()
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList(),
    )

    private val _errorMessageResId: MutableStateFlow<Int?> = MutableStateFlow(null)
    override val errorMessageResId: StateFlow<Int?> = _errorMessageResId

    private var billingClient: BillingClient? = null

    val billingClientListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i("Billing", "Billing setup ok")
                coroutineScope.launch {
                    val purchases = queryPurchases()
                    val newProductId = purchases.findProductId(plan)
                    val newPlan = plan.takeIf { newProductId != null }
                    val newStatus = BillingStatus.Done(newPlan)
                    setCachedProductId(newProductId)
                    _status.value = newStatus
                }
            } else {
                Log.e("Billing", "Billing setup error: ${billingResult.debugMessage}")
                _errorMessageResId.value = R.string.billing_setup_error_unknown
            }
        }

        override fun onBillingServiceDisconnected() {
            Log.i("Billing", "Billing service disconnected")
            // Let's hope it's fine to do nothing here, since we use automatic reconnection
        }
    }

    val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK if purchases != null -> {
                Log.i("Billing", "Purchase ok")
                val newProductId = purchases.findProductId(plan)
                val newPlan = plan.takeIf { newProductId != null }
                val newStatus = BillingStatus.Done(newPlan)
                coroutineScope.launch {
                    setCachedProductId(newProductId)
                    _status.value = newStatus
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i("Billing", "Purchase cancelled")
                _errorMessageResId.value = R.string.billing_purchase_error_cancelled
            }

            else -> {
                Log.e("Billing", "Purchase error: ${billingResult.debugMessage}")
                _errorMessageResId.value = R.string.billing_purchase_error_unknown
            }
        }
    }

    override fun startConnection(context: Context) {
        coroutineScope.launch {
            val newProductId = getCachedProductId()
            val newPlan = plan.takeIf { newProductId != null && it.hasProductId(newProductId) }
            _status.value = BillingStatus.Done(newPlan)

            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enableAutoServiceReconnection()
                .build()
                .apply { startConnection(billingClientListener) }
        }
    }

    override fun endConnection() {
        billingClient?.endConnection()
    }

    override fun launchBillingFlow(activity: Activity, offerToken: String) {
        coroutineScope.launch {
            val (productDetails) = queryProductDetailsAndOffers()
                .firstOrNull { it.second.token == offerToken }
                ?: return@launch // TODO Don't exit silently

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient?.let { billingClient ->
                val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        Log.i("Billing", "Billing flow ok")
                    }

                    else -> {
                        Log.e("Billing", "Billing flow error: ${billingResult.debugMessage}")
                        _errorMessageResId.value = R.string.billing_purchase_error_unknown
                    }
                }
            }
            // TODO Don't exit silently if billingClient is null
        }
    }

    private fun queryProductDetailsAndOffers(): Flow<Pair<ProductDetails, Offer>> = flow {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(plan.oneTimeProductId)
                .setProductType(ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(plan.subscriptionProductId)
                .setProductType(ProductType.SUBS)
                .build(),
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        billingClient?.let { billingClient ->
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

    private suspend fun queryPurchases(): List<Purchase> =
        listOf(ProductType.INAPP, ProductType.SUBS).flatMap { productType ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()
            billingClient?.let { billingClient ->
                val purchasesResult = withContext(Dispatchers.IO) {
                    billingClient.queryPurchasesAsync(params)
                }
                purchasesResult.purchasesList
            } ?: emptyList()
        }
}

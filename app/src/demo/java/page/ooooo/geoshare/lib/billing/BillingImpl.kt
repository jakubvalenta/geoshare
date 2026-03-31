package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Message
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class BillingImpl(private val context: Context) : Billing {

    @StringRes
    override val appNameResId = R.string.app_name_pro
    override val features = persistentListOf(AutomationFeature)
    override val products = persistentListOf(
        BillingProduct("demo_lifetime", BillingProduct.Type.ONE_TIME),
        BillingProduct("demo_monthly", BillingProduct.Type.SUBSCRIPTION),
    )
    override val refundableDuration = 48.hours

    private val offers = persistentListOf(
        Offer("offer_one_time", "$19", Offer.Period.ONE_TIME, "demo_lifetime"),
        Offer("offer_subscription", "$1.50", Offer.Period.MONTHLY, "demo_monthly"),
    )

    private val _status: MutableStateFlow<BillingStatus> = MutableStateFlow(BillingStatus.Loading())
    override val status: StateFlow<BillingStatus> = _status

    private val _message: MutableStateFlow<Message?> = MutableStateFlow(null)
    override val message: StateFlow<Message?> = _message

    override fun startConnection() {
        _message.value = null
        CoroutineScope(Dispatchers.Default).launch {
            delay(2.seconds)
            _status.value = BillingStatus.NotPurchased(pending = false)
        }
    }

    override fun endConnection() {}

    override suspend fun queryOffers(): BillingOffers = BillingOffers.Done(offers)

    override suspend fun launchBillingFlow(activity: Activity, offerToken: String) {
        _message.value = null
        delay(1.seconds)
        val product = offers.firstOrNull { offer -> offer.token == offerToken }?.let { offer ->
            products.firstOrNull { product -> product.id == offer.productId }
        }
        if (product != null) {
            _status.value = BillingStatus.NotPurchased(pending = true)
            delay(3.seconds)
            _status.value = BillingStatus.Purchased(product, expired = false, refundable = true)
        } else {
            _message.value = Message(context.getString(R.string.billing_purchase_error_unknown), isError = true)
        }
    }

    override fun manageProduct(activity: Activity, product: BillingProduct) {
        _message.value = null
        when (product.type) {
            BillingProduct.Type.DONATION -> {}

            BillingProduct.Type.ONE_TIME -> {
                _status.value = BillingStatus.NotPurchased(pending = false)
            }

            BillingProduct.Type.SUBSCRIPTION -> {
                // If the status is not expired, make it expired
                _status.value = (_status.value as? BillingStatus.Purchased)
                    ?.takeUnless { it.expired }
                    ?.copy(expired = true)
                    // If the status is expired, make it not purchased
                    ?: BillingStatus.NotPurchased(pending = false)
            }
        }
    }

    override suspend fun showInAppMessages(activity: Activity) {}

    override fun dismissMessage() {
        _message.value = null
    }
}

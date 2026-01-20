package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import page.ooooo.geoshare.R
import kotlin.time.Duration

class BillingImpl(context: Context) : Billing(context) {

    private val product = BillingProduct("free_one_time", BillingProduct.Type.DONATION)

    @StringRes
    override val appNameResId = R.string.app_name
    override val features = persistentListOf(AutomationFeature)
    override val products = persistentListOf(product)
    override val refundableDuration = Duration.ZERO

    override val status = flowOf(BillingStatus.Purchased(product, refundable = false))
        .stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.WhileSubscribed(5000),
            BillingStatus.Loading(),
        )

    override val message = flowOf(null)
        .stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.WhileSubscribed(5000),
            null,
        )

    override fun startConnection() {}

    override fun endConnection() {}

    override suspend fun queryOffers(): List<Offer> = emptyList()

    override suspend fun launchBillingFlow(activity: Activity, offerToken: String) {}

    override fun manageProduct(product: BillingProduct) {}

    override suspend fun showInAppMessages(activity: Activity) {}
}

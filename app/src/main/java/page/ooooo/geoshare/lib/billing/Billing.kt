package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.lib.Message
import kotlin.time.Duration

@Suppress("EmptyMethod")
abstract class Billing(
    protected val context: Context,
) {
    abstract val appNameResId: Int
    abstract val features: ImmutableList<Feature>
    abstract val products: ImmutableList<BillingProduct>
    abstract val refundableDuration: Duration

    abstract val message: StateFlow<Message?>
    abstract val status: StateFlow<BillingStatus>

    abstract fun startConnection()

    abstract fun endConnection()

    abstract suspend fun queryOffers(): List<Offer>

    abstract suspend fun launchBillingFlow(activity: Activity, offerToken: String)

    abstract fun manageProduct(product: BillingProduct)
}

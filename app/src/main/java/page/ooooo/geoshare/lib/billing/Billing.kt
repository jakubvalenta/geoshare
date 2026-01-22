package page.ooooo.geoshare.lib.billing

import android.app.Activity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.lib.Message
import kotlin.time.Duration

@Suppress("EmptyMethod")
interface Billing {
    val appNameResId: Int
    val features: ImmutableList<Feature>
    val products: ImmutableList<BillingProduct>
    val refundableDuration: Duration

    val message: StateFlow<Message?>
    val status: StateFlow<BillingStatus>

    fun startConnection()

    fun endConnection()

    suspend fun queryOffers(): List<Offer>

    suspend fun launchBillingFlow(activity: Activity, offerToken: String)

    fun manageProduct(product: BillingProduct)

    suspend fun showInAppMessages(activity: Activity)
}

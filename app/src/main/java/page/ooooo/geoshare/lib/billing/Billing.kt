package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

abstract class Billing(
    @Suppress("unused") context: Context,
) {
    abstract val appNameResId: Int
    abstract val features: ImmutableList<Feature>
    abstract val products: ImmutableList<BillingProduct>
    abstract val refundableDuration: Duration

    abstract val errorMessageResId: StateFlow<Int?>
    abstract val offers: Flow<List<Offer>>
    abstract val status: StateFlow<BillingStatus>

    abstract fun startConnection()

    abstract fun endConnection()

    abstract suspend fun launchBillingFlow(activity: Activity, offerToken: String)
}

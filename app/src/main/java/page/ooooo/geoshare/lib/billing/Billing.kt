package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

abstract class Billing(
    @Suppress("unused") context: Context,
) {
    abstract val availablePlans: List<Plan>
    abstract val status: StateFlow<BillingStatus>
    abstract val offers: Flow<List<Offer>>
    abstract val errorMessageResId: StateFlow<Int?>

    abstract fun startConnection()

    abstract fun endConnection()

    abstract suspend fun launchBillingFlow(activity: Activity, offerToken: String)
}

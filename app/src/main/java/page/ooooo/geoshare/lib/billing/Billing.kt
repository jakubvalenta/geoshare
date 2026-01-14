package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.StateFlow

abstract class Billing(
    @Suppress("unused") context: Context,
) {
    abstract val plans: List<Plan>
    abstract val status: StateFlow<BillingStatus>
    abstract val offers: StateFlow<List<Offer>>
    abstract val errorMessageResId: StateFlow<Int?>

    abstract fun startConnection()

    abstract fun endConnection()

    abstract suspend fun launchBillingFlow(activity: Activity, offerToken: String)
}

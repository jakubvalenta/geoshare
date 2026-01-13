package page.ooooo.geoshare.lib.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface Billing {
    val plans: List<Plan>
    val status: StateFlow<BillingStatus>
    val offers: StateFlow<List<Offer>>
    val errorMessageResId: StateFlow<Int?>

    fun startConnection()

    fun endConnection()

    suspend fun launchBillingFlow(activity: Activity, offerToken: String)
}

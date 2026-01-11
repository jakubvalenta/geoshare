package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.BillingCachedProductIdPreference

abstract class Billing(
    val coroutineScope: CoroutineScope,
    val userPreferencesRepository: UserPreferencesRepository,
) {
    abstract val status: StateFlow<BillingStatus>
    abstract val offers: StateFlow<List<Offer>>
    abstract val errorMessageResId: StateFlow<Int?>

    abstract fun startConnection(context: Context)

    abstract fun endConnection()

    abstract fun launchBillingFlow(activity: Activity, offerToken: String)

    suspend fun getCachedProductId(): String? =
        userPreferencesRepository.getValue(BillingCachedProductIdPreference)

    suspend fun setCachedProductId(newProductId: String?) {
        userPreferencesRepository.setValue(BillingCachedProductIdPreference, newProductId)
    }
}

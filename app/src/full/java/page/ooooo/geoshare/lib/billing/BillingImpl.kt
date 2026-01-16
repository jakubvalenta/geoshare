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

class BillingImpl(context: Context) : Billing(context) {

    private val product = BillingProduct("full_one_time", BillingProduct.Type.ONE_TIME)

    @StringRes
    override val appNameResId = R.string.app_name
    override val products = persistentListOf(product)
    override val features = persistentListOf(AutomationFeature)

    override val status = flowOf(BillingStatus.Purchased(product))
        .stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.WhileSubscribed(5000),
            BillingStatus.Loading(),
        )

    override val offers = flowOf(emptyList<Offer>())
        .stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    override val errorMessageResId = flowOf(null)
        .stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.WhileSubscribed(5000),
            null,
        )

    override fun startConnection() {}

    override fun endConnection() {}

    override suspend fun launchBillingFlow(activity: Activity, offerToken: String) {}
}

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

    private val plan = object : Plan {
        @StringRes
        override val appNameResId = R.string.app_name
        override val oneTimeProductId = "full_one_time"
        override val subscriptionProductId = "full_subscription"
        override val features = persistentListOf(AutomationFeature)
    }

    override val availablePlans = listOf(plan)

    override val status = flowOf(BillingStatus.Done(plan))
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = BillingStatus.Loading,
        )

    override val offers = flowOf(emptyList<Offer>())
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    override val errorMessageResId = flowOf(null)
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override fun startConnection() {}

    override fun endConnection() {}

    override suspend fun launchBillingFlow(activity: Activity, offerToken: String) {}
}

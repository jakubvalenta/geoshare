package page.ooooo.geoshare.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.billing.BillingOffers
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.Feature
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billing: Billing,
) : ViewModel() {

    val billingAppNameResId: Int = billing.appNameResId
    val billingMessage: StateFlow<Message?> = billing.message
    val billingFeatures: ImmutableList<Feature> = billing.features
    val billingOffers: StateFlow<BillingOffers> =
        billing.status
            .filter { it !is BillingStatus.Loading }
            .distinctUntilChanged()
            .map {
                billing.queryOffers()
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                BillingOffers.Loading(),
            )
    val billingRefundableDuration: Duration = billing.refundableDuration
    val billingStatus: StateFlow<BillingStatus> = billing.status

    // Methods

    fun consumePurchases() {
        billing.consumePurchases()
    }

    fun launchBillingFlow(activity: Activity, offerToken: String) {
        viewModelScope.launch {
            billing.launchBillingFlow(activity, offerToken)
        }
    }

    fun manageBillingProduct(activity: Activity, product: BillingProduct) {
        billing.manageProduct(activity, product)
    }

    fun dismissMessage() {
        billing.dismissMessage()
    }

    // Lifecycle

    fun onResume(activity: Activity) {
        billing.startConnection()
        viewModelScope.launch {
            billing.showInAppMessages(activity)
        }
    }

    override fun onCleared() {
        super.onCleared()
        billing.endConnection()
    }
}

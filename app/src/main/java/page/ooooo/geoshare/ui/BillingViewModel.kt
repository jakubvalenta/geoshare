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
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.billing.FeatureStatus
import page.ooooo.geoshare.lib.billing.Offer
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billing: Billing,
) : ViewModel() {

    val automationFeatureStatus: StateFlow<FeatureStatus> =
        billing.status
            .map { billingStatus ->
                when (billingStatus) {
                    is BillingStatus.Loading -> FeatureStatus.LOADING
                    is BillingStatus.Purchased if billing.features.contains(AutomationFeature) -> FeatureStatus.AVAILABLE
                    else -> FeatureStatus.NOT_AVAILABLE
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                FeatureStatus.LOADING,
            )
    val billingAppNameResId: Int = billing.appNameResId
    val billingMessage: StateFlow<Message?> = billing.message
    val billingFeatures: ImmutableList<Feature> = billing.features
    val billingOffers: StateFlow<List<Offer>> =
        billing.status
            .filter { it !is BillingStatus.Loading }
            .distinctUntilChanged()
            .map {
                billing.queryOffers()
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val billingRefundableDuration: Duration = billing.refundableDuration
    val billingStatus: StateFlow<BillingStatus> = billing.status

    // Methods

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

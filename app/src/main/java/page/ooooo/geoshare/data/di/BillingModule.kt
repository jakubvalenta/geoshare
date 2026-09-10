package page.ooooo.geoshare.data.di

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.billing.BillingImpl
import page.ooooo.geoshare.lib.billing.BillingOffers
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.billing.Feature
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBilling(@ApplicationContext context: Context): Billing =
        BillingImpl(context)
}

class FakeBilling(
    override val context: Context,
    override val features: ImmutableList<Feature> = persistentListOf(AutomationFeature, CustomLinkFeature),
    initialStatus: BillingStatus = BillingStatus.Loading(),
) : Billing {
    override val appNameResId: Int = R.string.app_name_pro
    override val products: ImmutableList<BillingProduct> = persistentListOf(
        BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
        BillingProduct("fake_subscription", BillingProduct.Type.SUBSCRIPTION),
    )
    override val refundableDuration: Duration = 48.hours
    override val message: StateFlow<Message?> = MutableStateFlow(null).asStateFlow()

    private val _status: MutableStateFlow<BillingStatus> = MutableStateFlow(initialStatus)
    override val status: StateFlow<BillingStatus> = _status.asStateFlow()

    fun setStatus(newStatus: BillingStatus) {
        _status.value = newStatus
    }

    override fun startConnection() {
        throw NotImplementedError()
    }

    override fun endConnection() {
        throw NotImplementedError()
    }

    override suspend fun queryOffers(): BillingOffers {
        throw NotImplementedError()
    }

    override fun consumePurchases() {
        throw NotImplementedError()
    }

    override suspend fun launchBillingFlow(activity: Activity, offerToken: String) {
        throw NotImplementedError()
    }

    override fun manageProduct(activity: Activity, product: BillingProduct) {
        throw NotImplementedError()
    }

    override suspend fun showInAppMessages(activity: Activity) {
        throw NotImplementedError()
    }

    override fun dismissMessage() {
        throw NotImplementedError()
    }
}

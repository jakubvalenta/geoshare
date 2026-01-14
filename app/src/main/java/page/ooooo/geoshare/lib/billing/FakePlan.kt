package page.ooooo.geoshare.lib.billing

import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R

object FakePlan : Plan {
    @StringRes
    override val appNameResId: Int = R.string.app_name_pro
    override val features = persistentListOf(AutomationFeature)
    override val products = persistentListOf(
        BillingProduct("fake_one_time_product", BillingProduct.Type.ONE_TIME),
        BillingProduct("fake_subscription_product", BillingProduct.Type.SUBSCRIPTION),
    )
}

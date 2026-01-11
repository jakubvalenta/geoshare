package page.ooooo.geoshare.lib.billing

import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R

object FakeEmptyPlan : Plan {
    @StringRes
    override val appNameResId: Int = R.string.app_name
    override val features = persistentListOf<Feature>()
    override val oneTimeProductId = "fake_one_time_product"
    override val subscriptionProductId = "fake_subscription_product"
}

object FakeFullPlan : Plan {
    @StringRes
    override val appNameResId: Int = R.string.app_name_pro
    override val features = persistentListOf(AutomationFeature)
    override val oneTimeProductId = "fake_one_time_product"
    override val subscriptionProductId = "fake_subscription_product"
}

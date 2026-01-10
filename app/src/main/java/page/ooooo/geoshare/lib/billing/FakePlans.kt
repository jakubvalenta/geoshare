package page.ooooo.geoshare.lib.billing

import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R

object FakeEmptyPlan : Plan {
    @StringRes
    override val appNameResId: Int = R.string.app_name
    override val features = persistentListOf<Feature>()
    override val oneTimeProductId = "fake_empty_one_time"
    override val subscriptionProductId = "fake_empty_subscription"
}

object FakeFullPlan : Plan {
    @StringRes
    override val appNameResId: Int = R.string.app_name_pro
    override val features = persistentListOf(AutomationFeature)
    override val oneTimeProductId = "fake_full_one_time"
    override val subscriptionProductId = "fake_full_subscription"
}

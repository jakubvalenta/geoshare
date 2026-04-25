package page.ooooo.geoshare.lib.billing

import androidx.annotation.StringRes
import page.ooooo.geoshare.R

sealed interface Feature {
    val titleResId: Int
    val descriptionResId: Int
}

object AutomationFeature : Feature {
    @StringRes
    override val titleResId = R.string.user_preferences_automation_title

    @StringRes
    override val descriptionResId = R.string.billing_feature_automation_description
}

object CustomLinkFeature : Feature {
    @StringRes
    override val titleResId = R.string.billing_feature_custom_link

    @StringRes
    override val descriptionResId = R.string.billing_feature_custom_link_description
}

package page.ooooo.geoshare.lib.billing

import androidx.annotation.StringRes
import page.ooooo.geoshare.R

sealed interface Feature {
    val titleResId: Int
    val descriptionResId: Int
    val itemsResIds: List<Int>
}

object AutomationFeature : Feature {
    @StringRes
    override val titleResId = R.string.billing_automation_title

    @StringRes
    override val descriptionResId = R.string.user_preferences_automation_description

    override val itemsResIds = listOf(
        R.string.billing_automation_open_app,
        R.string.billing_automation_navigate,
        R.string.billing_automation_copy,
    )
}

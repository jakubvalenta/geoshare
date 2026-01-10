package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
interface Plan {
    val appNameResId: Int
    val features: ImmutableList<Feature>
    val oneTimeProductId: String
    val subscriptionProductId: String

    fun hasProductId(productId: String): Boolean =
        oneTimeProductId == productId || subscriptionProductId == productId
}

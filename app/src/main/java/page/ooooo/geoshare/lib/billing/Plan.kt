package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
interface Plan {
    val appNameResId: Int
    val features: ImmutableList<Feature>
    val products: ImmutableList<BillingProduct>

    fun hasProductId(productId: String): Boolean = products.any { it.id == productId }
}

package page.ooooo.geoshare.lib.billing

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface Product {
    val id: String
    val features: ImmutableList<Feature>
    val paid: Boolean
}

object DefaultProduct : Product {
    override val id = "default"
    override val features = persistentListOf<Feature>()
    override val paid = false
}

object FullProduct : Product {
    override val id = "full"
    override val features = persistentListOf(
        AutomationFeature,
    )
    override val paid = false
}

object ProProduct : Product {
    override val id = "pro"
    override val features = persistentListOf(
        AutomationFeature,
    )
    override val paid = true
}

val allProducts = mapOf(
    DefaultProduct.id to DefaultProduct,
    FullProduct.id to FullProduct,
    ProProduct.id to ProProduct,
)

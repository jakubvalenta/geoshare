package page.ooooo.geoshare.lib.billing

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface Product {
    val id: String
    val features: ImmutableList<Feature>
}

object DefaultProduct : Product {
    override val id = "default"
    override val features = persistentListOf<Feature>()
}

object FullProduct : Product {
    override val id = "full"
    override val features = persistentListOf(
        AutomationFeature,
    )
}

object ProProduct : Product {
    override val id = "pro"
    override val features = persistentListOf(
        AutomationFeature,
    )
}

val allProducts = mapOf(
    DefaultProduct.id to DefaultProduct,
    FullProduct.id to FullProduct,
    ProProduct.id to ProProduct,
)

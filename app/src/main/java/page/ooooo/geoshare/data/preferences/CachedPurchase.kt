package page.ooooo.geoshare.data.preferences

import kotlinx.serialization.Serializable

@Serializable
data class CachedPurchase(val productId: String, val token: String)

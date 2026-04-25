package page.ooooo.geoshare.data.local.preferences

import kotlinx.serialization.Serializable

@Serializable
data class CachedPurchase(val productId: String, val token: String)

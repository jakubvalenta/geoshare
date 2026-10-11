package page.ooooo.geoshare.data.preferences

import kotlinx.serialization.Serializable

@Serializable
data class CachedServerToken(val token: String, val publicKey: String)

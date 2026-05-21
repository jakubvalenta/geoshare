package page.ooooo.geoshare.data.local.preferences

import kotlinx.serialization.Serializable

@Serializable
data class CachedApiToken(val token: String, val publicKey: String)

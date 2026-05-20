package page.ooooo.geoshare.data.local.preferences

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ApiConfig {
    val baseUrl: String

    @Serializable
    @SerialName("KEY")
    data class WithKeyAuth(override val baseUrl: String, val header: String, val key: String) : ApiConfig

    @Serializable
    @SerialName("ATTESTATION")
    data class WithAttestationAuth(override val baseUrl: String) : ApiConfig
}

package page.ooooo.geoshare.data.local.preferences

sealed interface Authentication {
    data class ApiKey(val header: String, val value: String) : Authentication
    class Attestation : Authentication
}

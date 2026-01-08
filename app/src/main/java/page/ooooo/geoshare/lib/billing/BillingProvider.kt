package page.ooooo.geoshare.lib.billing

interface BillingProvider {
    suspend fun queryStatus(): BillingStatus
}

package page.ooooo.geoshare.lib.billing

object BillingProviderImpl : BillingProvider {
    override suspend fun queryStatus() = BillingStatus.Done(FullProduct)
}

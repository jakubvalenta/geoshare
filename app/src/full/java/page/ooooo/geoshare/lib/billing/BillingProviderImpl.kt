package page.ooooo.geoshare.lib.billing

object BillingProviderImpl : BillingProvider {

    @Immutable
    object FullPlan : Plan {
        override val features = persistentListOf(AutomationFeature)
        override val oneTimeProductIds = persistentListOf()
        override val subscriptionProductIds = persistentListOf()
    }

    override suspend fun queryStatus() = BillingStatus.Done(FullPlan)
}

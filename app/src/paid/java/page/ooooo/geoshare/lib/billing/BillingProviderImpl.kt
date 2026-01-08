package page.ooooo.geoshare.lib.billing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BillingProviderImpl : BillingProvider {
    override suspend fun queryStatus() = withContext(Dispatchers.IO) {
        BillingStatus.Loading
    }
}

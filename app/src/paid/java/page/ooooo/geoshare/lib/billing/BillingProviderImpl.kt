package page.ooooo.geoshare.lib.billing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

object BillingProviderImpl : BillingProvider {
    override suspend fun queryStatus() = withContext(Dispatchers.IO) {
        delay(5.seconds)
        BillingStatus.Done(setOf(DefaultProduct, ProProduct).random())
    }
}

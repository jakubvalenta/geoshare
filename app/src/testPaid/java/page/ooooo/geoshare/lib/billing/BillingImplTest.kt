package page.ooooo.geoshare.lib.billing

import android.content.Context
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.FakeLog

class BillingImplTest {

    @Test
    fun status_whenPlanIsPurchased_isDoneWithPlan() = runTest {
        val context: Context = mock {}
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK)
                        .build(),
                    listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("pro_one_time")
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK)
                        .build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
    }

    @Test
    fun status_whenPlanIsNotPurchased_isDoneWithNull() {

    }
}

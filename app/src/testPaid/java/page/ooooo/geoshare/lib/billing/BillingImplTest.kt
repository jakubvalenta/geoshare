package page.ooooo.geoshare.lib.billing

import android.content.Context
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.FakeLog

class BillingImplTest {

    private val context: Context = mock {}

    @Test
    fun status_whenStartConnectionIsNotCalled_isLoading() {
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
        assertEquals(
            BillingStatus.Loading,
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenBillingSetupResponseIsError_isLoading() {
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
                        .setResponseCode(BillingResponseCode.ERROR)
                        .build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Loading,
            billingImpl.status.value,
        )
        assertEquals(
            R.string.billing_setup_error_unknown,
            billingImpl.errorMessageResId.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseIsError_isLoading() {
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.ERROR)
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
        assertEquals(
            BillingStatus.Loading,
            billingImpl.status.value,
        )
        assertEquals(
            R.string.billing_purchase_error_unknown,
            billingImpl.errorMessageResId.value,
        )
    }

    @Test
    fun status_whenPurchaseTypeIsPurchased_isDoneAndHasPlan() = runTest {
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
        assertEquals(
            BillingStatus.Done(billingImpl.availablePlans.first()),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchaseTypeIsPending_isDoneAndHasPlanNull() {
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK)
                        .build(),
                    listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PENDING
                            on { products } doReturn listOf("spam")
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
        assertEquals(
            BillingStatus.Done(null),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsKnownAndUnknownProducts_isDoneAndHasPlan() {
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK)
                        .build(),
                    listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("spam_1")
                        },
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("pro_subscription")
                        },
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("spam_2")
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
        assertEquals(
            BillingStatus.Done(billingImpl.availablePlans.first()),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseDoesNotContainKnownProduct_isDoneAndHasPlanNull() {
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK)
                        .build(),
                    listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("spam")
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
        assertEquals(
            BillingStatus.Done(null),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenFirstPurchasesResponseContainsKnowProductAndSecondOneDoesNot_isDoneAndHasPlan() {
        var responseIndex = 0
        val responseProductIds = mapOf(
            0 to "pro_one_time",
            1 to "spam",
        )
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK)
                        .build(),
                    listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf(responseProductIds[responseIndex++])
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
        assertEquals(
            BillingStatus.Done(billingImpl.availablePlans.first()),
            billingImpl.status.value,
        )
    }
}

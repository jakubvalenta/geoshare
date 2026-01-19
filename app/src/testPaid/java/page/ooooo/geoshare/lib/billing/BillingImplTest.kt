package page.ooooo.geoshare.lib.billing

import android.content.Context
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.Message
import kotlin.time.Duration.Companion.hours

@Suppress("EmptyMethod")
@OptIn(ExperimentalCoroutinesApi::class)
class BillingImplTest {

    private val context: Context = mock {
        on { getString(R.string.billing_purchase_error_unknown) } doReturn "Error when making a purchase"
        on { getString(R.string.billing_setup_error_unknown) } doReturn "Error when fetching purchases"
    }

    @Test
    fun status_whenStartConnectionIsNotCalled_isLoading() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("pro_one_time")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        assertTrue(billingImpl.status.value is BillingStatus.Loading)
    }

    @Test
    fun status_whenBillingSetupResponseIsError_isLoading() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("pro_one_time")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertTrue(billingImpl.status.value is BillingStatus.Loading)
        assertEquals(
            Message("Error when fetching purchases", isError = true),
            billingImpl.message.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseIsError_isLoading() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("pro_one_time")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertTrue(billingImpl.status.value is BillingStatus.Loading)
        assertEquals(
            Message("Error when making a purchase", isError = true),
            billingImpl.message.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsKnownProductWithStatePurchased_isPurchased() = runTest {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("pro_one_time")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "pro_one_time" },
                refundable = true
            ),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsProductWithOldPurchaseTime_isPurchasedAndNotRefundable() {
        val purchaseTimeValue = System.currentTimeMillis() - 49.hours.inWholeMilliseconds
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("pro_one_time")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "pro_one_time" },
                refundable = false
            ),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsKnownProductWithStatePending_isNotPurchased() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("spam")
                            on { purchaseState } doReturn Purchase.PurchaseState.PENDING
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertTrue(billingImpl.status.value is BillingStatus.NotPurchased)
    }

    @Test
    fun status_whenPurchasesResponseContainsKnownAndUnknownProducts_isPurchased() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("spam_1")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                        mock<Purchase> {
                            on { products } doReturn listOf("pro_subscription")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                        mock<Purchase> {
                            on { products } doReturn listOf("spam_2")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "pro_subscription" },
                refundable = true
            ),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseDoesNotContainKnownProduct_isNotPurchased() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("spam")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertTrue(billingImpl.status.value is BillingStatus.NotPurchased)
    }

    @Test
    fun status_whenFirstPurchasesResponseContainsKnownProductAndSecondResponseContainsUnknownProduct_isPurchased() {
        val purchaseTimeValue = System.currentTimeMillis()
        val responseProductIds = listOf(
            "pro_one_time",
            "spam",
        ).iterator()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf(responseProductIds.next())
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                        },
                    )
                )
            }

            override fun startConnection(p0: BillingClientStateListener) {
                p0.onBillingSetupFinished(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            }
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "pro_one_time" },
                refundable = true
            ),
            billingImpl.status.value,
        )
    }

    @Test
    fun queryOffers_whenStartConnectionIsNotCalled_isEmptyList() {
    }

    @Test
    fun queryOffers_whenProductDetailsResponseIsError_isEmptyList() {
    }

    @Test
    fun queryOffers_whenProductDetailsResponseThrowsException_isEmptyList() {
    }

    @Test
    fun queryOffers_whenProductDetailsResponseContainsNoProductDetails_isEmptyList() {
    }

    @Test
    fun queryOffers_whenProductDetailsResponseContainsNoOffers_isEmptyList() {
    }

    @Test
    fun queryOffers_whenProductDetailsResponseContainsSeveralProductDetailsWithSeveralOffers_isListOfAllOffers() =
        runTest {
            val responseProductDetailsLists = listOf(
                listOf(
                    mock<ProductDetails.OneTimePurchaseOfferDetails> {
                        on { offerToken } doReturn "offer_one_time_details_list"
                        on { formattedPrice } doReturn "$1"
                    }.let { oneTimePurchaseOfferDetails ->
                        mock {
                            on { productId } doReturn "product_1"
                            on { oneTimePurchaseOfferDetailsList } doReturn listOf(oneTimePurchaseOfferDetails)
                        }
                    },
                    mock<ProductDetails.OneTimePurchaseOfferDetails> {
                        on { offerToken } doReturn "offer_one_time_details"
                        on { formattedPrice } doReturn "$3.33"
                    }.let { oneTimePurchaseOfferDetailsParam ->
                        mock<ProductDetails> {
                            on { productId } doReturn "product_2"
                            on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                        }
                    },
                ),
                listOf(
                    mock<ProductDetails.PricingPhase> {
                        on { billingPeriod } doReturn "P1M"
                        on { formattedPrice } doReturn "$99.90"
                        on { recurrenceMode } doReturn ProductDetails.RecurrenceMode.INFINITE_RECURRING
                    }.let { pricingPhase ->
                        mock<ProductDetails.PricingPhases> {
                            on { pricingPhaseList } doReturn listOf(pricingPhase)
                        }
                    }.let { pricingPhasesParam ->
                        mock<ProductDetails.SubscriptionOfferDetails> {
                            on { offerToken } doReturn "offer_subscription_details"
                            on { pricingPhases } doReturn pricingPhasesParam
                        }
                    }.let { subscriptionOfferDetailsParam ->
                        mock {
                            on { productId } doReturn "product_2"
                            on { subscriptionOfferDetails } doReturn listOf(subscriptionOfferDetailsParam)
                        }
                    },
                ),
            ).iterator()
            val billingClient = object : FakeBillingClient() {
                override fun queryProductDetailsAsync(
                    p0: QueryProductDetailsParams,
                    p1: ProductDetailsResponseListener,
                ) {
                    p1.onProductDetailsResponse(
                        BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(),
                        QueryProductDetailsResult.create(
                            responseProductDetailsLists.next(),
                            emptyList(),
                        )
                    )
                }

                override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                }

                override fun startConnection(p0: BillingClientStateListener) {}
            }
            val billingClientBuilder = FakeBillingClientBuilder(billingClient)
            val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
            billingImpl.startConnection()
            assertEquals(
                listOf(
                    Offer("offer_one_time_details_list", "$1", Offer.Period.ONE_TIME, "product_1"),
                    Offer("offer_one_time_details", "$3.33", Offer.Period.ONE_TIME, "product_2"),
                    Offer("offer_subscription_details", "$99.90", Offer.Period.MONTHLY, "product_2"),
                ),
                billingImpl.queryOffers(),
            )
        }

    @Test
    fun queryOffers_whenProductDetailsResponseContainsWeeklyOrFiniteSubscriptions_isListOfOnlyMonthlyInfiniteOffers() =
        runTest {
            val responseProductDetailsLists = listOf(
                emptyList(),
                listOf(
                    mock<ProductDetails.PricingPhase> {
                        on { billingPeriod } doReturn "P11"
                        on { formattedPrice } doReturn "$1"
                        on { recurrenceMode } doReturn ProductDetails.RecurrenceMode.INFINITE_RECURRING
                    }.let { pricingPhase ->
                        mock<ProductDetails.PricingPhases> {
                            on { pricingPhaseList } doReturn listOf(pricingPhase)
                        }
                    }.let { pricingPhasesParam ->
                        mock<ProductDetails.SubscriptionOfferDetails> {
                            on { offerToken } doReturn "offer_weekly_infinite"
                            on { pricingPhases } doReturn pricingPhasesParam
                        }
                    }.let { subscriptionOfferDetailsParam ->
                        mock {
                            on { productId } doReturn "product_1"
                            on { subscriptionOfferDetails } doReturn listOf(subscriptionOfferDetailsParam)
                        }
                    },
                    mock<ProductDetails.PricingPhase> {
                        on { billingPeriod } doReturn "P1M"
                        on { formattedPrice } doReturn "$2"
                        on { recurrenceMode } doReturn ProductDetails.RecurrenceMode.FINITE_RECURRING
                    }.let { pricingPhase ->
                        mock<ProductDetails.PricingPhases> {
                            on { pricingPhaseList } doReturn listOf(pricingPhase)
                        }
                    }.let { pricingPhasesParam ->
                        mock<ProductDetails.SubscriptionOfferDetails> {
                            on { offerToken } doReturn "offer_monthly_finite"
                            on { pricingPhases } doReturn pricingPhasesParam
                        }
                    }.let { subscriptionOfferDetailsParam ->
                        mock {
                            on { productId } doReturn "product_1"
                            on { subscriptionOfferDetails } doReturn listOf(subscriptionOfferDetailsParam)
                        }
                    },
                    mock<ProductDetails.PricingPhase> {
                        on { billingPeriod } doReturn "P1M"
                        on { formattedPrice } doReturn "$3"
                        on { recurrenceMode } doReturn ProductDetails.RecurrenceMode.INFINITE_RECURRING
                    }.let { pricingPhase ->
                        mock<ProductDetails.PricingPhases> {
                            on { pricingPhaseList } doReturn listOf(pricingPhase)
                        }
                    }.let { pricingPhasesParam ->
                        mock<ProductDetails.SubscriptionOfferDetails> {
                            on { offerToken } doReturn "offer_monthly_infinite"
                            on { pricingPhases } doReturn pricingPhasesParam
                        }
                    }.let { subscriptionOfferDetailsParam ->
                        mock<ProductDetails> {
                            on { productId } doReturn "product_1"
                            on { subscriptionOfferDetails } doReturn listOf(subscriptionOfferDetailsParam)
                        }
                    },
                ),
            ).iterator()
            val billingClient = object : FakeBillingClient() {
                override fun queryProductDetailsAsync(
                    p0: QueryProductDetailsParams,
                    p1: ProductDetailsResponseListener,
                ) {
                    p1.onProductDetailsResponse(
                        BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(),
                        QueryProductDetailsResult.create(
                            responseProductDetailsLists.next(),
                            emptyList(),
                        )
                    )
                }

                override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                }

                override fun startConnection(p0: BillingClientStateListener) {}
            }
            val billingClientBuilder = FakeBillingClientBuilder(billingClient)
            val billingImpl = BillingImpl(context, billingClientBuilder, FakeLog)
            billingImpl.startConnection()
            assertEquals(
                listOf(
                    Offer("offer_monthly_infinite", "$3", Offer.Period.MONTHLY, "product_1"),
                ),
                billingImpl.queryOffers(),
            )
        }
}

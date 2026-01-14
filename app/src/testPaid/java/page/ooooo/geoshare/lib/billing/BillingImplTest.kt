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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.FakeLog

@OptIn(ExperimentalCoroutinesApi::class)
class BillingImplTest {

    private val context: Context = mock {}

    @Test
    fun status_whenStartConnectionIsNotCalled_isLoading() {
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("pro_one_time")
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
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("pro_one_time")
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
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR).build(), listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("pro_one_time")
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
            BillingStatus.Loading,
            billingImpl.status.value,
        )
        assertEquals(
            R.string.billing_purchase_error_unknown,
            billingImpl.errorMessageResId.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsKnownProductWithTypePurchased_isDoneAndHasPlan() = runTest {
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("pro_one_time")
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
            BillingStatus.Done(billingImpl.availablePlans.first()),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsKnownProductWithTypePending_isDoneAndHasPlanNull() {
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PENDING
                            on { products } doReturn listOf("spam")
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
            BillingStatus.Done(null),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsKnownAndUnknownProducts_isDoneAndHasPlan() {
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
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
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
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
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf("spam")
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
            BillingStatus.Done(null),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenFirstPurchasesResponseContainsKnownProductAndSecondResponseContainsUnknownProduct_isDoneAndHasPlan() {
        val responseProductIds = listOf(
            "pro_one_time",
            "spam",
        ).iterator()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { products } doReturn listOf(responseProductIds.next())
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
            BillingStatus.Done(billingImpl.availablePlans.first()),
            billingImpl.status.value,
        )
    }

    @Test
    fun offers_whenStartConnectionIsNotCalled_isEmptyList() {
    }

    @Test
    fun offers_whenProductDetailsResponseIsError_isEmptyList() {
    }

    @Test
    fun offers_whenProductDetailsResponseThrowsException_isEmptyList() {
    }

    @Test
    fun offers_whenProductDetailsResponseContainsNoProductDetails_isEmptyList() {
    }

    @Test
    fun offers_whenProductDetailsResponseContainsNoOffers_isEmptyList() {
    }

    @Test
    fun offers_whenProductDetailsResponseContainsSeveralProductDetailsWithSeveralOffers_isListOfAllOffers() = runTest {
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
            override fun queryProductDetailsAsync(p0: QueryProductDetailsParams, p1: ProductDetailsResponseListener) {
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
            billingImpl.offers.first(),
        )
    }

    @Test
    fun offers_whenProductDetailsResponseContainsWeeklyOrFiniteSubscriptions_isListOfOnlyMonthlyInfiniteOffers() =
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
                billingImpl.offers.first(),
            )
        }
}

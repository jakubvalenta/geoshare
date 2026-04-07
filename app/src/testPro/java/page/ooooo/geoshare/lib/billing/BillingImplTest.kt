package page.ooooo.geoshare.lib.billing

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResponseListener
import com.android.billingclient.api.InAppMessageResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.Message
import kotlin.time.Duration.Companion.hours

@Suppress("EmptyMethod")
@OptIn(ExperimentalCoroutinesApi::class)
class BillingImplTest {

    private val resources: Resources = mock {
        on { getString(R.string.app_name_pro) } doReturn "GeoShare Pro"
        on { getString(R.string.billing_offers_error) } doReturn "Failed to fetch offers"
        on { getString(R.string.billing_purchase_error_cancelled) } doReturn "Purchase cancelled"
        on { getString(R.string.billing_purchase_error_unknown) } doReturn "Failed to make the purchase"
        on { getString(eq(R.string.billing_purchase_success), any()) } doReturn "Thanks for buying GeoShare Pro"
        on { getString(R.string.billing_setup_error_unknown) } doReturn "Failed to fetch purchases"
    }
    private val context: Context = mock()

    @Test
    fun status_whenStartConnectionIsNotCalled_isLoading() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
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
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertTrue(billingImpl.status.value is BillingStatus.Loading)
        assertEquals(
            Message("Failed to fetch purchases", isError = true),
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
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertTrue(billingImpl.status.value is BillingStatus.Loading)
        assertEquals(
            Message("Failed to make the purchase", isError = true),
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
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "test_lifetime" },
                expired = false,
                refundable = true,
            ),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsProductWithAutoRenewingFalse_isPurchasedAndExpired() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("test_monthly")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
                            on { isAutoRenewing } doReturn false
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "test_monthly" },
                expired = true,
                refundable = true,
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
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "test_lifetime" },
                expired = false,
                refundable = false,
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
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.NotPurchased(pending = true),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenPurchasesResponseContainsProductWithStateUnspecified_isNotPurchased() {
        val purchaseTimeValue = System.currentTimeMillis()
        val billingClient = object : FakeBillingClient() {
            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.UNSPECIFIED_STATE
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.NotPurchased(pending = false),
            billingImpl.status.value,
        )
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
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
                        },
                        mock<Purchase> {
                            on { products } doReturn listOf("test_monthly")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
                            on { isAutoRenewing } doReturn true
                        },
                        mock<Purchase> {
                            on { products } doReturn listOf("spam_2")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "test_monthly" },
                expired = false,
                refundable = true,
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
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.NotPurchased(pending = false),
            billingImpl.status.value,
        )
    }

    @Test
    fun status_whenFirstPurchasesResponseContainsKnownProductAndSecondResponseContainsUnknownProduct_isPurchased() {
        val purchaseTimeValue = System.currentTimeMillis()
        val responseProductIds = listOf(
            "test_lifetime",
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
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "test_lifetime" },
                expired = false,
                refundable = true,
            ),
            billingImpl.status.value,
        )
    }

    @Test
    fun queryOffers_whenProductDetailsResponseThrowsException_returnsEmptyList() = runTest {
        val billingClient = object : FakeBillingClient() {
            override fun queryProductDetailsAsync(
                p0: QueryProductDetailsParams,
                p1: ProductDetailsResponseListener,
            ) {
                throw Exception()
            }

            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
            }

            override fun startConnection(p0: BillingClientStateListener) {}
        }
        val billingClientBuilder = FakeBillingClientBuilder(billingClient)
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.startConnection()
        assertEquals(
            BillingOffers.Done(persistentListOf()),
            billingImpl.queryOffers(),
        )
        assertEquals(
            Message("Failed to fetch offers", isError = true),
            billingImpl.message.value,
        )
    }

    @Test
    fun queryOffers_whenProductDetailsResponseContainsNoProductDetails_returnsEmptyList() = runTest {
        val responseProductDetailsLists = listOf(
            emptyList<ProductDetails>(),
            emptyList(),
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        assertEquals(
            BillingOffers.Done(persistentListOf()),
            billingImpl.queryOffers(),
        )
        assertNull(billingImpl.message.value)
    }

    @Test
    fun queryOffers_whenProductDetailsResponseContainsNoOffers_returnsEmptyList() = runTest {
        val responseProductDetailsLists = listOf(
            listOf(
                mock<ProductDetails> {
                    on { productId } doReturn "test_lifetime"
                    on { oneTimePurchaseOfferDetailsList } doReturn emptyList()
                },
                mock<ProductDetails> {
                    on { productId } doReturn "test_monthly"
                    on { oneTimePurchaseOfferDetails } doReturn null
                },
            ),
            listOf(
                mock<ProductDetails> {
                    on { productId } doReturn "test_monthly"
                    on { subscriptionOfferDetails } doReturn null
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
            ),
            resources = resources,
            log = FakeLog,
        )
        assertEquals(
            BillingOffers.Done(persistentListOf()),
            billingImpl.queryOffers(),
        )
        assertNull(billingImpl.message.value)
    }

    @Test
    fun queryOffers_whenBillingProductsContainsOnlyDonation_returnsEmptyList() = runTest {
        val responseProductDetailsLists = listOf(
            listOf(
                mock<ProductDetails.OneTimePurchaseOfferDetails> {
                    on { offerToken } doReturn "offer_lifetime_details"
                    on { formattedPrice } doReturn "$3.33"
                }.let { oneTimePurchaseOfferDetailsParam ->
                    mock<ProductDetails> {
                        on { productId } doReturn "test_donation"
                        on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                    }
                },
            ),
            emptyList(),
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_donation", BillingProduct.Type.DONATION),
            ),
            resources = resources,
            log = FakeLog,
        )
        assertEquals(
            BillingOffers.Done(persistentListOf()),
            billingImpl.queryOffers(),
        )
        assertNull(billingImpl.message.value)
    }

    @Test
    fun queryOffers_whenProductDetailsResponseContainsSeveralProductDetailsWithSeveralOffers_returnsListOfAllOffers() =
        runTest {
            val responseProductDetailsLists = listOf(
                listOf(
                    mock<ProductDetails.OneTimePurchaseOfferDetails> {
                        on { offerToken } doReturn "offer_lifetime_details_list"
                        on { formattedPrice } doReturn "$1"
                    }.let { oneTimePurchaseOfferDetails ->
                        mock<ProductDetails> {
                            on { productId } doReturn "test_lifetime"
                            on { oneTimePurchaseOfferDetailsList } doReturn listOf(oneTimePurchaseOfferDetails)
                        }
                    },
                    mock<ProductDetails.OneTimePurchaseOfferDetails> {
                        on { offerToken } doReturn "offer_lifetime_details"
                        on { formattedPrice } doReturn "$3.33"
                    }.let { oneTimePurchaseOfferDetailsParam ->
                        mock<ProductDetails> {
                            on { productId } doReturn "test_monthly"
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
                            on { offerToken } doReturn "offer_monthly_details"
                            on { pricingPhases } doReturn pricingPhasesParam
                        }
                    }.let { subscriptionOfferDetailsParam ->
                        mock<ProductDetails> {
                            on { productId } doReturn "test_monthly"
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
            val billingImpl = BillingImpl(
                context,
                billingClientBuilder,
                products = persistentListOf(
                    BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                    BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
                ),
                resources = resources,
                log = FakeLog,
            )
            assertEquals(
                BillingOffers.Done(
                    persistentListOf(
                        Offer("offer_lifetime_details_list", "$1", Offer.Period.ONE_TIME, "test_lifetime"),
                        Offer("offer_lifetime_details", "$3.33", Offer.Period.ONE_TIME, "test_monthly"),
                        Offer("offer_monthly_details", "$99.90", Offer.Period.MONTHLY, "test_monthly"),
                    )
                ),
                billingImpl.queryOffers(),
            )
        }

    @Test
    fun queryOffers_whenProductDetailsResponseContainsWeeklyOrFiniteSubscriptions_returnsListOfOnlyMonthlyInfiniteOffers() =
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
                        mock<ProductDetails> {
                            on { productId } doReturn "test_lifetime"
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
                        mock<ProductDetails> {
                            on { productId } doReturn "test_lifetime"
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
                            on { productId } doReturn "test_lifetime"
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
            val billingImpl = BillingImpl(
                context,
                billingClientBuilder,
                products = persistentListOf(
                    BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                    BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
                ),
                resources = resources,
                log = FakeLog,
            )
            assertEquals(
                BillingOffers.Done(
                    persistentListOf(
                        Offer("offer_monthly_infinite", "$3", Offer.Period.MONTHLY, "test_lifetime"),
                    )
                ),
                billingImpl.queryOffers(),
            )
        }

    @Test
    fun launchBillingFlow_whenOfferTokenIsNotKnown_setsErrorMessage() = runTest {
        val responseProductDetailsLists = listOf(
            listOf(
                mock<ProductDetails.OneTimePurchaseOfferDetails> {
                    on { offerToken } doReturn "offer_lifetime_details"
                    on { formattedPrice } doReturn "$3.33"
                }.let { oneTimePurchaseOfferDetailsParam ->
                    mock<ProductDetails> {
                        on { productId } doReturn "test_lifetime"
                        on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                    }
                },
            ),
            emptyList(),
        ).iterator()
        val billingClient = object : FakeBillingClient() {
            override fun acknowledgePurchase(
                p0: AcknowledgePurchaseParams,
                p1: AcknowledgePurchaseResponseListener,
            ) {
            }

            override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
                purchasesUpdatedListener?.onPurchasesUpdated(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn System.currentTimeMillis()
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
                        })
                )
                return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
            }

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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
            ),
            resources = resources,
            log = FakeLog,
        )
        billingImpl.launchBillingFlow(mock(), "spam")
        assertTrue(billingImpl.status.value is BillingStatus.Loading)
        assertEquals(
            Message("Failed to make the purchase", isError = true),
            billingImpl.message.value,
        )
    }

    @Test
    fun launchBillingFlow_whenOfferTokenIsKnownAndLaunchResponseIsError_setsErrorMessage() = runTest {
        val responseProductDetailsLists = listOf(
            listOf(
                mock<ProductDetails.OneTimePurchaseOfferDetails> {
                    on { offerToken } doReturn "offer_lifetime_details"
                    on { formattedPrice } doReturn "$3.33"
                }.let { oneTimePurchaseOfferDetailsParam ->
                    mock<ProductDetails> {
                        on { productId } doReturn "test_lifetime"
                        on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                        on { zza() } doReturn "test_package_name"
                    }
                },
            ),
            emptyList(),
        ).iterator()
        val billingClient = object : FakeBillingClient() {
            override fun acknowledgePurchase(
                p0: AcknowledgePurchaseParams,
                p1: AcknowledgePurchaseResponseListener,
            ) {
            }

            override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
                purchasesUpdatedListener?.onPurchasesUpdated(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn System.currentTimeMillis()
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
                        })
                )
                return BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR).build()
            }

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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
            ),
            productDetailsParamsBuilder = { FakeProductDetailsParamsBuilder() },
            billingFlowParamsBuilder = { FakeBillingFlowParamsBuilder() },
            resources = resources,
            log = FakeLog,
        )
        billingImpl.launchBillingFlow(mock(), "offer_lifetime_details")
        assertTrue(billingImpl.status.value is BillingStatus.Purchased)
        assertEquals(
            Message("Failed to make the purchase", isError = true),
            billingImpl.message.value,
        )
    }

    @Test
    fun launchBillingFlow_whenOfferTokenIsKnownAndPurchasesUpdatedResponseIsUserCancelled_setsErrorMessage() = runTest {
        val responseProductDetailsLists = listOf(
            listOf(
                mock<ProductDetails.OneTimePurchaseOfferDetails> {
                    on { offerToken } doReturn "offer_lifetime_details"
                    on { formattedPrice } doReturn "$3.33"
                }.let { oneTimePurchaseOfferDetailsParam ->
                    mock<ProductDetails> {
                        on { productId } doReturn "test_lifetime"
                        on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                    }
                },
            ),
            emptyList(),
        ).iterator()
        val billingClient = object : FakeBillingClient() {
            override fun acknowledgePurchase(
                p0: AcknowledgePurchaseParams,
                p1: AcknowledgePurchaseResponseListener,
            ) {
            }

            override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
                purchasesUpdatedListener?.onPurchasesUpdated(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.USER_CANCELED).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn System.currentTimeMillis()
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
                        })
                )
                return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
            }

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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
            ),
            productDetailsParamsBuilder = { FakeProductDetailsParamsBuilder() },
            billingFlowParamsBuilder = { FakeBillingFlowParamsBuilder() },
            resources = resources,
            log = FakeLog,
        )
        billingImpl.launchBillingFlow(mock(), "offer_lifetime_details")
        assertTrue(billingImpl.status.value is BillingStatus.Loading)
        assertEquals(
            Message("Purchase cancelled", isError = true),
            billingImpl.message.value,
        )
    }

    @Test
    fun launchBillingFlow_whenOfferTokenIsKnownAndPurchasesUpdatedResponseIsError_setsErrorMessage() = runTest {
        val responseProductDetailsLists = listOf(
            listOf(
                mock<ProductDetails.OneTimePurchaseOfferDetails> {
                    on { offerToken } doReturn "offer_lifetime_details"
                    on { formattedPrice } doReturn "$3.33"
                }.let { oneTimePurchaseOfferDetailsParam ->
                    mock<ProductDetails> {
                        on { productId } doReturn "test_lifetime"
                        on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                    }
                },
            ),
            emptyList(),
        ).iterator()
        val billingClient = object : FakeBillingClient() {
            override fun acknowledgePurchase(
                p0: AcknowledgePurchaseParams,
                p1: AcknowledgePurchaseResponseListener,
            ) {
            }

            override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
                purchasesUpdatedListener?.onPurchasesUpdated(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.ERROR).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf("test_lifetime")
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn System.currentTimeMillis()
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
                        })
                )
                return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
            }

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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
            ),
            productDetailsParamsBuilder = { FakeProductDetailsParamsBuilder() },
            billingFlowParamsBuilder = { FakeBillingFlowParamsBuilder() },
            resources = resources,
            log = FakeLog,
        )
        billingImpl.launchBillingFlow(mock(), "offer_lifetime_details")
        assertTrue(billingImpl.status.value is BillingStatus.Loading)
        assertEquals(
            Message("Failed to make the purchase", isError = true),
            billingImpl.message.value,
        )
    }

    @Test
    fun launchBillingFlow_whenOfferTokenIsKnownAndPurchasesUpdatedResponseIsOk_acknowledgesEachPurchasedPurchase() =
        runTest {
            val responseProductDetailsLists = listOf(
                listOf(
                    mock<ProductDetails.OneTimePurchaseOfferDetails> {
                        on { offerToken } doReturn "offer_lifetime_details"
                        on { formattedPrice } doReturn "$3.33"
                    }.let { oneTimePurchaseOfferDetailsParam ->
                        mock<ProductDetails> {
                            on { productId } doReturn "test_lifetime"
                            on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                        }
                    },
                ),
                emptyList(),
            ).iterator()
            val acknowledgedPurchaseParamsList = mutableListOf<AcknowledgePurchaseParams>()
            val billingClient = object : FakeBillingClient() {
                override fun acknowledgePurchase(
                    p0: AcknowledgePurchaseParams,
                    p1: AcknowledgePurchaseResponseListener,
                ) {
                    acknowledgedPurchaseParamsList.add(p0)
                }

                override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
                    purchasesUpdatedListener?.onPurchasesUpdated(
                        BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                            mock<Purchase> {
                                on { products } doReturn listOf("test_lifetime")
                                on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                                on { purchaseTime } doReturn System.currentTimeMillis()
                                on { purchaseToken } doReturn "test_purchase_token_purchased_1"
                                on { isAcknowledged } doReturn false
                            },
                            mock<Purchase> {
                                on { products } doReturn listOf("test_lifetime")
                                on { purchaseState } doReturn Purchase.PurchaseState.PENDING
                                on { purchaseTime } doReturn System.currentTimeMillis()
                                on { purchaseToken } doReturn "test_purchase_token_pending"
                                on { isAcknowledged } doReturn false
                            },
                            mock<Purchase> {
                                on { products } doReturn listOf("test_lifetime")
                                on { purchaseState } doReturn Purchase.PurchaseState.UNSPECIFIED_STATE
                                on { purchaseTime } doReturn System.currentTimeMillis()
                                on { purchaseToken } doReturn "test_purchase_token_unspecified_state"
                                on { isAcknowledged } doReturn false
                            },
                            mock<Purchase> {
                                on { products } doReturn listOf("test_lifetime")
                                on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                                on { purchaseTime } doReturn System.currentTimeMillis()
                                on { purchaseToken } doReturn "test_purchase_token_purchased_2"
                                on { isAcknowledged } doReturn false
                            },
                            mock<Purchase> {
                                on { products } doReturn listOf("test_lifetime")
                                on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                                on { purchaseTime } doReturn System.currentTimeMillis()
                                on { purchaseToken } doReturn "test_purchase_token_purchased_acknowledged"
                                on { isAcknowledged } doReturn true
                            },
                        )
                    )
                    return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                }

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
            val billingImpl = BillingImpl(
                context,
                billingClientBuilder,
                products = persistentListOf(
                    BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                ),
                productDetailsParamsBuilder = { FakeProductDetailsParamsBuilder() },
                billingFlowParamsBuilder = { FakeBillingFlowParamsBuilder() },
                resources = resources,
                log = FakeLog,
            )
            billingImpl.launchBillingFlow(mock(), "offer_lifetime_details")
            assertEquals(
                listOf("test_purchase_token_purchased_1", "test_purchase_token_purchased_2"),
                acknowledgedPurchaseParamsList.map { it.purchaseToken },
            )
            assertTrue(billingImpl.status.value is BillingStatus.Purchased)
            assertEquals(
                Message("Thanks for buying GeoShare Pro"),
                billingImpl.message.value,
            )
        }

    @Test
    fun launchBillingFlow_whenOfferTokenIsKnownAndPurchasesUpdatedResponseIsOkAndProductIsPurchased_setsStatusToPurchased() =
        runTest {
            val responseProductDetailsLists = listOf(
                listOf(
                    mock<ProductDetails.OneTimePurchaseOfferDetails> {
                        on { offerToken } doReturn "offer_lifetime_details"
                        on { formattedPrice } doReturn "$3.33"
                    }.let { oneTimePurchaseOfferDetailsParam ->
                        mock<ProductDetails> {
                            on { productId } doReturn "test_lifetime"
                            on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                        }
                    },
                ),
                emptyList(),
            ).iterator()
            val acknowledgedPurchaseParamsList = mutableListOf<AcknowledgePurchaseParams>()
            val billingClient = object : FakeBillingClient() {

                override fun acknowledgePurchase(
                    p0: AcknowledgePurchaseParams,
                    p1: AcknowledgePurchaseResponseListener,
                ) {
                    acknowledgedPurchaseParamsList.add(p0)
                }

                override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
                    purchasesUpdatedListener?.onPurchasesUpdated(
                        BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                            mock<Purchase> {
                                on { products } doReturn listOf("test_lifetime")
                                on { purchaseToken } doReturn "test_purchase_token_purchased"
                                on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                                on { purchaseTime } doReturn System.currentTimeMillis()
                                on { isAcknowledged } doReturn false
                            },
                        )
                    )
                    return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                }

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
            val billingImpl = BillingImpl(
                context,
                billingClientBuilder,
                products = persistentListOf(
                    BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                ),
                productDetailsParamsBuilder = { FakeProductDetailsParamsBuilder() },
                billingFlowParamsBuilder = { FakeBillingFlowParamsBuilder() },
                resources = resources,
                log = FakeLog,
            )
            billingImpl.launchBillingFlow(mock(), "offer_lifetime_details")
            assertEquals(
                BillingStatus.Purchased(
                    BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                ),
                billingImpl.status.value,
            )
            assertEquals(
                Message("Thanks for buying GeoShare Pro"),
                billingImpl.message.value,
            )
        }

    @Test
    fun launchBillingFlow_whenOfferTokenIsKnownAndPurchasesUpdatedResponseIsOkAndProductIsPurchasedAndSubscriptionAndOld_setsStatusToPurchasedAndSubscriptionAndNotRefundable() =
        runTest {
            val responseProductDetailsLists = listOf(
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
                            on { offerToken } doReturn "offer_monthly_details"
                            on { pricingPhases } doReturn pricingPhasesParam
                        }
                    }.let { subscriptionOfferDetailsParam ->
                        mock<ProductDetails> {
                            on { productId } doReturn "test_monthly"
                            on { subscriptionOfferDetails } doReturn listOf(subscriptionOfferDetailsParam)
                        }
                    },
                ),
                emptyList(),
            ).iterator()
            val acknowledgedPurchaseParamsList = mutableListOf<AcknowledgePurchaseParams>()
            val billingClient = object : FakeBillingClient() {
                override fun acknowledgePurchase(
                    p0: AcknowledgePurchaseParams,
                    p1: AcknowledgePurchaseResponseListener,
                ) {
                    acknowledgedPurchaseParamsList.add(p0)
                }

                override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
                    purchasesUpdatedListener?.onPurchasesUpdated(
                        BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                            mock<Purchase> {
                                on { products } doReturn listOf("test_monthly")
                                on { purchaseToken } doReturn "test_purchase_token_purchased"
                                on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                                on { purchaseTime } doReturn System.currentTimeMillis() - 49.hours.inWholeMilliseconds
                                on { isAcknowledged } doReturn false
                                on { isAutoRenewing } doReturn true
                            },
                        )
                    )
                    return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                }

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
            val billingImpl = BillingImpl(
                context,
                billingClientBuilder,
                products = persistentListOf(
                    BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
                ),
                productDetailsParamsBuilder = { FakeProductDetailsParamsBuilder() },
                billingFlowParamsBuilder = { FakeBillingFlowParamsBuilder() },
                resources = resources,
                log = FakeLog,
            )
            billingImpl.launchBillingFlow(mock(), "offer_monthly_details")
            assertEquals(
                BillingStatus.Purchased(
                    BillingProduct("test_monthly", BillingProduct.Type.SUBSCRIPTION),
                    expired = false,
                    refundable = false,
                ),
                billingImpl.status.value,
            )
            assertEquals(
                Message("Thanks for buying GeoShare Pro"),
                billingImpl.message.value,
            )
        }

    @Test
    fun launchBillingFlow_whenOfferTokenIsKnownAndPurchasesUpdatedResponseIsOkAndProductIsNotPurchased_setsStatusToNotPurchased() =
        runTest {
            val responseProductDetailsLists = listOf(
                listOf(
                    mock<ProductDetails.OneTimePurchaseOfferDetails> {
                        on { offerToken } doReturn "offer_lifetime_details"
                        on { formattedPrice } doReturn "$3.33"
                    }.let { oneTimePurchaseOfferDetailsParam ->
                        mock<ProductDetails> {
                            on { productId } doReturn "test_lifetime"
                            on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetailsParam
                        }
                    },
                ),
                emptyList(),
            ).iterator()
            val acknowledgedPurchaseParamsList = mutableListOf<AcknowledgePurchaseParams>()
            val billingClient = object : FakeBillingClient() {
                override fun acknowledgePurchase(
                    p0: AcknowledgePurchaseParams,
                    p1: AcknowledgePurchaseResponseListener,
                ) {
                    acknowledgedPurchaseParamsList.add(p0)
                }

                override fun launchBillingFlow(p0: Activity, p1: BillingFlowParams): BillingResult {
                    purchasesUpdatedListener?.onPurchasesUpdated(
                        BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                            mock<Purchase> {
                                on { products } doReturn listOf("spam")
                                on { purchaseToken } doReturn "test_purchase_token_purchased"
                                on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                                on { purchaseTime } doReturn System.currentTimeMillis()
                                on { isAcknowledged } doReturn false
                            },
                        )
                    )
                    return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                }

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
            val billingImpl = BillingImpl(
                context,
                billingClientBuilder,
                products = persistentListOf(
                    BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
                ),
                productDetailsParamsBuilder = { FakeProductDetailsParamsBuilder() },
                billingFlowParamsBuilder = { FakeBillingFlowParamsBuilder() },
                resources = resources,
                log = FakeLog,
            )
            billingImpl.launchBillingFlow(mock(), "offer_lifetime_details")
            assertEquals(
                BillingStatus.NotPurchased(pending = false),
                billingImpl.status.value,
            )
            assertNull(billingImpl.message.value)
        }

    @Test
    fun showInAppMessages_waitsForConnection_andWhenResponseIsNotActionNeeded_doesNothing() = runTest {
        val purchaseTimeValue = System.currentTimeMillis()
        val purchasedProductIds = listOf(
            "spam_lifetime",
            "spam_monthly",
            "test_lifetime",
            "test_monthly",
        ).iterator()
        val billingClient = object : FakeBillingClient() {
            override fun showInAppMessages(
                p0: Activity,
                p1: InAppMessageParams,
                p2: InAppMessageResponseListener,
            ): BillingResult {
                p2.onInAppMessageResponse(
                    InAppMessageResult(
                        InAppMessageResult.InAppMessageResponseCode.NO_ACTION_NEEDED,
                        null,
                    )
                )
                return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
            }

            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf(purchasedProductIds.next())
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
            ),
            resources = resources,
            log = FakeLog,
        )

        // Show in-app messages. It waits for connection, so status remains Loading
        val job = launch {
            billingImpl.showInAppMessages(mock())
        }
        assertTrue(billingImpl.status.value is BillingStatus.Loading)

        // Start connection. It queries product "spam", so status becomes NotPurchased
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.NotPurchased(pending = false),
            billingImpl.status.value,
        )

        // Now that connection is ready, in-app messages run, but result is NO_ACTION_NEEDED, so status remains NotPurchased
        job.join()
        assertEquals(
            BillingStatus.NotPurchased(pending = false),
            billingImpl.status.value,
        )
    }

    @Test
    fun showInAppMessages_waitsForConnection_andWhenResponseIsSubscriptionStatusUpdated_setsNewStatus() = runTest {
        val purchaseTimeValue = System.currentTimeMillis()
        val purchasedProductIdsAndAutoRenewing = listOf(
            "spam_lifetime" to false,
            "spam_monthly" to true,
            "test_lifetime" to false,
            "test_monthly" to true,
        ).iterator()
        val billingClient = object : FakeBillingClient() {
            override fun showInAppMessages(
                p0: Activity,
                p1: InAppMessageParams,
                p2: InAppMessageResponseListener,
            ): BillingResult {
                p2.onInAppMessageResponse(
                    InAppMessageResult(
                        InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED,
                        null,
                    )
                )
                return BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
            }

            override fun queryPurchasesAsync(p0: QueryPurchasesParams, p1: PurchasesResponseListener) {
                val (productId, autoRenewing) = purchasedProductIdsAndAutoRenewing.next()
                p1.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(), listOf(
                        mock<Purchase> {
                            on { products } doReturn listOf(productId)
                            on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
                            on { purchaseTime } doReturn purchaseTimeValue
                            on { purchaseToken } doReturn "test_purchase_token_purchased"
                            on { isAutoRenewing } doReturn autoRenewing
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
        val billingImpl = BillingImpl(
            context,
            billingClientBuilder,
            products = persistentListOf(
                BillingProduct("test_lifetime", BillingProduct.Type.ONE_TIME),
            ),
            resources = resources,
            log = FakeLog,
        )

        // Show in-app messages. It waits for connection, so status remains Loading
        val job = launch {
            billingImpl.showInAppMessages(mock())
        }
        assertTrue(billingImpl.status.value is BillingStatus.Loading)

        // Start connection. It queries product "spam", so status becomes NotPurchased
        billingImpl.startConnection()
        assertEquals(
            BillingStatus.NotPurchased(pending = false),
            billingImpl.status.value,
        )

        // Now that connection is ready, in-app messages query product "test_lifetime", so status becomes Purchased
        job.join()
        assertEquals(
            BillingStatus.Purchased(
                product = billingImpl.products.first { it.id == "test_lifetime" },
                expired = false,
                refundable = true,
            ),
            billingImpl.status.value,
        )
    }
}

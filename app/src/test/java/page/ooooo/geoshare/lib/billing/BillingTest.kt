package page.ooooo.geoshare.lib.billing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.BillingStatusPreference
import page.ooooo.geoshare.lib.FakeLog
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class BillingTest {

    @Test
    fun refresh_cachedBillingStatusIsLoadingAndBillingProviderReturnsStatusLoading_synchronouslySetsBillingStatusAndCachesIt() =
        runTest {
            val now = System.currentTimeMillis()
            val initialStatus = BillingStatus.Done(DefaultProduct)
            val cachedBillingStatus = BillingStatus.Loading
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(BillingStatusPreference) } doReturn cachedBillingStatus
            }
            val newBillingStatus = BillingStatus.Loading
            val billing = Billing(
                coroutineScope = backgroundScope,
                userPreferencesRepository = mockUserPreferencesRepository,
                billingProvider = object : BillingProvider {
                    override suspend fun queryStatus() = newBillingStatus
                },
                initialStatus = initialStatus,
                log = FakeLog,
            )
            assertEquals(initialStatus, billing.status.value)
            val job = billing.refresh(now)
            assertEquals(newBillingStatus, billing.status.value)
            job?.join()
            assertEquals(newBillingStatus, billing.status.value)
            verify(mockUserPreferencesRepository).setValue(BillingStatusPreference, newBillingStatus)
        }

    @Test
    fun refresh_cachedBillingStatusIsLoadingAndBillingProviderReturnsStatusValid_synchronouslySetsBillingStatusAndCachesIt() =
        runTest {
            val now = System.currentTimeMillis()
            val initialStatus = BillingStatus.Done(DefaultProduct)
            val cachedBillingStatus = BillingStatus.Loading
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(BillingStatusPreference) } doReturn cachedBillingStatus
            }
            val newBillingStatus = BillingStatus.Done(ProProduct, now)
            val billing = Billing(
                coroutineScope = backgroundScope,
                userPreferencesRepository = mockUserPreferencesRepository,
                billingProvider = object : BillingProvider {
                    override suspend fun queryStatus() = newBillingStatus
                },
                initialStatus = initialStatus,
                log = FakeLog,
            )
            assertEquals(initialStatus, billing.status.value)
            val job = billing.refresh(now)
            assertEquals(newBillingStatus, billing.status.value)
            job?.join()
            assertEquals(newBillingStatus, billing.status.value)
            verify(mockUserPreferencesRepository).setValue(BillingStatusPreference, newBillingStatus)
        }

    @Test
    fun refresh_cachedBillingStatusIsDoneAndExpiredAndBillingProviderReturnsStatusLoading_asynchronouslySetsBillingStatusAndCachesIt() =
        runTest {
            val now = System.currentTimeMillis()
            val initialStatus = BillingStatus.Done(DefaultProduct)
            val cachedBillingStatus = BillingStatus.Done(ProProduct, now - 25.hours.inWholeMilliseconds)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(BillingStatusPreference) } doReturn cachedBillingStatus
            }
            val newBillingStatus = BillingStatus.Loading
            val billing = Billing(
                coroutineScope = backgroundScope,
                userPreferencesRepository = mockUserPreferencesRepository,
                billingProvider = object : BillingProvider {
                    override suspend fun queryStatus(): BillingStatus = withContext(Dispatchers.IO) {
                        delay(10.milliseconds)
                        newBillingStatus
                    }
                },
                initialStatus = initialStatus,
                log = FakeLog,
            )
            assertEquals(initialStatus, billing.status.value)
            val job = billing.refresh(now)
            assertEquals(initialStatus, billing.status.value)
            job?.join()
            assertEquals(newBillingStatus, billing.status.value)
            verify(mockUserPreferencesRepository).setValue(BillingStatusPreference, newBillingStatus)
        }

    @Test
    fun refresh_cachedBillingStatusIsDoneAndExpiredAndBillingProviderReturnsStatusValid_asynchronouslySetsBillingStatusAndCachesIt() =
        runTest {
            val now = System.currentTimeMillis()
            val initialStatus = BillingStatus.Done(DefaultProduct)
            val cachedBillingStatus = BillingStatus.Done(ProProduct, now - 25.hours.inWholeMilliseconds)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(BillingStatusPreference) } doReturn cachedBillingStatus
            }
            val newBillingStatus = BillingStatus.Done(ProProduct)
            val billing = Billing(
                coroutineScope = backgroundScope,
                userPreferencesRepository = mockUserPreferencesRepository,
                billingProvider = object : BillingProvider {
                    override suspend fun queryStatus() = newBillingStatus
                },
                initialStatus = initialStatus,
                log = FakeLog,
            )
            assertEquals(initialStatus, billing.status.value)
            val job = billing.refresh(now)
            assertEquals(initialStatus, billing.status.value)
            job?.join()
            assertEquals(newBillingStatus, billing.status.value)
            verify(mockUserPreferencesRepository).setValue(BillingStatusPreference, newBillingStatus)
        }

    @Test
    fun refresh_cachedBillingStatusIsDoneAndExpiredAndBillingProviderThrowsException_asynchronouslySetsBillingStatusToLoading() =
        runTest {
            val now = System.currentTimeMillis()
            val initialStatus = BillingStatus.Done(DefaultProduct)
            val cachedBillingStatus = BillingStatus.Done(ProProduct, now - 25.hours.inWholeMilliseconds)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(BillingStatusPreference) } doReturn cachedBillingStatus
            }
            val billing = Billing(
                coroutineScope = backgroundScope,
                userPreferencesRepository = mockUserPreferencesRepository,
                billingProvider = object : BillingProvider {
                    override suspend fun queryStatus(): BillingStatus {
                        throw Exception("Billing provider error")
                    }
                },
                initialStatus = initialStatus,
                log = FakeLog,
            )
            assertEquals(initialStatus, billing.status.value)
            val job = billing.refresh(now)
            assertEquals(initialStatus, billing.status.value)
            job?.join()
            assertEquals(BillingStatus.Loading, billing.status.value)
            verify(mockUserPreferencesRepository, never()).setValue(eq(BillingStatusPreference), any())
        }

    @Test
    fun refresh_cachedBillingStatusIsDoneAndNotExpired_synchronouslySetsBillingStatusToTheCachedOne() =
        runTest {
            val now = System.currentTimeMillis()
            val initialStatus = BillingStatus.Done(DefaultProduct)
            val cachedBillingStatus = BillingStatus.Done(ProProduct, now - 23.hours.inWholeMilliseconds)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(BillingStatusPreference) } doReturn cachedBillingStatus
            }
            val billing = Billing(
                coroutineScope = backgroundScope,
                initialStatus = initialStatus,
                billingProvider = object : BillingProvider {
                    override suspend fun queryStatus() = BillingStatus.Done(FullProduct) // Not called
                },
                userPreferencesRepository = mockUserPreferencesRepository,
            )
            assertEquals(initialStatus, billing.status.value)
            val job = billing.refresh(now)
            assertEquals(cachedBillingStatus, billing.status.value)
            job?.join()
            assertEquals(cachedBillingStatus, billing.status.value)
            verify(mockUserPreferencesRepository, never()).setValue(eq(BillingStatusPreference), any())
        }
}

package page.ooooo.geoshare.lib.state

import android.content.Context
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.di.FakeBilling
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.preferences.CachedPurchase
import page.ooooo.geoshare.data.local.preferences.CachedPurchasePreference
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ConversionSucceededTest {
    private val context: Context = mock()
    private val coordinateConverter: CoordinateConverter = mock()
    private val linkRepository: LinkRepository = FakeLinkRepository()
    private val log = FakeLog
    private val source = "https://maps.google.com/foo"
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))

    @Test
    fun transition_whenPointsAreEmpty_returnsNull() = runTest {
        val points = persistentListOf<WGS84Point>()
        val automation = CopyCoordsDecAutomation
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertNull(state.transition(stateContext))
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsNoop_returnsNull() = runTest {
        val automation = NoopAutomation
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertNull(state.transition(stateContext))
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIdIsNotSet_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(context)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertNull(state.transition(stateContext))
        assertNull(userPreferencesRepository.getValue(CachedPurchasePreference))
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIsAnUnknownProduct_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(context)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(
                automation = automation,
                cachedPurchase = CachedPurchase(productId = "spam", token = "spam_purchased"),
            )
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertNull(state.transition(stateContext))
        assertEquals(
            CachedPurchase(productId = "spam", token = "spam_purchased"),
            userPreferencesRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIsAKnownProduct_returnsAutomationRequested() = runTest {
        val automation = CopyCoordsDecAutomation
        val output = CopyCoordsDecOutput(coordinateConverter)
        val billing = FakeBilling(context)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(
                automation = automation,
                cachedPurchase = CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            )
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertEquals(
            AutomationRequested(source, points, output),
            state.transition(stateContext),
        )
        assertEquals(
            CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            userPreferencesRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusDoesNotContainAutomationFeature_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(
            context,
            features = persistentListOf(),
            initialStatus = BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
        )
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertNull(state.transition(stateContext))
        assertEquals(
            CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            userPreferencesRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusContainsAutomationFeature_returnsAutomationRequested() = runTest {
        val automation = CopyCoordsDecAutomation
        val output = CopyCoordsDecOutput(coordinateConverter)
        val billing = FakeBilling(
            context,
            initialStatus = BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            )
        )
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertEquals(
            AutomationRequested(source, points, output),
            state.transition(stateContext),
        )
        assertEquals(
            CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            userPreferencesRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndItBecomesPurchasedWithinTimeout_returnsAutomationRequested() = runTest {
        val automation = CopyCoordsDecAutomation
        val output = CopyCoordsDecOutput(coordinateConverter)
        val billing = FakeBilling(context)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points, billingStatusTimeout = 3.seconds)
        var res: ConversionState? = null
        launch {
            res = state.transition(stateContext)
        }
        advanceTimeBy(2.seconds)
        billing.setStatus(
            BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            )
        )
        advanceUntilIdle()
        assertEquals(
            AutomationRequested(source, points, output),
            res,
        )
        assertEquals(
            CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            userPreferencesRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusIsNotPurchasedAndItBecomesPurchasedWithinTimeout_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(
            context,
            initialStatus = BillingStatus.NotPurchased(),
        )
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points, billingStatusTimeout = 3.seconds)
        var res: ConversionState? = null
        launch {
            res = state.transition(stateContext)
        }
        advanceTimeBy(2.seconds)
        billing.setStatus(
            BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            )
        )
        advanceUntilIdle()
        assertNull(res)
        assertNull(userPreferencesRepository.getValue(CachedPurchasePreference))
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndItBecomesPurchasedAfterTimeout_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(context)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points, billingStatusTimeout = 3.seconds)
        var res: ConversionState? = null
        launch {
            res = state.transition(stateContext)
        }
        advanceTimeBy(5.seconds)
        billing.setStatus(
            BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            )
        )
        advanceUntilIdle()
        assertNull(res)
        assertNull(userPreferencesRepository.getValue(CachedPurchasePreference))
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsCopyCoords_returnsAutomationRequested() = runTest {
        val automation = CopyCoordsDecAutomation
        val output = CopyCoordsDecOutput(coordinateConverter)
        val billing = FakeBilling(
            context,
            initialStatus = BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            )
        )
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertEquals(
            AutomationRequested(source, points, output),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsOpenLinkAndLinkIsUnknown_returnsNull() = runTest {
        val automation = ShareLinkUriAutomation(Link(name = "Link that is not in repository").uuid)
        val delay = 2.seconds
        val billing = FakeBilling(
            context,
            initialStatus = BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            )
        )
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation, automationDelay = delay)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.billing } doReturn billing
            on { this@on.coordinateConverter } doReturn coordinateConverter
            on { this@on.linkRepository } doReturn linkRepository
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(source, points)
        assertNull(state.transition(stateContext))
    }
}

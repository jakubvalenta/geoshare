package page.ooooo.geoshare.data.state

import android.content.Context
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.android.UriScheme
import page.ooooo.geoshare.data.billing.BillingProduct
import page.ooooo.geoshare.data.billing.BillingStatus
import page.ooooo.geoshare.data.database.Link
import page.ooooo.geoshare.data.di.FakeBilling
import page.ooooo.geoshare.data.di.FakeGoogleMapsDisplayLink
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferenceRepository
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.FakeLog
import page.ooooo.geoshare.data.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.data.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.data.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.data.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.data.preferences.CachedPurchase
import page.ooooo.geoshare.data.preferences.CachedPurchasePreference
import page.ooooo.geoshare.data.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.preferences.NoopAutomation
import page.ooooo.geoshare.data.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.preferences.UserPreferenceValues
import page.ooooo.geoshare.data.uri.FakeUriQuote
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class AutomationRequestedTest {
    private val context: Context = mock()
    private val coordinateConverter: CoordinateConverter = mock()
    private val linkRepository: LinkRepository = FakeLinkRepository()
    private val log = FakeLog
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val uriQuote = FakeUriQuote

    @Test
    fun transition_whenPointsAreEmpty_returnsNull() = runTest {
        val points = persistentListOf<WGS84Point>()
        val automation = CopyCoordsDecAutomation
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = mock(),
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertNull(state.transition(stateContext))
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsNoop_returnsNull() = runTest {
        val automation = NoopAutomation
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = mock(),
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertNull(state.transition(stateContext))
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIdIsNotSet_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(context)
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertNull(state.transition(stateContext))
        assertNull(userPreferenceRepository.getValue(CachedPurchasePreference))
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIsAnUnknownProduct_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(context)
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(
                automation = automation,
                cachedPurchase = CachedPurchase(productId = "spam", token = "spam_purchased"),
            )
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertNull(state.transition(stateContext))
        assertEquals(
            CachedPurchase(productId = "spam", token = "spam_purchased"),
            userPreferenceRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIsAKnownProduct_returnsActionReady() = runTest {
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter, uriQuote).toAction(points.last())
        val billing = FakeBilling(context)
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(
                automation = automation,
                cachedPurchase = CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            )
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertEquals(
            ActionReady(action, isAutomation = true),
            state.transition(stateContext),
        )
        assertEquals(
            CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            userPreferenceRepository.getValue(CachedPurchasePreference),
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
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertNull(state.transition(stateContext))
        assertEquals(
            CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            userPreferenceRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusContainsAutomationFeature_returnsActionReady() = runTest {
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter, uriQuote).toAction(points.last())
        val billing = FakeBilling(
            context,
            initialStatus = BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            )
        )
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertEquals(
            ActionReady(action, isAutomation = true),
            state.transition(stateContext),
        )
        assertEquals(
            CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            userPreferenceRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndItBecomesPurchasedWithinTimeout_returnsActionReady() = runTest {
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter, uriQuote).toAction(points.last())
        val billing = FakeBilling(context)
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points, billingStatusTimeout = 3.seconds)
        val res = async {
            state.transition(stateContext)
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
            ActionReady(action, isAutomation = true),
            res.await(),
        )
        assertEquals(
            CachedPurchase(productId = "fake_one_time", token = "test_purchased"),
            userPreferenceRepository.getValue(CachedPurchasePreference),
        )
    }

    @Test
    fun transition_whenBillingStatusIsNotPurchasedAndItBecomesPurchasedWithinTimeout_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(
            context,
            initialStatus = BillingStatus.NotPurchased,
        )
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points, billingStatusTimeout = 3.seconds)
        val res = async {
            state.transition(stateContext)
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
        assertNull(res.await())
        assertNull(userPreferenceRepository.getValue(CachedPurchasePreference))
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndItBecomesPurchasedAfterTimeout_returnsNull() = runTest {
        val automation = CopyCoordsDecAutomation
        val billing = FakeBilling(context)
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points, billingStatusTimeout = 3.seconds)
        val res = async {
            state.transition(stateContext)
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
        assertNull(res.await())
        assertNull(userPreferenceRepository.getValue(CachedPurchasePreference))
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsCopyCoords_returnsActionReady() = runTest {
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter, uriQuote).toAction(points.last())
        val billing = FakeBilling(
            context,
            initialStatus = BillingStatus.Purchased(
                product = BillingProduct("fake_one_time", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            )
        )
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertEquals(
            ActionReady(action, isAutomation = true),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsOpenApp_returnsActionWaiting() = runTest {
        val automation = OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)
        val output = OpenDisplayGeoUriOutput(
            UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO),
            coordinateConverter,
            uriQuote,
        )
        val action = output.toAction(points.last())
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
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertEquals(
            ActionWaiting(action, output, isAutomation = true, delay = delay),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsOpenLink_returnsActionWaiting() = runTest {
        val automation = ShareLinkUriAutomation(FakeGoogleMapsDisplayLink.uuid)
        val output = ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote)
        val action = output.toAction(points.last())
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
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertEquals(
            ActionWaiting(action, output, isAutomation = true, delay = delay),
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
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertNull(state.transition(stateContext))
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsSaveGpx_returnsActionWaiting() = runTest {
        val automation = SavePointsGpxAutomation
        val output = SavePointsGpxOutput(coordinateConverter)
        val action = output.toAction(points)
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
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertEquals(
            ActionWaiting(action, output, isAutomation = true, delay = delay),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsShare_returnsActionWaiting() = runTest {
        val automation = OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)
        val output = OpenDisplayGeoUriOutput(
            UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO),
            coordinateConverter,
            uriQuote,
        )
        val action = output.toAction(points.last())
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
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
        val state = AutomationRequested(points)
        assertEquals(
            ActionWaiting(action, output, isAutomation = true, delay = delay),
            state.transition(stateContext),
        )
    }
}

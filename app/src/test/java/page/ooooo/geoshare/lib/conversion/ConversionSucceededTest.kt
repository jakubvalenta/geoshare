package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import page.ooooo.geoshare.data.di.FakeGoogleMapsDisplayLink
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.CachedPurchase
import page.ooooo.geoshare.data.local.preferences.CachedPurchasePreference
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ConversionSucceededTest {
    private val coordinateConverter: CoordinateConverter = mock()

    @Test
    fun transition_whenPointsAreEmpty_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf<WGS84Point>()
        val automation = CopyCoordsDecAutomation
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext: ConversionStateContext = mock {
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertNull(state.transition())
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsNoop_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = NoopAutomation
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext: ConversionStateContext = mock {
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertNull(state.transition())
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIdIsNotSet_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { setValue(eq(CachedPurchasePreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertNull(state.transition())
        verify(userPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIsAnUnknownProduct_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(CachedPurchasePreference) } doReturn
                CachedPurchase(productId = "spam", token = "spam_purchased")
            on { setValue(eq(CachedPurchasePreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertNull(state.transition())
        verify(userPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndCachedProductIsAKnownProduct_returnsActionReady() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(CachedPurchasePreference) } doReturn
                CachedPurchase(productId = "test", token = "test_purchased")
            on { setValue(eq(CachedPurchasePreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertEquals(
            ActionReady(source, points, action, isAutomation = true),
            state.transition(),
        )
        verify(userPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun transition_whenBillingStatusDoesNotContainAutomationFeature_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf()
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { setValue(eq(CachedPurchasePreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertNull(state.transition())
        verify(userPreferencesRepository).setValue(
            CachedPurchasePreference,
            CachedPurchase(productId = "test", token = "test_purchased"),
        )
    }

    @Test
    fun transition_whenBillingStatusContainsAutomationFeature_returnsActionReady() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertEquals(
            ActionReady(source, points, action, isAutomation = true),
            state.transition(),
        )
        verify(userPreferencesRepository).setValue(
            CachedPurchasePreference,
            CachedPurchase(productId = "test", token = "test_purchased"),
        )
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndItBecomesPurchasedWithinTimeout_returnsActionReady() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.Loading())
        val billing: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points, billingStatusTimeout = 3.seconds)
        var res: State? = null
        launch {
            res = state.transition()
        }
        advanceTimeBy(2.seconds)
        mockStatus.value = BillingStatus.Purchased(
            product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
            expired = false,
            refundable = true,
            token = "test_purchased",
        )
        advanceUntilIdle()
        assertEquals(
            ActionReady(source, points, action, isAutomation = true),
            res,
        )
        verify(userPreferencesRepository).setValue(
            CachedPurchasePreference,
            CachedPurchase(productId = "test", token = "test_purchased"),
        )
    }

    @Test
    fun transition_whenBillingStatusIsNotPurchasedAndItBecomesPurchasedWithinTimeout_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.NotPurchased())
        val billing: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points, billingStatusTimeout = 3.seconds)
        var res: State? = null
        launch {
            res = state.transition()
        }
        advanceTimeBy(2.seconds)
        mockStatus.value = BillingStatus.Purchased(
            product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
            expired = false,
            refundable = true,
            token = "test_purchased",
        )
        advanceUntilIdle()
        assertNull(res)
        verify(userPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun transition_whenBillingStatusIsLoadingAndItBecomesPurchasedAfterTimeout_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.Loading())
        val billing: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points, billingStatusTimeout = 3.seconds)
        var res: State? = null
        launch {
            res = state.transition()
        }
        advanceTimeBy(5.seconds)
        mockStatus.value = BillingStatus.Purchased(
            product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
            expired = false,
            refundable = true,
            token = "test_purchased",
        )
        advanceUntilIdle()
        assertNull(res)
        verify(userPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsCopyCoords_returnsActionReady() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertEquals(
            ActionReady(source, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsOpenApp_returnsActionWaiting() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)
        val action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val delay = 2.seconds
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertEquals(
            ActionWaiting(stateContext, source, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsOpenLink_returnsActionWaiting() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = ShareLinkUriAutomation(FakeGoogleMapsDisplayLink.uuid)
        val action = ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter).toAction(points.last())
        val delay = 2.seconds
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertEquals(
            ActionWaiting(stateContext, source, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsOpenLinkAndLinkIsUnknown_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = ShareLinkUriAutomation(Link(name = "Link that is not in repository").uuid)
        val delay = 2.seconds
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertNull(state.transition())
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsSaveGpx_returnsActionWaiting() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = SavePointsGpxAutomation
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val delay = 2.seconds
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertEquals(
            ActionWaiting(stateContext, source, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsShare_returnsActionWaiting() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)
        val action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val delay = 2.seconds
        val billing: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext: ConversionStateContext = mock {
            on { billing } doReturn billing
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = ConversionSucceeded(stateContext, source, points)
        assertEquals(
            ActionWaiting(stateContext, source, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

}

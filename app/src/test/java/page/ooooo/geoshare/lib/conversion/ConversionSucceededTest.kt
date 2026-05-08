package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversionSucceededTest {
    @Test
    fun conversionSucceeded_pointsAreEmpty_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf<Point>()
        val automation = CopyCoordsDecAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsNoop_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = NoopAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndCachedProductIdIsNotSet_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { setValue(eq(CachedPurchasePreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndCachedProductIsAnUnknownProduct_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(CachedPurchasePreference) } doReturn
                CachedPurchase(productId = "spam", token = "spam_purchased")
            on { setValue(eq(CachedPurchasePreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndCachedProductIsAKnownProduct_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(CachedPurchasePreference) } doReturn
                CachedPurchase(productId = "test", token = "test_purchased")
            on { setValue(eq(CachedPurchasePreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusDoesNotContainAutomationFeature_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockBilling: Billing = mock {
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
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { setValue(eq(CachedPurchasePreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
        verify(mockUserPreferencesRepository).setValue(
            CachedPurchasePreference,
            CachedPurchase(productId = "test", token = "test_purchased"),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusContainsAutomationFeature_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val mockBilling: Billing = mock {
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
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
        verify(mockUserPreferencesRepository).setValue(
            CachedPurchasePreference,
            CachedPurchase(productId = "test", token = "test_purchased"),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndItBecomesPurchasedWithinTimeout_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.Loading())
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points, billingStatusTimeout = 3.seconds)
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
            ActionReady(inputUriString, points, action, isAutomation = true),
            res,
        )
        verify(mockUserPreferencesRepository).setValue(
            CachedPurchasePreference,
            CachedPurchase(productId = "test", token = "test_purchased"),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsNotPurchasedAndItBecomesPurchasedWithinTimeout_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.NotPurchased())
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points, billingStatusTimeout = 3.seconds)
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
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndItBecomesPurchasedAfterTimeout_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.Loading())
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points, billingStatusTimeout = 3.seconds)
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
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(CachedPurchasePreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsCopyCoords_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val mockBilling: Billing = mock {
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
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenApp_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)
        val action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val delay = 2.seconds
        val mockBilling: Billing = mock {
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
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenLink_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = ShareLinkUriAutomation(FakeGoogleMapsDisplayLink.uuid)
        val action =
            ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter).toAction(points.last())
        val delay = 2.seconds
        val mockBilling: Billing = mock {
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
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenLinkAndLinkIsUnknown_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = ShareLinkUriAutomation(Link(name = "Link that is not in repository").uuid)
        val delay = 2.seconds
        val mockBilling: Billing = mock {
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
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsSaveGpx_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = SavePointsGpxAutomation
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val delay = 2.seconds
        val mockBilling: Billing = mock {
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
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsShare_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)
        val action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val delay = 2.seconds
        val mockBilling: Billing = mock {
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
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

}

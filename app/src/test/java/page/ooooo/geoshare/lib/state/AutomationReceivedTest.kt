package page.ooooo.geoshare.lib.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeGoogleMapsDisplayLink
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.UriScheme
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import kotlin.time.Duration.Companion.seconds

class AutomationReceivedTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))

    @Test
    fun transition_whenUserPreferenceAutomationIsOpenApp_returnsActionWaiting() = runTest {
        val automation = OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)
        val output = OpenDisplayGeoUriOutput(
            UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO),
            coordinateConverter,
        )
        val action = output.toAction(points.last())
        val delay = 2.seconds
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(userPreferencesRepository)
        val state = AutomationReceived(points, output)
        Assert.assertEquals(
            ActionWaiting(action, output, isAutomation = true, delay = delay),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsOpenLink_returnsActionWaiting() = runTest {
        val automation = ShareLinkUriAutomation(FakeGoogleMapsDisplayLink.uuid)
        val output = ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter)
        val action = output.toAction(points.last())
        val delay = 2.seconds
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(userPreferencesRepository)
        val state = AutomationReceived(points, output)
        Assert.assertEquals(
            ActionWaiting(action, output, isAutomation = true, delay = delay),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenUserPreferenceAutomationIsSaveGpx_returnsActionWaiting() = runTest {
        val automation = SavePointsGpxAutomation
        val output = SavePointsGpxOutput(coordinateConverter)
        val action = output.toAction(points)
        val delay = 2.seconds
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(userPreferencesRepository)
        val state = AutomationReceived(points, output)
        Assert.assertEquals(
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
        )
        val action = output.toAction(points.last())
        val delay = 2.seconds
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(automation = automation, automationDelay = delay)
        )
        val stateContext = ActionStateContext(userPreferencesRepository)
        val state = AutomationReceived(points, output)
        Assert.assertEquals(
            ActionWaiting(action, output, isAutomation = true, delay = delay),
            state.transition(stateContext),
        )
    }
}

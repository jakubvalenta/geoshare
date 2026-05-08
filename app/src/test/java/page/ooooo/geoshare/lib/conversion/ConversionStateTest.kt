package page.ooooo.geoshare.lib.conversion

import android.content.Context
import android.content.res.Resources
import io.ktor.http.HttpStatusCode
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.network.MockNetworkTools
import page.ooooo.geoshare.lib.network.NetworkTools

class ConversionStateTest {

    private val mockResources: Resources = mock {
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_connection_permission_denied) } doReturn "This link is not supported without connecting to the map service"
        on { getString(R.string.conversion_failed_missing_url) } doReturn "Missing URL"
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "invalid URL")
        } doReturn "Failed to process web page due to: invalid URL"
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "no points")
        } doReturn "Failed to process web page due to: no points"
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "response error 404")
        } doReturn "Failed to process web page due to: response error 404"
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "timeout")
        } doReturn "Failed to process web page due to: timeout"
        on { getString(R.string.conversion_failed_parse_url_error) } doReturn "Failed to process map link"
        on { getString(R.string.conversion_failed_reason_invalid_url) } doReturn "invalid URL"
        on { getString(R.string.conversion_failed_reason_missing_header) } doReturn "missing HTTP header"
        on { getString(R.string.conversion_failed_reason_no_points) } doReturn "no points"
        on { getString(R.string.conversion_failed_reason_timeout) } doReturn "timeout"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "invalid URL")
        } doReturn "Failed to resolve short link due to: invalid URL"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "missing HTTP header")
        } doReturn "Failed to resolve short link due to: missing HTTP header"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "response error 404")
        } doReturn "Failed to resolve short link due to: response error 404"
        on { getString(R.string.conversion_failed_unsupported_service) } doReturn "Unsupported map service"
        on { getString(R.string.converter_google_maps_loading_indicator_title) } doReturn "Connecting to Google..."
        on {
            getString(R.string.conversion_loading_indicator_description, 2, 10, "connection closed")
        } doReturn "Attempt 2 out of 10 due to connection closed."
        on { getString(R.string.conversion_succeeded_location_loading_indicator_title) } doReturn "Finding your location..."
        on { getString(R.string.network_exception_eof) } doReturn "connection closed"
        on {
            getString(R.string.network_exception_response_error, HttpStatusCode.NotFound.value)
        } doReturn "response error 404"
    }
    private val mockContext: Context = mock {}
    private val geometries = Geometries(mockContext)
    private val coordinateConverter = CoordinateConverter(geometries)
    private val outputRepository = OutputRepository(coordinateConverter)
    private val linkRepository: LinkRepository = FakeLinkRepository()
    private val userPreferencesRepository: UserPreferencesRepository = FakeUserPreferencesRepository()
    private val uriQuote = FakeUriQuote

    private fun mockStateContext(
        inputs: List<Input<*>> = listOf(GeoUriInput, GoogleMapsUriInput),
        networkTools: NetworkTools = MockNetworkTools(),
        linkRepository: LinkRepository = this@ConversionStateTest.linkRepository,
        userPreferencesRepository: UserPreferencesRepository = this@ConversionStateTest.userPreferencesRepository,
        billing: Billing = mock {},
        log: ILog = FakeLog,
        uriQuote: UriQuote = this@ConversionStateTest.uriQuote,
    ) = ConversionStateContext(
        inputs = inputs,
        networkTools = networkTools,
        linkRepository = linkRepository,
        outputRepository = outputRepository,
        resources = mockResources,
        userPreferencesRepository = userPreferencesRepository,
        billing = billing,
        log = log,
        uriQuote = uriQuote,
    )
}

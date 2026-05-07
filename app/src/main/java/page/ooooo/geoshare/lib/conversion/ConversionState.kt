package page.ooooo.geoshare.lib.conversion

import androidx.annotation.StringRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.CachedPurchase
import page.ooooo.geoshare.data.local.preferences.CachedPurchasePreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.network.NetworkTools
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.UnrecoverableNetworkException
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.BasicAction
import page.ooooo.geoshare.lib.outputs.FileAction
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.inputs.ApiInput
import page.ooooo.geoshare.lib.inputs.NewHtmlInput
import page.ooooo.geoshare.lib.inputs.NewInput
import page.ooooo.geoshare.lib.inputs.NewUriInput
import page.ooooo.geoshare.lib.inputs.NewWebInput
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.ShortLinkGetInput
import page.ooooo.geoshare.lib.inputs.ShortLinkHeadInput
import java.net.MalformedURLException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ConversionState : State {
    override suspend fun transition(): State? = null

    interface HasError {
        val message: String
        val rawData: String
    }

    interface HasResult {
        val rawData: String
        val points: Points
    }

    interface HasPermission {
        val permissionTitleResId: Int

        suspend fun grant(doNotAsk: Boolean): State
        suspend fun deny(doNotAsk: Boolean): State
    }

    interface HasSmallLoadingIndicator {
        fun getLoadingIndicator(): LoadingIndicator.Small
    }

    interface HasLargeLoadingIndicator {
        fun getLoadingIndicator(): LoadingIndicator.Large?
    }

    companion object {
        const val TAG = "ConversionState"
    }
}

class Initial : ConversionState

data class ReceivedData(
    val stateContext: ConversionStateContext,
    val rawData: String,
) : ConversionState {
    override suspend fun transition(): State {
        if (rawData.isEmpty()) {
            return ConversionFailed(
                stateContext.resources.getString(R.string.conversion_failed_missing_url),
                rawData,
            )
        }
        for (input in stateContext.inputs) {
            val data = input.pattern?.find(rawData)?.groupOrNull()
            if (data != null) {
                return FoundInput(stateContext, rawData, data, input)
            }
        }
        return ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_unsupported_service),
            rawData,
        )
    }
}

data class FoundInput(
    val stateContext: ConversionStateContext,
    val rawData: String,
    val data: String,
    val input: NewInput,
    val permission: Permission? = null,
    val prevPoints: Points? = null,
) : ConversionState {
    override suspend fun transition() =
        if (input is NewInput.HasPermission) {
            when (permission ?: stateContext.userPreferencesRepository.getValue(
                ConnectionPermissionPreference
            )) {
                Permission.ALWAYS -> GrantedPermission(
                    stateContext,
                    rawData,
                    data,
                    input,
                    loadingIndicatorTitleResId = input.loadingIndicatorTitleResId,
                    permission = Permission.ALWAYS,
                    prevPoints = prevPoints,
                )

                Permission.ASK -> RequestedPermission(
                    stateContext,
                    rawData,
                    data,
                    input,
                    permissionTitleResId = input.permissionTitleResId,
                    loadingIndicatorTitleResId = input.loadingIndicatorTitleResId,
                )

                Permission.NEVER -> DeniedPermission(stateContext, rawData, input)
            }
        } else {
            GrantedPermission(stateContext, rawData, data, input)
        }
}

data class RequestedPermission(
    val stateContext: ConversionStateContext,
    val rawData: String,
    val data: String,
    val input: NewInput,
    override val permissionTitleResId: Int,
    val loadingIndicatorTitleResId: Int,
) : ConversionState, ConversionState.HasPermission {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return GrantedPermission(stateContext, rawData, data, input, loadingIndicatorTitleResId, Permission.ALWAYS)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return DeniedPermission(stateContext, rawData, input)
    }
}

/**
 * TODO Documentation
 *
 * When GrantedParseWebPermission is the current state, the UI should load [data] as a URL in a WebView and call
 * [onWebUrlChange] once the page loaded in the WebView changes its URL. A page usually changes its URL by calling
 * JavaScript `history.pushState()`.
 *
 * Transitioning this waits for [onWebUrlChange] to be called within [timeout]. If it doesn't happen, it returns a failure.
 *
 * To allow communication between [transition] and [onWebUrlChange], this state contains a mutable private state
 * [webUrlStringFlow]. This doesn't feel elegant and should be implemented differently, when we figure out how.
 */
data class GrantedPermission(
    val stateContext: ConversionStateContext,
    val rawData: String,
    val data: String,
    val input: NewInput,
    val loadingIndicatorTitleResId: Int? = null,
    val permission: Permission? = null,
    val prevPoints: Points? = null,
    val lastAttempt: NetworkTools.Attempt? = null,
    val maxAttempts: Int = 10,
    val timeout: Duration = 60.seconds, // TODO Rename
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ConversionState, ConversionState.HasLargeLoadingIndicator {
    private val webUrlStringFlow = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    override suspend fun transition(): State {
        val result: ParseResult = try {
            withContext(dispatcher) {
                when (input) {
                    is ShortLinkGetInput -> {
                        val uri = Uri.parse(data, stateContext.uriQuote)
                        val url = uri.toUrl()
                        val unshortenedUrlString = stateContext.networkTools.httpGetRedirectedUrlString(
                            url, lastAttempt = lastAttempt, maxAttempts = maxAttempts
                        )
                        val unshortenedUri =
                            Uri.parse(unshortenedUrlString, stateContext.uriQuote).toAbsoluteUri(uri)
                        stateContext.log.i(
                            ConversionState.TAG,
                            "Unshorten: Resolved short URI $data to $unshortenedUri",
                        )
                        input.parse(unshortenedUri)
                    }

                    is ShortLinkHeadInput -> {
                        val uri = Uri.parse(data, stateContext.uriQuote)
                        val url = uri.toUrl()
                        val unshortenedUrlString = stateContext.networkTools.httpHeadLocationHeader(
                            url, lastAttempt = lastAttempt, maxAttempts = maxAttempts
                        )
                        val unshortenedUri =
                            Uri.parse(unshortenedUrlString, stateContext.uriQuote).toAbsoluteUri(uri)
                        stateContext.log.i(
                            ConversionState.TAG,
                            "Unshorten: Resolved short URI $data to $unshortenedUri",
                        )
                        input.parse(unshortenedUri)
                    }

                    is NewUriInput -> {
                        val uri = Uri.parse(data, stateContext.uriQuote)
                        input.parse(uri, stateContext.uriQuote)
                    }

                    is NewHtmlInput -> {
                        val url = Uri.parse(data, stateContext.uriQuote).toUrl()
                        stateContext.log.i(ConversionState.TAG, "Parse: Downloading $url")
                        stateContext.networkTools.httpGetBodyAsByteReadChannel(
                            url, lastAttempt = lastAttempt, maxAttempts = maxAttempts, dispatcher = Dispatchers.Default
                        ) { channel ->
                            input.parse(channel, prevPoints, uriQuote = stateContext.uriQuote, log = stateContext.log)
                        }
                    }

                    is NewWebInput -> {
                        // Wait for webUrlString to be set
                        val webUrlString = webUrlStringFlow
                            .filterNotNull()
                            .timeout(timeout)
                            .first()
                        input.parse(webUrlString)
                    }

                    is ApiInput -> {
                        val url = Uri.parse(data, stateContext.uriQuote).toUrl()
                        input.parse(url)
                    }
                }
            }
        } catch (_: TimeoutCancellationException) {
            stateContext.log.e(ConversionState.TAG, "Parse: Timed out")
            return ConversionFailed(
                stateContext.resources.getString(
                    R.string.conversion_failed_parse_html_error_with_reason,
                    stateContext.resources.getString(R.string.conversion_failed_reason_timeout),
                ),
                rawData,
            )
        } catch (_: CancellationException) {
            return ConversionFailed(
                stateContext.resources.getString(R.string.conversion_failed_cancelled),
                rawData
            )
        } catch (_: MalformedURLException) {
            return ConversionFailed(
                stateContext.resources.getString(
                    R.string.conversion_failed_unshorten_error_with_reason,
                    stateContext.resources.getString(R.string.conversion_failed_reason_invalid_url),
                ),
                rawData,
            )
        } catch (tr: RecoverableNetworkException) {
            return GrantedPermission(
                stateContext,
                rawData,
                data,
                input,
                loadingIndicatorTitleResId,
                permission,
                prevPoints,
                NetworkTools.Attempt(lastAttempt?.number?.plus(1) ?: 1, tr),
            )
        } catch (tr: UnrecoverableNetworkException) {
            return ConversionFailed(
                stateContext.resources.getString(
                    R.string.conversion_failed_unshorten_error_with_reason,
                    tr.getMessage(stateContext.resources),
                ),
                rawData,
            )
        }
        return result.run {
            if (points.lastOrNull()?.hasCoordinates() == true) {
                stateContext.log.i(ConversionState.TAG, "Parse: Converted $data to $points")
                ConversionSucceeded(stateContext, rawData, points)
            } else if (nextInput != null) {
                stateContext.log.i(ConversionState.TAG, "Parse: Going to next input $nextInput") // TODO toString()
                FoundInput(stateContext, rawData, nextData ?: data, nextInput, permission, points)
            } else if (points.lastOrNull()?.hasName() == true) {
                stateContext.log.i(ConversionState.TAG, "Parse: Converted $data to $points")
                ConversionSucceeded(stateContext, rawData, points)
            } else {
                stateContext.log.i(ConversionState.TAG, "Parse: Failed to parse $data")
                ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_parse_url_error),
                    rawData,
                )
                // TODO Specific errors
                // ConversionFailed(
                //     stateContext.resources.getString(
                //         R.string.conversion_failed_parse_html_error_with_reason,
                //         stateContext.resources.getString(R.string.conversion_failed_reason_no_points),
                //     ),
                //     rawData,
                // )
            }
        }
    }

    // TODO
    fun onWebUrlChange(urlString: String) {
        webUrlStringFlow.value = urlString
    }

    override fun getLoadingIndicator() = loadingIndicatorTitleResId?.let { loadingIndicatorTitleResId ->
        LoadingIndicator.Large(
            title = stateContext.resources.getString(loadingIndicatorTitleResId),
            description = lastAttempt?.let {
                stateContext.resources.getString(
                    R.string.conversion_loading_indicator_description,
                    it.number,
                    maxAttempts,
                    it.cause.getMessage(stateContext.resources),
                )
            },
        )
    }
}

data class DeniedPermission(
    val stateContext: ConversionStateContext,
    val rawData: String,
    val input: NewInput,
) : ConversionState {
    override suspend fun transition(): State =
        ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_connection_permission_denied),
            rawData,
        )
}

data class ConversionSucceeded(
    val stateContext: ConversionStateContext,
    override val rawData: String,
    override val points: Points,
    val billingStatusTimeout: Duration = 3.seconds,
) : ConversionState, ConversionState.HasResult {
    @OptIn(FlowPreview::class)
    override suspend fun transition(): State? {
        val lastPoint = points.lastOrNull() ?: return null
        val automation = stateContext.userPreferencesRepository.getValue(AutomationPreference)
        if (automation is NoopAutomation) {
            return null
        }

        val billingStatus: BillingStatus = try {
            // Wait for billing status to appear; it should appear, because we call Billing.startConnection() in onCreate
            stateContext.billing.status
                .filter {
                    when (it) {
                        is BillingStatus.Loading -> false

                        is BillingStatus.Pending, is BillingStatus.NotPurchased -> true

                        is BillingStatus.Purchased -> {
                            // If billing status appeared within timeout, cache it
                            stateContext.userPreferencesRepository.setValue(
                                CachedPurchasePreference,
                                CachedPurchase(productId = it.product.id, token = it.token),
                            )
                            true
                        }
                    }
                }
                .timeout(billingStatusTimeout)
                .first()
        } catch (_: TimeoutCancellationException) {
            // If billing status didn't appear, try to read it from cache
            stateContext.log.w(
                ConversionState.TAG, "Automation: Billing status didn't appear within $billingStatusTimeout"
            )
            stateContext.userPreferencesRepository.getValue(CachedPurchasePreference)
                ?.let { cachedPurchase ->
                    stateContext.billing.products.firstOrNull { product -> cachedPurchase.productId == product.id }
                        ?.let { product ->
                            stateContext.log.w(ConversionState.TAG, "Automation: Found cached billing status")
                            BillingStatus.Purchased(
                                product,
                                expired = false,
                                refundable = true,
                                token = cachedPurchase.token,
                            )
                        }
                }
                ?: run {
                    stateContext.log.w(ConversionState.TAG, "Automation: Didn't find cached billing status")
                    BillingStatus.Loading()
                }
        }

        if (billingStatus is BillingStatus.Purchased && stateContext.billing.features.contains(AutomationFeature)) {
            val output = stateContext.outputRepository.getAutomationOutput(
                automation = automation,
                getLinkByUUID = { stateContext.linkRepository.getByUUID(it) },
            ) ?: return null
            val action = when (output) {
                is PointOutput -> output.toAction(lastPoint)
                is PointsOutput -> output.toAction(points)
            }
            if (output is Output.HasAutomationDelay) {
                val delay = stateContext.userPreferencesRepository.getValue(AutomationDelayPreference)
                return ActionWaiting(stateContext, rawData, points, action, isAutomation = true, delay = delay)
            }
            return ActionReady(rawData, points, action, isAutomation = true)
        }
        return null
    }
}

data class ConversionFailed(
    override val message: String,
    override val rawData: String,
) : ConversionState, ConversionState.HasError

data class ActionWaiting(
    val stateContext: ConversionStateContext,
    override val rawData: String,
    override val points: Points,
    val action: Action<*>,
    @Suppress("SameParameterValue") val isAutomation: Boolean,
    val delay: Duration,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = try {
        if (delay.isPositive()) {
            delay(delay)
        }
        ActionReady(rawData, points, action, isAutomation)
    } catch (_: CancellationException) {
        ActionFinished(rawData, points, action, isAutomation)
    }
}

data class ActionReady(
    override val rawData: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = when (action) {
        is BasicAction -> BasicActionReady(rawData, points, action, isAutomation)
        is FileAction -> FileUriRequested(rawData, points, action, isAutomation)
        is LocationAction -> LocationRationaleRequested(rawData, points, action, isAutomation)
    }
}

data class BasicActionReady(
    override val rawData: String,
    override val points: Points,
    val action: BasicAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class FileActionReady(
    override val rawData: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
    val uri: android.net.Uri,
) : ConversionState, ConversionState.HasResult

data class LocationActionReady(
    override val rawData: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point,
) : ConversionState, ConversionState.HasResult

data class ActionRan(
    override val rawData: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
    val success: Boolean?,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = when (success) {
        true -> ActionSucceeded(rawData, points, action, isAutomation)
        false -> ActionFailed(rawData, points, action, isAutomation)
        else -> ActionFinished(rawData, points, action, isAutomation)
    }
}

data class ActionSucceeded(
    override val rawData: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(rawData, points, action, isAutomation)
    }
}

data class ActionFailed(
    override val rawData: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(rawData, points, action, isAutomation)
    }
}

data class ActionFinished(
    override val rawData: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class FileUriRequested(
    override val rawData: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationRationaleRequested(
    override val rawData: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationRationaleShown(
    override val rawData: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasPermission, ConversionState.HasResult {
    @StringRes
    override val permissionTitleResId = R.string.conversion_succeeded_location_rationale_dialog_title

    override suspend fun grant(doNotAsk: Boolean): State =
        LocationRationaleConfirmed(rawData, points, action, isAutomation)

    override suspend fun deny(doNotAsk: Boolean): State =
        ActionFinished(rawData, points, action, isAutomation)
}

data class LocationRationaleConfirmed(
    override val rawData: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationPermissionReceived(
    val stateContext: ConversionStateContext,
    override val rawData: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasSmallLoadingIndicator, ConversionState.HasResult {
    override fun getLoadingIndicator() = LoadingIndicator.Small(
        stateContext.resources.getString(R.string.conversion_succeeded_location_loading_indicator_title)
    )
}

data class LocationReceived(
    override val rawData: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point?,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = if (location == null) {
        LocationFindingFailed(rawData, points, action, isAutomation)
    } else {
        LocationActionReady(rawData, points, action, isAutomation = isAutomation, location = location)
    }
}

data class LocationFindingFailed(
    override val rawData: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(rawData, points, action, isAutomation)
    }
}

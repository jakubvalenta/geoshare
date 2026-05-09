package page.ooooo.geoshare.lib.conversion

import android.net.Uri
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
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.inputs.WebViewInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.BasicInput
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
import java.net.MalformedURLException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ConversionState : State {
    override suspend fun transition(): State? = null

    interface HasError {
        val message: String
        val source: String
    }

    interface HasResult {
        val source: String
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

// TODO Rename to SourceReceived
data class ReceivedSourceData(
    val stateContext: ConversionStateContext,
    val source: String,
) : ConversionState {
    override suspend fun transition(): State {
        if (source.isEmpty()) {
            return ConversionFailed(
                stateContext.resources.getString(R.string.conversion_failed_missing_url),
                source,
            )
        }
        for (input in stateContext.inputs) {
            val match = input.match(source)
            if (match != null) {
                return FoundInput(stateContext, source, match, input)
            }
        }
        return ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_unsupported_service),
            source,
        )
    }
}

// TODO Rename to InputFound
data class FoundInput<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val permission: Permission? = null,
    val prevPoints: Points? = null,
) : ConversionState {
    override suspend fun transition() =
        if (input is Input.HasPermission) {
            when (permission ?: stateContext.userPreferencesRepository.getValue(
                ConnectionPermissionPreference
            )) {
                Permission.ALWAYS -> GrantedPermission(
                    stateContext,
                    source,
                    match,
                    input,
                    loadingIndicatorTitleResId = input.loadingIndicatorTitleResId,
                    permission = Permission.ALWAYS,
                    prevPoints = prevPoints,
                )

                Permission.ASK -> RequestedPermission(
                    stateContext,
                    source,
                    match,
                    input,
                    permissionTitleResId = input.permissionTitleResId,
                    loadingIndicatorTitleResId = input.loadingIndicatorTitleResId,
                )

                Permission.NEVER -> DeniedPermission(stateContext, source, input)
            }
        } else {
            GrantedPermission(stateContext, source, match, input)
        }
}

// TODO Rename to PermissionRequested
data class RequestedPermission<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    override val permissionTitleResId: Int,
    val loadingIndicatorTitleResId: Int,
) : ConversionState, ConversionState.HasPermission {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return GrantedPermission(stateContext, source, match, input, loadingIndicatorTitleResId, Permission.ALWAYS)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return DeniedPermission(stateContext, source, input)
    }
}

// TODO Rename to PermissionGranted
data class GrantedPermission<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val loadingIndicatorTitleResId: Int? = null,
    val permission: Permission? = null,
    val prevPoints: Points? = null,
    val lastAttempt: NetworkTools.Attempt? = null,
    val maxAttempts: Int = 10,
) : ConversionState {
    override suspend fun transition(): State =
        when (input) {
            is BasicInput -> GrantedPermissionBasicInput(
                stateContext,
                source,
                match,
                input,
                loadingIndicatorTitleResId,
                permission,
                prevPoints,
                lastAttempt,
                maxAttempts,
            )

            is WebViewInput -> GrantedPermissionWebViewInput(
                stateContext,
                source,
                match,
                input,
                permission,
                prevPoints,
            )
        }
}

// TODO Rename to PermissionGrantedBasicInput
data class GrantedPermissionBasicInput<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: BasicInput<T>,
    val loadingIndicatorTitleResId: Int? = null,
    val permission: Permission? = null,
    val prevPoints: Points? = null,
    val lastAttempt: NetworkTools.Attempt? = null,
    val maxAttempts: Int = 10,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ConversionState, ConversionState.HasLargeLoadingIndicator {
    @OptIn(FlowPreview::class)
    override suspend fun transition(): State = try {
        withContext(dispatcher) {
            // Run parsing on another thread, because maybe it's computationally expensive
            try {
                val result = input.withData(
                    match,
                    stateContext.networkTools,
                    lastAttempt,
                    maxAttempts,
                    stateContext.uriQuote,
                    stateContext.log
                ) { data ->
                    input.parse(data, prevPoints, stateContext.uriQuote, stateContext.log)
                }
                ParsedData(stateContext, source, match, input, result, permission, prevPoints)
            } catch (_: MalformedURLException) {
                ConversionFailed(
                    stateContext.resources.getString(
                        R.string.conversion_failed_unshorten_error_with_reason,
                        stateContext.resources.getString(R.string.conversion_failed_reason_invalid_url),
                    ),
                    source,
                )
            } catch (tr: RecoverableNetworkException) {
                GrantedPermission(
                    stateContext,
                    source,
                    match,
                    input,
                    loadingIndicatorTitleResId,
                    permission,
                    prevPoints,
                    lastAttempt = NetworkTools.Attempt(lastAttempt?.number?.plus(1) ?: 2, tr),
                    maxAttempts = maxAttempts,
                )
            } catch (tr: UnrecoverableNetworkException) {
                // TODO Don't mention short link in the error message
                ConversionFailed(
                    stateContext.resources.getString(
                        R.string.conversion_failed_unshorten_error_with_reason,
                        tr.getMessage(stateContext.resources),
                    ),
                    source,
                )
            }
        }
    } catch (_: CancellationException) {
        // Cancellation must be caught outside withContext, because withContext somehow rethrows errors
        ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_cancelled),
            source
        )
    }

    override fun getLoadingIndicator() = loadingIndicatorTitleResId?.let { loadingIndicatorTitleResId ->
        LoadingIndicator.Large(
            title = stateContext.resources.getString(loadingIndicatorTitleResId),
            description = lastAttempt?.let {
                stateContext.resources.getString(
                    R.string.conversion_loading_indicator_description,
                    it.number, // TODO Should this be it.number + 1?
                    maxAttempts,
                    it.cause.getMessage(stateContext.resources),
                )
            },
        )
    }
}

/**
 * When this state is the current state, the UI should load [match] as a page URL in a WebView and call [setData] with
 * the URL that the page redirects to once it's fully loaded.
 *
 * The [transition] function of this state will wait for [setData] to be called. If it's not called within [timeout], it
 * returns failure.
 */
// TODO Rename to PermissionGrantedWebViewInput
data class GrantedPermissionWebViewInput(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: WebViewInput,
    val permission: Permission? = null,
    val prevPoints: Points? = null,
    val timeout: Duration = 60.seconds,
    val dispatcher: CoroutineContext = Dispatchers.Default,
) : ConversionState {
    private val dataFlow = MutableStateFlow<String?>(null)

    fun setData(data: String) {
        dataFlow.value = data
    }

    @OptIn(FlowPreview::class)
    override suspend fun transition(): State = try {
        withContext(dispatcher) {
            // Run parsing on another thread, because maybe it's computationally expensive
            try {
                val data = dataFlow
                    .filterNotNull()
                    .timeout(timeout)
                    .first()
                val result = input.parse(data, prevPoints, stateContext.uriQuote, stateContext.log)
                ParsedData(stateContext, source, match, input, result, permission, prevPoints)
            } catch (_: TimeoutCancellationException) {
                stateContext.log.e(ConversionState.TAG, "Parse: Timed out")
                ConversionFailed(
                    stateContext.resources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        stateContext.resources.getString(R.string.conversion_failed_reason_timeout),
                    ),
                    source,
                )
            }
        }
    } catch (_: CancellationException) {
        // Cancellation must be caught outside withContext, because withContext somehow rethrows errors
        ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_cancelled),
            source,
        )
    }
}

// TODO Rename to PermissionDenied
data class DeniedPermission<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val input: Input<T>,
) : ConversionState {
    override suspend fun transition(): State =
        ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_connection_permission_denied),
            source,
        )
}

// TODO Rename to DataParsed
data class ParsedData(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<*>,
    val result: ParseResult,
    val permission: Permission? = null,
    val prevPoints: Points? = null,
) : ConversionState {
    override suspend fun transition(): State =
        result.run {
            if (points.lastOrNull()?.hasCoordinates() == true) {
                stateContext.log.i(ConversionState.TAG, "Parse: Converted $match to $points")
                ConversionSucceeded(stateContext, source, points)
            } else if (nextInput != null) {
                stateContext.log.i(ConversionState.TAG, "Parse: Going to next input $nextInput") // TODO toString()
                FoundInput(stateContext, source, nextMatch ?: match, nextInput, permission, points)
            } else if (points.lastOrNull()?.hasName() == true) {
                stateContext.log.i(ConversionState.TAG, "Parse: Converted $match to $points")
                ConversionSucceeded(stateContext, source, points)
            } else {
                stateContext.log.i(ConversionState.TAG, "Parse: Failed to parse $match")
                ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_parse_url_error),
                    source,
                )
                // TODO Use specific error R.string.conversion_failed_parse_html_error_with_reason with R.string.conversion_failed_reason_no_points
                // TODO Use specific error R.string.conversion_failed_connection_permission_denied
            }
        }
}

data class ConversionSucceeded(
    val stateContext: ConversionStateContext,
    override val source: String,
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
                return ActionWaiting(stateContext, source, points, action, isAutomation = true, delay = delay)
            }
            return ActionReady(source, points, action, isAutomation = true)
        }
        return null
    }
}

data class ConversionFailed(
    override val message: String,
    override val source: String,
) : ConversionState, ConversionState.HasError

data class ActionWaiting(
    val stateContext: ConversionStateContext,
    override val source: String,
    override val points: Points,
    val action: Action<*>,
    @Suppress("SameParameterValue") val isAutomation: Boolean,
    val delay: Duration,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = try {
        if (delay.isPositive()) {
            delay(delay)
        }
        ActionReady(source, points, action, isAutomation)
    } catch (_: CancellationException) {
        ActionFinished(source, points, action, isAutomation)
    }
}

data class ActionReady(
    override val source: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = when (action) {
        is BasicAction -> BasicActionReady(source, points, action, isAutomation)
        is FileAction -> FileUriRequested(source, points, action, isAutomation)
        is LocationAction -> LocationRationaleRequested(source, points, action, isAutomation)
    }
}

data class BasicActionReady(
    override val source: String,
    override val points: Points,
    val action: BasicAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class FileActionReady(
    override val source: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
    val uri: Uri,
) : ConversionState, ConversionState.HasResult

data class LocationActionReady(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point,
) : ConversionState, ConversionState.HasResult

data class ActionRan(
    override val source: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
    val success: Boolean?,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = when (success) {
        true -> ActionSucceeded(source, points, action, isAutomation)
        false -> ActionFailed(source, points, action, isAutomation)
        else -> ActionFinished(source, points, action, isAutomation)
    }
}

data class ActionSucceeded(
    override val source: String,
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
        return ActionFinished(source, points, action, isAutomation)
    }
}

data class ActionFailed(
    override val source: String,
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
        return ActionFinished(source, points, action, isAutomation)
    }
}

data class ActionFinished(
    override val source: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class FileUriRequested(
    override val source: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationRationaleRequested(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationRationaleShown(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasPermission, ConversionState.HasResult {
    @StringRes
    override val permissionTitleResId = R.string.conversion_succeeded_location_rationale_dialog_title

    override suspend fun grant(doNotAsk: Boolean): State =
        LocationRationaleConfirmed(source, points, action, isAutomation)

    override suspend fun deny(doNotAsk: Boolean): State =
        ActionFinished(source, points, action, isAutomation)
}

data class LocationRationaleConfirmed(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationPermissionReceived(
    val stateContext: ConversionStateContext,
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasSmallLoadingIndicator, ConversionState.HasResult {
    override fun getLoadingIndicator() = LoadingIndicator.Small(
        stateContext.resources.getString(R.string.conversion_succeeded_location_loading_indicator_title)
    )
}

data class LocationReceived(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point?,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = if (location == null) {
        LocationFindingFailed(source, points, action, isAutomation)
    } else {
        LocationActionReady(source, points, action, isAutomation = isAutomation, location = location)
    }
}

data class LocationFindingFailed(
    override val source: String,
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
        return ActionFinished(source, points, action, isAutomation)
    }
}

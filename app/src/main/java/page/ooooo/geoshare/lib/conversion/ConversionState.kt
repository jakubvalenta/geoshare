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
}

class Initial : ConversionState

data class SourceReceived(
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
                return InputFound(stateContext, source, match, input)
            }
        }
        return ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_unsupported_service),
            source,
        )
    }
}

data class InputFound<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val permission: Permission? = null,
    val prevResult: ParseResult? = null,
) : ConversionState {
    override suspend fun transition() =
        if (input is Input.HasPermission) {
            when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                Permission.ALWAYS -> PermissionGranted(
                    stateContext, source, match, input, Permission.ALWAYS, prevResult
                )

                Permission.ASK -> PermissionRequested(
                    stateContext, source, match, input, prevResult, input.permissionTitleResId
                )

                Permission.NEVER -> PermissionDenied(stateContext, source, match, input, Permission.NEVER, prevResult)
            }
        } else {
            PermissionGranted(stateContext, source, match, input, permission, prevResult)
        }
}

data class PermissionRequested<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val prevResult: ParseResult? = null,
    override val permissionTitleResId: Int,
) : ConversionState, ConversionState.HasPermission {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return PermissionGranted(stateContext, source, match, input, Permission.ALWAYS, prevResult)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return PermissionDenied(stateContext, source, match, input, Permission.NEVER, prevResult)
    }
}

data class PermissionGranted<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val permission: Permission?,
    val prevResult: ParseResult? = null,
    val lastAttempt: NetworkTools.Attempt? = null,
    val maxAttempts: Int = 10,
) : ConversionState {
    override suspend fun transition(): State =
        when (input) {
            is BasicInput -> PermissionGrantedBasicInput(
                stateContext, source, match, input, permission, prevResult, lastAttempt, maxAttempts
            )

            is WebViewInput -> PermissionGrantedWebViewInput(
                stateContext, source, match, input, permission, prevResult,
            )
        }
}

data class PermissionGrantedBasicInput<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: BasicInput<T>,
    val permission: Permission?,
    val prevResult: ParseResult? = null,
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
                    input.parse(data, match, prevResult, stateContext.uriQuote, stateContext.log)
                }
                DataParsed(stateContext, source, match, input, result, permission, prevResult)
            } catch (_: MalformedURLException) {
                ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_reason_invalid_url),
                    source,
                )
            } catch (tr: RecoverableNetworkException) {
                PermissionGranted(
                    stateContext,
                    source,
                    match,
                    input,
                    permission,
                    prevResult,
                    lastAttempt = NetworkTools.Attempt(lastAttempt?.number?.plus(1) ?: 2, tr),
                    maxAttempts = maxAttempts,
                )
            } catch (tr: UnrecoverableNetworkException) {
                ConversionFailed(
                    tr.getMessage(stateContext.resources),
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

    override fun getLoadingIndicator() =
        (input as? Input.HasPermission)?.loadingIndicatorTitleResId?.let { loadingIndicatorTitleResId ->
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

/**
 * When this state is the current state, the UI should load [match] as a page URL in a WebView and call [setData] with
 * the URL that the page redirects to once it's fully loaded.
 *
 * The [transition] function of this state will wait for [setData] to be called. If it's not called within [timeout], it
 * returns failure.
 */
data class PermissionGrantedWebViewInput(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: WebViewInput,
    val permission: Permission?,
    val prevResult: ParseResult? = null,
    val timeout: Duration = 60.seconds,
    val dispatcher: CoroutineContext = Dispatchers.Default,
) : ConversionState, ConversionState.HasLargeLoadingIndicator {
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
                val result = input.parse(data, match, prevResult, stateContext.uriQuote, stateContext.log)
                DataParsed(stateContext, source, match, input, result, permission, prevResult)
            } catch (_: TimeoutCancellationException) {
                stateContext.log.e(TAG, "Timed out")
                ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_reason_timeout),
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

    override fun getLoadingIndicator() =
        LoadingIndicator.Large(stateContext.resources.getString(input.loadingIndicatorTitleResId))

    private companion object {
        private const val TAG = "PermissionGrantedWebViewInput"
    }
}

data class PermissionDenied<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val permission: Permission,
    val prevResult: ParseResult? = null,
) : ConversionState {
    override suspend fun transition(): State =
        if (prevResult != null) {
            // Fall back to previous result
            DataParsed(stateContext, source, match, input, result = ParseResult(), permission, prevResult)
        } else {
            ConversionFailed(
                stateContext.resources.getString(R.string.conversion_failed_connection_permission_denied),
                source,
            )
        }
}

data class DataParsed<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val result: ParseResult,
    val permission: Permission?,
    val prevResult: ParseResult? = null,
) : ConversionState {
    override suspend fun transition(): State =
        result.run {
            if (points.lastOrNull()?.hasCoordinates() == true) {
                stateContext.log.i(TAG, "Converted $match to $points")
                ConversionSucceeded(stateContext, source, points)
            } else if (nextInput != null) {
                val nextMatch = nextMatch ?: match
                stateContext.log.i(TAG, "Going to next input $nextInput and match $nextMatch")
                InputFound(stateContext, source, nextMatch, nextInput, permission, prevResult = this)
            } else if (points.lastOrNull()?.hasName() == true) {
                stateContext.log.i(TAG, "Converted $match to $points")
                ConversionSucceeded(stateContext, source, points)
            } else if (prevResult != null) {
                stateContext.log.i(TAG, "Fall back to previous result $prevResult.points")
                ConversionSucceeded(stateContext, source, prevResult.points)
            } else {
                stateContext.log.i(TAG, "Failed to parse $match")
                ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_reason_no_points),
                    source,
                )
            }
        }

    private companion object {
        private const val TAG = "ConversionSucceeded"
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
            stateContext.log.w(TAG, "Automation: Billing status didn't appear within $billingStatusTimeout")
            stateContext.userPreferencesRepository.getValue(CachedPurchasePreference)
                ?.let { cachedPurchase ->
                    stateContext.billing.products.firstOrNull { product -> cachedPurchase.productId == product.id }
                        ?.let { product ->
                            stateContext.log.w(TAG, "Automation: Found cached billing status")
                            BillingStatus.Purchased(
                                product,
                                expired = false,
                                refundable = true,
                                token = cachedPurchase.token,
                            )
                        }
                }
                ?: run {
                    stateContext.log.w(TAG, "Automation: Didn't find cached billing status")
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

    private companion object {
        private const val TAG = "ConversionSucceeded"
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

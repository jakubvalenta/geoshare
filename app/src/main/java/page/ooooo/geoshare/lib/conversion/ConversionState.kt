package page.ooooo.geoshare.lib.conversion

import android.net.Uri
import androidx.annotation.StringRes
import kotlinx.coroutines.CancellationException
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
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.calcExponentialBackoffMillis
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.inputs.BasicInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.NextStep
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.WebViewInput
import page.ooooo.geoshare.lib.network.MaxAttemptsReachedNetworkException
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.UnrecoverableNetworkException
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.ActionResult
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

class Initial : ConversionState {
    override fun toString() = "Initial"
}

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

    override fun toString() = "SourceReceived"
}

data class InputFound<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val permission: Permission? = null,
    val prevResult: ParseResult? = null,
) : ConversionState {
    override suspend fun transition(): State {
        stateContext.log.i(TAG, "Using input $input with match $match")
        return if (input is Input.HasPermission) {
            when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                Permission.ALWAYS -> PermissionGranted(
                    stateContext, source, match, input, Permission.ALWAYS, prevResult
                )

                Permission.ASK -> PermissionRequested(
                    stateContext, source, match, input, prevResult, input.permissionTitleResId
                )

                Permission.NEVER -> DataParsed(
                    stateContext,
                    source,
                    match,
                    input,
                    result = ParseResult(),
                    permission = Permission.NEVER,
                    prevResult,
                )
            }
        } else {
            PermissionGranted(stateContext, source, match, input, permission, prevResult)
        }
    }

    private companion object {
        private const val TAG = "InputFound"
    }

    override fun toString() = TAG
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
        return DataParsed(
            stateContext, source, match, input, result = ParseResult(), permission = Permission.NEVER, prevResult
        )
    }

    override fun toString() = "PermissionRequested"
}

data class PermissionGranted<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input<T>,
    val permission: Permission?,
    val prevResult: ParseResult? = null,
) : ConversionState {
    override suspend fun transition(): State =
        when (input) {
            is BasicInput -> PermissionGrantedBasicInput(stateContext, source, match, input, permission, prevResult)
            is WebViewInput -> PermissionGrantedWebViewInput(stateContext, source, match, input, permission, prevResult)
        }

    override fun toString() = "PermissionGranted"
}

/**
 * Fetches input data using [BasicInput.withData] and parses it using [BasicInput.parse].
 *
 * When [BasicInput.withData] fails, it is retried up to [maxAttempts]. Retrying is done by recursively transitioning
 * this state while tracking the number of attempts made and the cause of the last failure in [lastAttempt].
 *
 * We use this custom retrying instead of the [io.ktor.client.plugins.HttpRequestRetry] plugin, because it makes the
 * state change, which allows the UI to react to it and show the user a message about the progress of the retrying.
 */
data class PermissionGrantedBasicInput<T>(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: BasicInput<T>,
    val permission: Permission?,
    val prevResult: ParseResult? = null,
    val lastAttempt: Attempt<RecoverableNetworkException>? = null,
    val maxAttempts: Int = 10,
    val dispatcher: CoroutineContext = Dispatchers.Default,
) : ConversionState, ConversionState.HasLargeLoadingIndicator {
    @OptIn(FlowPreview::class)
    override suspend fun transition(): State = try {
        withContext(dispatcher) {
            val attemptNumber = lastAttempt?.number?.plus(1) ?: 1
            try {
                if (lastAttempt != null && lastAttempt.number >= maxAttempts) {
                    stateContext.log.w(TAG, "Maximum number of $maxAttempts attempts reached for $match")
                    throw MaxAttemptsReachedNetworkException(lastAttempt.cause)
                }
                val delayMillis = calcExponentialBackoffMillis(attemptNumber)
                if (delayMillis > 0) {
                    stateContext.log.i(
                        TAG, "Waiting ${delayMillis}ms before attempt $attemptNumber of $maxAttempts for $match"
                    )
                    delay(delayMillis)
                }
                val result = input.withData(
                    match,
                    stateContext.engine,
                    stateContext.log,
                    stateContext.uriQuote,
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
                val attempt = Attempt(attemptNumber, tr)
                PermissionGrantedBasicInput(
                    stateContext, source, match, input, permission, prevResult, attempt, maxAttempts
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
                        it.number + 1,
                        maxAttempts,
                        it.cause.getMessage(stateContext.resources),
                    )
                },
            )
        }

    private companion object {
        private const val TAG = "PermissionGrantedBasicInput"
    }

    override fun toString() = TAG
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

    override fun toString() = TAG
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
                stateContext.log.i(
                    TAG, "Extracted point with coordinates $points from $match"
                )
                ConversionSucceeded(stateContext, source, points)
            } else if (nextStep != null) {
                stateContext.log.i(
                    TAG, "Failed to extract point with coordinates from $match, going to next step"
                )
                when (nextStep) {
                    is NextStep.NextInput ->
                        InputFound(stateContext, source, nextStep.match, nextStep.input, permission, prevResult = this)

                    is NextStep.NextSource ->
                        // TODO Test
                        // TODO Overwrites original link
                        // TODO Loses permission and previous result
                        SourceReceived(stateContext, nextStep.source)
                }

            } else if (points.lastOrNull()?.hasName() == true) {
                stateContext.log.i(
                    TAG, "Extracted point with name $points from $match"
                )
                ConversionSucceeded(stateContext, source, points)
            } else if (prevResult?.points?.lastOrNull()?.run { hasCoordinates() || hasName() } == true) {
                stateContext.log.i(
                    TAG, "Failed to extract point from $match, using previous result ${prevResult.points}"
                )
                ConversionSucceeded(stateContext, source, prevResult.points)
            } else if (permission == Permission.NEVER) {
                stateContext.log.i(
                    TAG, "Failed to extract point from $match, because permission was denied"
                )
                ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_connection_permission_denied),
                    source,
                )
            } else {
                stateContext.log.i(
                    TAG, "Failed to extract point from $match"
                )
                ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_reason_no_points),
                    source,
                )
            }
        }

    private companion object {
        private const val TAG = "DataParsed"
    }

    override fun toString() = TAG
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
            stateContext.log.w(TAG, "Billing status didn't appear within $billingStatusTimeout")
            stateContext.userPreferencesRepository.getValue(CachedPurchasePreference)
                ?.let { cachedPurchase ->
                    stateContext.billing.products.firstOrNull { product -> cachedPurchase.productId == product.id }
                        ?.let { product ->
                            stateContext.log.w(TAG, "Found cached billing status")
                            BillingStatus.Purchased(
                                product,
                                expired = false,
                                refundable = true,
                                token = cachedPurchase.token,
                            )
                        }
                }
                ?: run {
                    stateContext.log.w(TAG, "Didn't find cached billing status")
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
                return ActionWaiting(stateContext, source, points, action, output, isAutomation = true, delay = delay)
            }
            return ActionReady(source, points, action, isAutomation = true)
        }
        return null
    }

    private companion object {
        private const val TAG = "ConversionSucceeded"
    }

    override fun toString() = TAG
}

data class ConversionFailed(
    override val message: String,
    override val source: String,
) : ConversionState, ConversionState.HasError {
    override fun toString() = "ConversionFailed"
}

data class ActionWaiting(
    val stateContext: ConversionStateContext,
    override val source: String,
    override val points: Points,
    val action: Action<*>,
    val output: Output.HasAutomationDelay,
    @Suppress("SameParameterValue") val isAutomation: Boolean,
    val delay: Duration,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = try {
        if (delay.isPositive()) {
            delay(delay)
        }
        ActionReady(source, points, action, isAutomation)
    } catch (_: CancellationException) {
        ActionFinished(source, points, ActionResult.Failed)
    }

    override fun toString() = "ActionWaiting"
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

    override fun toString() = "ActionReady"
}

data class BasicActionReady(
    override val source: String,
    override val points: Points,
    val action: BasicAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "BasicActionReady"
}

data class FileActionReady(
    override val source: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
    val uri: Uri,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "FileActionReady"
}

data class LocationActionReady(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "LocationActionReady"
}

data class ActionRan(
    override val source: String,
    override val points: Points,
    val action: Action<*>,
    val actionResult: ActionResult,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = action.output.let { output ->
        if (!isAutomation) {
            when (actionResult) {
                ActionResult.Succeeded, ActionResult.SucceededAndFinish ->
                    if (output is Output.HasSuccessText) {
                        ActionSucceeded(source, points, actionResult, output)
                    } else {
                        ActionFinished(source, points, actionResult)
                    }

                ActionResult.Failed ->
                    if (output is Output.HasErrorText) {
                        ActionFailed(source, points, actionResult, output)
                    } else {
                        ActionFinished(source, points, actionResult)
                    }
            }
        } else {
            when (actionResult) {
                ActionResult.Succeeded, ActionResult.SucceededAndFinish ->
                    if (output is Output.HasAutomationSuccessText) {
                        ActionAutomationSucceeded(source, points, actionResult, output)
                    } else {
                        ActionFinished(source, points, actionResult)
                    }

                ActionResult.Failed ->
                    if (output is Output.HasAutomationErrorText) {
                        ActionAutomationFailed(source, points, actionResult, output)
                    } else {
                        ActionFinished(source, points, actionResult)
                    }
            }
        }
    }

    override fun toString() = "ActionRan"
}

data class ActionSucceeded(
    override val source: String,
    override val points: Points,
    val actionResult: ActionResult,
    val output: Output.HasSuccessText,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(source, points, actionResult)
    }

    override fun toString() = "ActionSucceeded"
}

data class ActionAutomationSucceeded(
    override val source: String,
    override val points: Points,
    val actionResult: ActionResult,
    val output: Output.HasAutomationSuccessText,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(source, points, actionResult)
    }

    override fun toString() = "ActionAutomationSucceeded"
}

data class ActionFailed(
    override val source: String,
    override val points: Points,
    val actionResult: ActionResult,
    val output: Output.HasErrorText,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(source, points, actionResult)
    }

    override fun toString() = "ActionFailed"
}

data class ActionAutomationFailed(
    override val source: String,
    override val points: Points,
    val actionResult: ActionResult,
    val output: Output.HasAutomationErrorText,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(source, points, actionResult)
    }

    override fun toString() = "ActionAutomationFailed"
}

data class ActionFinished(
    override val source: String,
    override val points: Points,
    val actionResult: ActionResult, // = ActionResult.Succeeded,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "ActionFinished"
}

data class FileUriRequested(
    override val source: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "FileUriRequested"
}

data class LocationRationaleRequested(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "LocationRationaleRequested"
}

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
        ActionFinished(source, points, ActionResult.Failed)

    override fun toString() = "LocationRationaleShown"
}

data class LocationRationaleConfirmed(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "LocationRationaleConfirmed"
}

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

    override fun toString() = "LocationPermissionReceived"
}

data class LocationReceived(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point?,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = if (location == null) {
        LocationFindingFailed(source, points, ActionResult.Failed)
    } else {
        LocationActionReady(source, points, action, isAutomation, location)
    }

    override fun toString() = "LocationReceived"
}

data class LocationFindingFailed(
    override val source: String,
    override val points: Points,
    val actionResult: ActionResult,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(source, points, actionResult)
    }

    override fun toString() = "LocationFindingFailed"
}

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
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.WebViewInput
import page.ooooo.geoshare.lib.inputs.merge
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
        val source: String
        val message: String
        val details: String?
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
                source,
                stateContext.resources.getString(R.string.conversion_failed_missing_url),
            )
        }
        for (input in stateContext.inputs) {
            val match = input.match(source)
            if (match != null) {
                return InputFound(stateContext, source, match, input)
            }
        }
        return ConversionFailed(
            source,
            stateContext.resources.getString(R.string.conversion_failed_unsupported_service),
        )
    }

    private companion object {
        private const val TAG = "SourceReceived"
    }

    override fun toString() = "$TAG(source=$source)"
}

data class InputFound(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input,
    val permission: Permission? = null,
    val results: List<ParseResult> = emptyList(),
) : ConversionState {
    override suspend fun transition(): State {
        return if (input is Input.HasPermission) {
            when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                Permission.ALWAYS -> PermissionGranted(
                    stateContext, source, match, input, Permission.ALWAYS, results
                )

                Permission.ASK -> PermissionRequested(
                    stateContext, source, match, input, results, input.permissionTitleResId
                )

                Permission.NEVER -> PermissionDenied(
                    stateContext, source, match, input, results
                )
            }
        } else {
            PermissionGranted(stateContext, source, match, input, permission, results)
        }
    }

    private companion object {
        private const val TAG = "InputFound"
    }

    override fun toString() =
        "$TAG(source=$source, match=$match, input=$input, permission=$permission, results=$results)"
}

data class PermissionRequested(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input,
    val results: List<ParseResult> = emptyList(),
    override val permissionTitleResId: Int,
) : ConversionState, ConversionState.HasPermission {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return PermissionGranted(stateContext, source, match, input, Permission.ALWAYS, results)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return PermissionDenied(stateContext, source, match, input, results)
    }

    private companion object {
        private const val TAG = "PermissionRequested"
    }

    override fun toString() = "$TAG(source=$source, match=$match, input=$input, results=$results)"
}

data class PermissionGranted(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input,
    val permission: Permission?,
    val results: List<ParseResult> = emptyList(),
) : ConversionState {
    override suspend fun transition(): State =
        when (input) {
            is BasicInput<*> ->
                PermissionGrantedBasicInput(stateContext, source, match, input, permission, results)

            is WebViewInput ->
                PermissionGrantedWebViewInput(stateContext, source, match, input, permission, results)
        }

    private companion object {
        private const val TAG = "PermissionGranted"
    }

    override fun toString() =
        "$TAG(source=$source, match=$match, input=$input, permission=$permission, results=$results)"
}

/**
 * Fetches input data using [BasicInput.fetch] and parses it using [BasicInput.parse].
 *
 * When [BasicInput.fetch] fails, it is retried up to [maxAttempts]. Retrying is done by recursively transitioning
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
    val results: List<ParseResult> = emptyList(),
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
                val result = input.fetch(match) { data ->
                    input.parse(data, match)
                }
                DataParsed(stateContext, source, match, input, permission, listOf(result) + results)
            } catch (_: MalformedURLException) {
                ConversionFailed(
                    source,
                    stateContext.resources.getString(R.string.conversion_failed_reason_invalid_url),
                )
            } catch (tr: RecoverableNetworkException) {
                val attempt = Attempt(attemptNumber, tr)
                PermissionGrantedBasicInput(
                    stateContext, source, match, input, permission, results, attempt, maxAttempts
                )
            } catch (tr: UnrecoverableNetworkException) {
                ConversionFailed(
                    source,
                    tr.getMessage(stateContext.resources),
                    details = tr.getDetails(),
                )
            }
        }
    } catch (_: CancellationException) {
        // Cancellation must be caught outside withContext, because withContext somehow rethrows errors
        ConversionFailed(
            source,
            stateContext.resources.getString(R.string.conversion_failed_cancelled),
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

    override fun toString() =
        "$TAG(source=$source, match=$match, input=$input, permission=$permission, results=$results, lastAttempt=$lastAttempt)"
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
    val results: List<ParseResult> = emptyList(),
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
                val result = input.parse(data, match)
                DataParsed(stateContext, source, match, input, permission, listOf(result) + results)
            } catch (_: TimeoutCancellationException) {
                stateContext.log.e(TAG, "Timed out")
                ConversionFailed(
                    source,
                    stateContext.resources.getString(R.string.conversion_failed_reason_timeout),
                )
            }
        }
    } catch (_: CancellationException) {
        // Cancellation must be caught outside withContext, because withContext somehow rethrows errors
        ConversionFailed(
            source,
            stateContext.resources.getString(R.string.conversion_failed_cancelled),
        )
    }

    override fun getLoadingIndicator() =
        LoadingIndicator.Large(stateContext.resources.getString(input.loadingIndicatorTitleResId))

    private companion object {
        private const val TAG = "PermissionGrantedWebViewInput"
    }

    override fun toString() =
        "$TAG(source=$source, match=$match, input=$input, permission=$permission, results=$results)"
}

// TODO Test
data class PermissionDenied(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input,
    val results: List<ParseResult>,
) : ConversionState {
    override suspend fun transition() =
        DataParsed(stateContext, source, match, input, Permission.NEVER, listOf(ParseResult()) + results)

    private companion object {
        private const val TAG = "PermissionDenied"
    }

    override fun toString() = "$TAG(source=$source, match=$match, input=$input, results=$results)"
}

data class DataParsed(
    val stateContext: ConversionStateContext,
    val source: String,
    val match: String,
    val input: Input,
    val permission: Permission?,
    val results: List<ParseResult>,
) : ConversionState {
    override suspend fun transition(): State =
        results.merge().run {
            if (points.lastOrNull()?.hasCoordinates() == true) {
                stateContext.log.i(
                    TAG, "Extracted point with coordinates $points from $match"
                )
                ConversionSucceeded(stateContext, source, points)
            } else if (nextStep != null) {
                stateContext.log.i(
                    TAG, "Failed to extract point with coordinates from $match, going to next step"
                )
                InputFound(stateContext, source, nextStep.match, nextStep.input, permission, results)
            } else if (points.lastOrNull()?.hasName() == true) {
                stateContext.log.i(
                    TAG, "Extracted point with name $points from $match"
                )
                ConversionSucceeded(stateContext, source, points)
            } else if (permission == Permission.NEVER) {
                stateContext.log.i(
                    TAG, "Failed to extract point from $match, because permission was denied"
                )
                ConversionFailed(
                    source,
                    stateContext.resources.getString(R.string.conversion_failed_connection_permission_denied),
                )
            } else {
                stateContext.log.i(
                    TAG, "Failed to extract point from $match"
                )
                ConversionFailed(
                    source,
                    stateContext.resources.getString(R.string.conversion_failed_reason_no_points),
                )
            }
        }

    private companion object {
        private const val TAG = "DataParsed"
    }

    override fun toString() =
        "$TAG(source=$source, match=$match, input=$input, permission=$permission, results=$results)"
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

    override fun toString() = "$TAG(source=$source, points=$points)"
}

data class ConversionFailed(
    override val source: String,
    override val message: String,
    override val details: String? = null,
) : ConversionState, ConversionState.HasError {
    private companion object {
        private const val TAG = "ConversionFailed"
    }

    override fun toString() = "$TAG(source=$source, message=$message)"
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

    private companion object {
        private const val TAG = "ActionWaiting"
    }

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"
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

    private companion object {
        private const val TAG = "ActionReady"
    }

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"
}

data class BasicActionReady(
    override val source: String,
    override val points: Points,
    val action: BasicAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    private companion object {
        private const val TAG = "BasicActionReady"
    }

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"
}

data class FileActionReady(
    override val source: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
    val uri: Uri,
) : ConversionState, ConversionState.HasResult {
    private companion object {
        private const val TAG = "FileActionReady"
    }

    override fun toString() =
        "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation, uri=$uri)"
}

data class LocationActionReady(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point,
) : ConversionState, ConversionState.HasResult {
    private companion object {
        private const val TAG = "LocationActionReady"
    }

    override fun toString() =
        "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation, location=$location)"
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

    private companion object {
        private const val TAG = "ActionRan"
    }

    override fun toString() =
        "$TAG(source=$source, points=$points, action=$action, actionResult=$actionResult, isAutomation=$isAutomation)"
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

    private companion object {
        private const val TAG = "ActionSucceeded"
    }

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"
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

    private companion object {
        private const val TAG = "ActionAutomationSucceeded"
    }

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"
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

    private companion object {
        private const val TAG = "ActionFailed"
    }

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"
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

    private companion object {
        private const val TAG = "ActionAutomationFailed"
    }

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"
}

data class ActionFinished(
    override val source: String,
    override val points: Points,
    val actionResult: ActionResult,
) : ConversionState, ConversionState.HasResult {
    private companion object {
        private const val TAG = "ActionFinished"
    }

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"
}

data class FileUriRequested(
    override val source: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    private companion object {
        private const val TAG = "FileUriRequested"
    }

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"
}

data class LocationRationaleRequested(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    private companion object {
        private const val TAG = "LocationRationaleRequested"
    }

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"
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

    private companion object {
        private const val TAG = "LocationRationaleShown"
    }

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"
}

data class LocationRationaleConfirmed(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    private companion object {
        private const val TAG = "LocationRationaleConfirmed"
    }

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"
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

    private companion object {
        private const val TAG = "LocationPermissionReceived"
    }

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"
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

    private companion object {
        private const val TAG = "LocationReceived"
    }

    override fun toString() =
        "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation, location=$location)"
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

    private companion object {
        private const val TAG = "LocationFindingFailed"
    }

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"
}

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
import page.ooooo.geoshare.lib.inputs.SyncInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.NoopInput
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.AsyncInput
import page.ooooo.geoshare.lib.inputs.FetchResult
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

    interface HasSource {
        val source: String
    }

    interface HasError : HasSource {
        val message: String
        val details: String?
    }

    interface HasResult : HasSource {
        val points: Points
    }

    interface HasPermission : HasSource {
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

typealias Results = Map<MatchedInput, ParseResult>

data class SourceReceived(
    val stateContext: ConversionStateContext,
    override val source: String,
) : ConversionState, ConversionState.HasSource {
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
                return InputMatched(stateContext, source, MatchedInput(input, match))
            }
        }
        return ConversionFailed(
            source,
            stateContext.resources.getString(R.string.conversion_failed_unsupported_service),
        )
    }

    override fun toString() = "$TAG(source=$source)"

    private companion object {
        private const val TAG = "SourceReceived"
    }
}

data class InputMatched(
    val stateContext: ConversionStateContext,
    override val source: String,
    val matchedInput: MatchedInput,
    val permission: Permission? = null,
    val results: Results = emptyMap(),
) : ConversionState, ConversionState.HasSource {
    override suspend fun transition(): State {
        return if (matchedInput.input is Input.HasPermission) {
            when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                Permission.ALWAYS -> PermissionGranted(
                    stateContext, source, matchedInput, Permission.ALWAYS, results
                )

                Permission.ASK -> PermissionRequested(
                    stateContext, source, matchedInput, results, matchedInput.input.permissionTitleResId
                )

                Permission.NEVER -> PermissionDenied(
                    stateContext, source, matchedInput, results
                )
            }
        } else {
            PermissionGranted(stateContext, source, matchedInput, permission, results)
        }
    }

    override fun toString() =
        "$TAG(source=$source, matchedInput=$matchedInput, permission=$permission, results=$results)"

    private companion object {
        private const val TAG = "InputMatched"
    }
}

data class PermissionRequested(
    val stateContext: ConversionStateContext,
    override val source: String,
    val matchedInput: MatchedInput,
    val results: Results = emptyMap(),
    override val permissionTitleResId: Int,
) : ConversionState, ConversionState.HasPermission {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return PermissionGranted(stateContext, source, matchedInput, Permission.ALWAYS, results)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return PermissionDenied(stateContext, source, matchedInput, results)
    }

    override fun toString() = "$TAG(source=$source, matchedInput=$matchedInput, results=$results)"

    private companion object {
        private const val TAG = "PermissionRequested"
    }
}

/**
 * Fetches and parses a [matchedInput].
 *
 * If the input is a [SyncInput], it calls [SyncInput.fetch] and [SyncInput.parse] immediately.
 *
 * If the input is an [AsyncInput], it waits for [setFetchResult] to be called and only then proceeds to
 * [AsyncInput.parse]. This is useful for [page.ooooo.geoshare.lib.inputs.WebViewInput], because in that case the
 * conversion halts, waits for the UI layer to render a WebView, and resumes once data is extracted from the web page.
 *
 * When fetching data fails, it is retried up to [maxAttempts]. Retrying is done by recursively transitioning this state
 * while tracking the number of attempts made and the cause of the last failure in [lastAttempt].
 *
 * We use this custom retrying instead of the standard [io.ktor.client.plugins.HttpRequestRetry] plugin, because our
 * custom retrying changes the current conversion state, which allows the UI to react to it and show the user a message
 * about the progress of the retrying.
 */
data class PermissionGranted(
    val stateContext: ConversionStateContext,
    override val source: String,
    val matchedInput: MatchedInput,
    val permission: Permission?,
    val results: Results,
    val lastAttempt: Attempt<RecoverableNetworkException>? = null,
    val maxAttempts: Int = 10,
    val dispatcher: CoroutineContext = Dispatchers.Default,
) : ConversionState, ConversionState.HasSource, ConversionState.HasLargeLoadingIndicator {
    private val fetchResultFlow = MutableStateFlow<FetchResult<String>?>(null)

    fun setFetchResult(fetchResult: FetchResult<String>) {
        fetchResultFlow.value = fetchResult
    }

    @OptIn(FlowPreview::class)
    override suspend fun transition(): State = try {
        withContext(dispatcher) {
            val attemptNumber = lastAttempt?.number?.plus(1) ?: 1
            try {
                if (lastAttempt != null && lastAttempt.number >= maxAttempts) {
                    stateContext.log.w(TAG, "Maximum number of $maxAttempts attempts reached for $matchedInput")
                    throw MaxAttemptsReachedNetworkException(lastAttempt.cause)
                }
                val delayMillis = calcExponentialBackoffMillis(attemptNumber)
                if (delayMillis > 0) {
                    stateContext.log.i(
                        TAG, "Waiting ${delayMillis}ms before attempt $attemptNumber of $maxAttempts for $matchedInput"
                    )
                    delay(delayMillis)
                }
                when (matchedInput.input) {
                    is SyncInput<*> -> {
                        val result = matchedInput.input.fetch(matchedInput.match) { data ->
                            matchedInput.input.parse(data, matchedInput.match)
                        }
                        DataParsed(stateContext, source, matchedInput, permission, results + (matchedInput to result))
                    }

                    is AsyncInput -> {
                        val fetchResult = fetchResultFlow
                            .filterNotNull()
                            .timeout(matchedInput.input.timeout)
                            .first()
                        val data = when (fetchResult) {
                            is FetchResult.Success<String> -> fetchResult.data
                            is FetchResult.Failure<String> -> throw fetchResult.exception
                        }
                        val result = matchedInput.input.parse(data, matchedInput.match)
                        DataParsed(stateContext, source, matchedInput, permission, results + (matchedInput to result))
                    }

                    is NoopInput -> DataParsed(
                        stateContext, source, matchedInput, permission, results + (matchedInput to ParseResult())
                    )
                }
            } catch (_: MalformedURLException) {
                ConversionFailed(
                    source,
                    stateContext.resources.getString(R.string.conversion_failed_reason_invalid_url),
                )
            } catch (tr: RecoverableNetworkException) {
                val attempt = Attempt(attemptNumber, tr)
                PermissionGranted(
                    stateContext, source, matchedInput, permission, results, attempt, maxAttempts
                )
            } catch (tr: UnrecoverableNetworkException) {
                ConversionFailed(
                    source,
                    tr.getMessage(stateContext.resources),
                    details = tr.getDetails(),
                )
            } catch (_: TimeoutCancellationException) {
                stateContext.log.w(TAG, "Timed out")
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
        (matchedInput.input as? Input.HasPermission)?.loadingIndicatorTitleResId?.let { loadingIndicatorTitleResId ->
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

    override fun toString() =
        "$TAG(source=$source, matchedInput=$matchedInput, permission=$permission, results=$results, lastAttempt=$lastAttempt)"

    private companion object {
        private const val TAG = "PermissionGranted"
    }
}

data class PermissionDenied(
    val stateContext: ConversionStateContext,
    override val source: String,
    val matchedInput: MatchedInput,
    val results: Results,
) : ConversionState, ConversionState.HasSource {
    override suspend fun transition() =
        DataParsed(stateContext, source, matchedInput, Permission.NEVER, results + (matchedInput to ParseResult()))

    override fun toString() = "$TAG(source=$source, matchedInput=$matchedInput, results=$results)"

    private companion object {
        private const val TAG = "PermissionDenied"
    }
}

data class DataParsed(
    val stateContext: ConversionStateContext,
    override val source: String,
    val matchedInput: MatchedInput,
    val permission: Permission?,
    val results: Results,
) : ConversionState, ConversionState.HasSource {
    override suspend fun transition(): State =
        results.values.reversed().merge().run {
            if (points.lastOrNull()?.hasCoordinates() == true) {
                stateContext.log.i(
                    TAG, "Extracted coordinates $points from $matchedInput"
                )
                ConversionSucceeded(stateContext, source, points)
            } else if (next != null) {
                if (next in results) {
                    stateContext.log.w(
                        TAG,
                        "Failed to extract point with coordinates from $matchedInput and next matched input creates a loop"
                    )
                    ConversionFailed(source, matchedInput.input.getErrorMessage(stateContext.resources))
                } else {
                    stateContext.log.i(
                        TAG, "Failed to extract point with coordinates from $matchedInput, going to next matched input"
                    )
                    InputMatched(stateContext, source, next, permission, results)
                }
            } else if (points.lastOrNull()?.hasName() == true) {
                stateContext.log.i(
                    TAG, "Extracted point with name $points from $matchedInput"
                )
                ConversionSucceeded(stateContext, source, points)
            } else if (permission == Permission.NEVER) {
                stateContext.log.i(
                    TAG, "Failed to extract point from $matchedInput, because permission was denied"
                )
                ConversionFailed(
                    source,
                    stateContext.resources.getString(R.string.conversion_failed_connection_permission_denied),
                )
            } else {
                stateContext.log.i(
                    TAG, "Failed to extract point from $matchedInput"
                )
                ConversionFailed(source, matchedInput.input.getErrorMessage(stateContext.resources))
            }
        }

    override fun toString() =
        "$TAG(source=$source, matchedInput=$matchedInput, permission=$permission, results=$results)"

    private companion object {
        private const val TAG = "DataParsed"
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

    override fun toString() = "$TAG(source=$source, points=$points)"

    private companion object {
        private const val TAG = "ConversionSucceeded"
    }
}

data class ConversionFailed(
    override val source: String,
    override val message: String,
    override val details: String? = null,
) : ConversionState, ConversionState.HasError {
    override fun toString() = "$TAG(source=$source, message=$message)"

    private companion object {
        private const val TAG = "ConversionFailed"
    }
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

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "ActionWaiting"
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

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "ActionReady"
    }
}

data class BasicActionReady(
    override val source: String,
    override val points: Points,
    val action: BasicAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "BasicActionReady"
    }
}

data class FileActionReady(
    override val source: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
    val uri: Uri,
) : ConversionState, ConversionState.HasResult {
    override fun toString() =
        "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation, uri=$uri)"

    private companion object {
        private const val TAG = "FileActionReady"
    }
}

data class LocationActionReady(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point,
) : ConversionState, ConversionState.HasResult {
    override fun toString() =
        "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation, location=$location)"

    private companion object {
        private const val TAG = "LocationActionReady"
    }
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

    override fun toString() =
        "$TAG(source=$source, points=$points, action=$action, actionResult=$actionResult, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "ActionRan"
    }
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

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"

    private companion object {
        private const val TAG = "ActionSucceeded"
    }
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

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"

    private companion object {
        private const val TAG = "ActionAutomationSucceeded"
    }
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

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"

    private companion object {
        private const val TAG = "ActionFailed"
    }
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

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"

    private companion object {
        private const val TAG = "ActionAutomationFailed"
    }
}

data class ActionFinished(
    override val source: String,
    override val points: Points,
    val actionResult: ActionResult,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"

    private companion object {
        private const val TAG = "ActionFinished"
    }
}

data class FileUriRequested(
    override val source: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "FileUriRequested"
    }
}

data class LocationRationaleRequested(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "LocationRationaleRequested"
    }
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

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "LocationRationaleShown"
    }
}

data class LocationRationaleConfirmed(
    override val source: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "LocationRationaleConfirmed"
    }
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

    override fun toString() = "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "LocationPermissionReceived"
    }
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

    override fun toString() =
        "$TAG(source=$source, points=$points, action=$action, isAutomation=$isAutomation, location=$location)"

    private companion object {
        private const val TAG = "LocationReceived"
    }
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

    override fun toString() = "$TAG(source=$source, points=$points, actionResult=$actionResult)"

    private companion object {
        private const val TAG = "LocationFindingFailed"
    }
}

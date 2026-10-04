package page.ooooo.geoshare.lib.state

import android.content.res.Resources
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.calcExponentialBackoffMillis
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.inputs.BasicInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.NoopInput
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.WebViewInput
import page.ooooo.geoshare.lib.inputs.merge
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.UnrecoverableNetworkException
import java.net.MalformedURLException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

interface ConversionState : State<ConversionStateContext> {
    override suspend fun transition(stateContext: ConversionStateContext): ConversionState? = null

    interface HasSource {
        val source: String
    }

    interface HasDescription {
        fun getDescription(resources: Resources): String
        fun getDetails(resources: Resources): String? = null
        fun getLoadingIndicatorTitle(resources: Resources): String? = null

        @Suppress("SameReturnValue")
        val uriString: String? get() = null
    }

    interface HasError : HasSource {
        val message: String
        val stackTrace: String?
        val warning: Boolean
    }

    interface HasResult : HasSource {
        val points: Points
    }

    interface HasPermission : State.HasPermission<ConversionStateContext> {
        override suspend fun grant(stateContext: ConversionStateContext, doNotAsk: Boolean): ConversionState
        override suspend fun deny(stateContext: ConversionStateContext, doNotAsk: Boolean): ConversionState
    }

    interface HasAttempt : HasSource {
        val lastAttempt: Attempt<RecoverableNetworkException>?
    }

    object Initial : ConversionState {
        override fun toString() = "Initial"
    }
}

data class ConversionStateContext(
    val inputs: List<Input> = emptyList(),
    val log: Log = DefaultLog,
    val resources: Resources,
    val uriQuote: UriQuote = DefaultUriQuote,
    val userPreferencesRepository: UserPreferencesRepository,
)

typealias Results = Map<MatchedInput<*>, ParseResult.Success>

data class SourceReceived(
    override val source: String,
) : ConversionState, ConversionState.HasSource {
    override suspend fun transition(stateContext: ConversionStateContext): ConversionState {
        if (source.isEmpty()) {
            return ConversionFailed(
                source,
                stateContext.resources.getString(R.string.conversion_failed_missing_url),
            )
        }
        for (input in stateContext.inputs) {
            val match = input.match(source)
            if (match != null) {
                return InputMatched(source, MatchedInput(input, match))
            }
        }
        return ConversionFailed(
            source,
            stateContext.resources.getString(R.string.conversion_failed_unsupported_service),
        )
    }

    override fun toString() = "SourceReceived(source=$source)"
}

data class InputMatched(
    override val source: String,
    val matchedInput: MatchedInput<*>,
    val permission: Permission? = null,
    val results: Results = emptyMap(),
) : ConversionState, ConversionState.HasSource {
    override suspend fun transition(stateContext: ConversionStateContext): ConversionState? {
        return if (matchedInput.input is Input.HasPermission) {
            when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                Permission.ALWAYS -> PermissionGranted(source, matchedInput, Permission.ALWAYS, results)
                Permission.ASK -> PermissionRequested(source, matchedInput, results)
                Permission.NEVER -> PermissionDenied(source, matchedInput, results)
            }
        } else {
            PermissionGranted(source, matchedInput, permission, results)
        }
    }

    override fun toString() =
        "InputMatched(source=$source, matchedInput=$matchedInput, permission=$permission, results=$results)"
}

data class PermissionRequested(
    override val source: String,
    val matchedInput: MatchedInput<*>,
    val results: Results = emptyMap(),
) : ConversionState, ConversionState.HasPermission, ConversionState.HasSource {
    override suspend fun grant(stateContext: ConversionStateContext, doNotAsk: Boolean): ConversionState {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return PermissionGranted(source, matchedInput, Permission.ALWAYS, results)
    }

    override suspend fun deny(stateContext: ConversionStateContext, doNotAsk: Boolean): ConversionState {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return PermissionDenied(source, matchedInput, results)
    }

    override fun toString() = "PermissionRequested(source=$source, matchedInput=$matchedInput, results=$results)"
}

data class PermissionGranted(
    override val source: String,
    val matchedInput: MatchedInput<*>,
    val permission: Permission?,
    val results: Results = emptyMap(),
) : ConversionState, ConversionState.HasSource {
    override suspend fun transition(stateContext: ConversionStateContext): ConversionState =
        when (matchedInput.input) {
            is BasicInput<*> ->
                PermissionGrantedBasicInput(
                    source, MatchedInput(matchedInput.input, matchedInput.match), permission, results
                )

            is WebViewInput ->
                PermissionGrantedWebViewInput(
                    source, MatchedInput(matchedInput.input, matchedInput.match), permission, results
                )

            is NoopInput ->
                DataParsed(source, matchedInput, permission, results + (matchedInput to ParseResult.Success()))
        }

    override fun toString() =
        "PermissionGranted(source=$source, matchedInput=$matchedInput, permission=$permission, results=$results)"
}

/**
 * Fetches and parses a match using [BasicInput].
 *
 * When it fails, it retries up to [maxAttempts] times. Retrying is done by recursively transitioning this state while
 * tracking the number of attempts made and the cause of the last failure in [lastAttempt].
 *
 * We use this custom retrying instead of the standard [io.ktor.client.plugins.HttpRequestRetry] plugin, because our
 * custom retrying changes the current conversion state, which allows the UI to react to it and show the user a message
 * about the progress of the retrying.
 */
data class PermissionGrantedBasicInput<T>(
    override val source: String,
    val matchedInput: MatchedInput<BasicInput<T>>,
    val permission: Permission?,
    val results: Results,
    override val lastAttempt: Attempt<RecoverableNetworkException>? = null,
    val maxAttempts: Int = 10,
    val dispatcher: CoroutineContext = Dispatchers.Default,
) : ConversionState, ConversionState.HasSource, ConversionState.HasDescription, ConversionState.HasAttempt {
    override suspend fun transition(stateContext: ConversionStateContext): ConversionState = try {
        withContext(dispatcher) {
            val attemptNumber = lastAttempt?.number?.plus(1) ?: 1
            try {
                if (lastAttempt != null && lastAttempt.number >= maxAttempts) {
                    stateContext.log.w(TAG, "Maximum number of $maxAttempts attempts reached for $matchedInput")
                    ConversionFailed(
                        source, lastAttempt.cause.getMessage(stateContext.resources), lastAttempt.cause.getDetails()
                    )
                } else {
                    val delayMillis = calcExponentialBackoffMillis(attemptNumber)
                    if (delayMillis > 0) {
                        stateContext.log.i(
                            TAG,
                            "Waiting ${delayMillis}ms before attempt $attemptNumber of $maxAttempts for $matchedInput"
                        )
                        delay(delayMillis.milliseconds)
                    }
                    when (
                        val result = matchedInput.input.fetch(matchedInput.match) { data ->
                            matchedInput.input.parse(data, matchedInput.match, stateContext.resources)
                        }
                    ) {
                        is ParseResult.Success -> DataParsed(
                            source, matchedInput, permission, results + (matchedInput to result)
                        )

                        is ParseResult.Warning -> ConversionFailed(source, result.message, warning = true)
                    }
                }
            } catch (_: MalformedURLException) {
                ConversionFailed(
                    source, stateContext.resources.getString(R.string.conversion_failed_reason_invalid_url)
                )
            } catch (tr: RecoverableNetworkException) {
                val attempt = Attempt(attemptNumber, tr)
                PermissionGrantedBasicInput(source, matchedInput, permission, results, attempt, maxAttempts)
            } catch (tr: UnrecoverableNetworkException) {
                ConversionFailed(source, tr.getMessage(stateContext.resources), tr.getDetails())
            }
        }
    } catch (_: CancellationException) {
        // Cancellation must be caught outside withContext, because withContext somehow rethrows errors
        ConversionFailed(source, stateContext.resources.getString(R.string.conversion_failed_cancelled))
    }

    override fun getDescription(resources: Resources) =
        resources.getString(R.string.conversion_processing, matchedInput.input.getName(resources))

    override fun getDetails(resources: Resources) = lastAttempt?.let {
        resources.getString(
            R.string.conversion_loading_indicator_description,
            it.number + 1,
            maxAttempts,
            it.cause.getMessage(resources),
        )
    }

    override fun getLoadingIndicatorTitle(resources: Resources) =
        if (matchedInput.input is Input.HasPermission) {
            resources.getString(R.string.conversion_connecting, matchedInput.input.group.getName(resources))
        } else {
            null
        }

    override val uriString = matchedInput.match

    override fun toString() =
        "$TAG(source=$source, matchedInput=$matchedInput, permission=$permission, results=$results, lastAttempt=$lastAttempt)"

    private companion object {
        private const val TAG = "PermissionGrantedBasicInput"
    }
}

/**
 * Waits for data to be provided and then parses it using [WebViewInput].
 *
 * When this state is the current state, the UI should:
 *
 * 1. Load [matchedInput]'s match as a page URL in a WebView.
 * 2. Get JavaScript code using [WebViewInput.getUnsafeExtractionJavaScript] and call it periodically.
 * 3. Once the result of the extraction JavaScript stops changing, complete [pendingData].
 *
 * When it fails, it retries up to [maxAttempts] times. Retrying is done by recursively transitioning this state while
 * tracking the number of attempts made and the cause of the last failure in [lastAttempt].
 *
 * We use this custom retrying instead of the standard [io.ktor.client.plugins.HttpRequestRetry] plugin, because our
 * custom retrying changes the current conversion state, which allows the UI to react to it and show the user a message
 * about the progress of the retrying.
 */
data class PermissionGrantedWebViewInput(
    override val source: String,
    val matchedInput: MatchedInput<WebViewInput>,
    val permission: Permission?,
    val results: Results,
    override val lastAttempt: Attempt<RecoverableNetworkException>? = null,
    val maxAttempts: Int = 3,
    val dispatcher: CoroutineContext = Dispatchers.Default,
) : ConversionState, ConversionState.HasSource, ConversionState.HasDescription, ConversionState.HasAttempt {
    val pendingData: CompletableDeferred<String> = CompletableDeferred()

    override suspend fun transition(stateContext: ConversionStateContext): ConversionState = try {
        withContext(dispatcher) {
            val attemptNumber = lastAttempt?.number?.plus(1) ?: 1
            try {
                if (lastAttempt != null && lastAttempt.number >= maxAttempts) {
                    stateContext.log.w(TAG, "Maximum number of $maxAttempts attempts reached for $matchedInput")
                    ConversionFailed(
                        source, lastAttempt.cause.getMessage(stateContext.resources), lastAttempt.cause.getDetails()
                    )
                } else {
                    val data = withTimeout(matchedInput.input.timeout) {
                        pendingData.await()
                    }
                    when (val result = matchedInput.input.parse(data, matchedInput.match, stateContext.resources)) {
                        is ParseResult.Success -> DataParsed(
                            source, matchedInput, permission, results + (matchedInput to result)
                        )

                        is ParseResult.Warning -> ConversionFailed(source, result.message, warning = true)
                    }
                }
            } catch (tr: RecoverableNetworkException) {
                val attempt = Attempt(attemptNumber, tr)
                PermissionGrantedWebViewInput(source, matchedInput, permission, results, attempt, maxAttempts)
            } catch (tr: UnrecoverableNetworkException) {
                ConversionFailed(source, tr.getMessage(stateContext.resources), tr.getDetails())
            } catch (_: TimeoutCancellationException) {
                stateContext.log.w(TAG, "Timed out")
                ConversionFailed(
                    source, stateContext.resources.getString(R.string.conversion_failed_reason_timeout)
                )
            }
        }
    } catch (_: CancellationException) {
        // Cancellation must be caught outside withContext, because withContext somehow rethrows errors
        ConversionFailed(source, stateContext.resources.getString(R.string.conversion_failed_cancelled))
    }

    override fun getDescription(resources: Resources) =
        resources.getString(R.string.conversion_processing, matchedInput.input.getName(resources))

    override fun getDetails(resources: Resources) = lastAttempt?.let {
        resources.getString(
            R.string.conversion_loading_indicator_description,
            it.number + 1,
            maxAttempts,
            it.cause.getMessage(resources),
        )
    }

    override fun getLoadingIndicatorTitle(resources: Resources) =
        resources.getString(R.string.conversion_connecting, matchedInput.input.group.getName(resources))

    override val uriString = matchedInput.match

    override fun toString() =
        "$TAG(source=$source, matchedInput=$matchedInput, permission=$permission, results=$results)"

    private companion object {
        private const val TAG = "PermissionGrantedWebViewInput"
    }
}

data class PermissionDenied(
    override val source: String,
    val matchedInput: MatchedInput<*>,
    val results: Results,
) : ConversionState, ConversionState.HasSource {
    override suspend fun transition(stateContext: ConversionStateContext) =
        DataParsed(source, matchedInput, Permission.NEVER, results + (matchedInput to ParseResult.Success()))

    override fun toString() = "PermissionDenied(source=$source, matchedInput=$matchedInput, results=$results)"
}

data class DataParsed(
    override val source: String,
    val matchedInput: MatchedInput<*>,
    val permission: Permission?,
    val results: Results,
) : ConversionState, ConversionState.HasSource {
    override suspend fun transition(stateContext: ConversionStateContext): ConversionState =
        results.values.reversed().merge().run {
            if (points.lastOrNull()?.hasCoordinates() == true) {
                stateContext.log.i(
                    TAG, "Extracted coordinates $points from $matchedInput"
                )
                ConversionSucceeded(source, points)
            } else if (next != null) {
                if (next in results) {
                    stateContext.log.w(
                        TAG,
                        "Failed to extract point with coordinates from $matchedInput and next matched input creates a loop"
                    )
                    ConversionFailed(
                        source,
                        stateContext.resources.getString(R.string.conversion_failed_reason_no_points),
                    )
                } else {
                    stateContext.log.i(
                        TAG, "Failed to extract point with coordinates from $matchedInput, going to next matched input"
                    )
                    InputMatched(source, next, permission, results)
                }
            } else if (points.lastOrNull()?.hasName() == true) {
                stateContext.log.i(
                    TAG, "Extracted point with name $points from $matchedInput"
                )
                ConversionSucceeded(source, points)
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
                ConversionFailed(
                    source,
                    stateContext.resources.getString(R.string.conversion_failed_reason_no_points),
                )
            }
        }

    override fun toString() =
        "$TAG(source=$source, matchedInput=$matchedInput, permission=$permission, results=$results)"

    private companion object {
        private const val TAG = "DataParsed"
    }
}

data class ConversionSucceeded(
    override val source: String,
    override val points: Points,
) : ConversionState, ConversionState.HasResult {
    override fun toString() = "ConversionSucceeded(source=$source, points=$points)"
}

data class ConversionFailed(
    override val source: String,
    override val message: String,
    override val stackTrace: String? = null,
    override val warning: Boolean = false,
) : ConversionState, ConversionState.HasError {
    override fun toString() = "ConversionFailed(source=$source, message=$message, warning=$warning)"
}

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
import page.ooooo.geoshare.lib.inputs.HtmlInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.ShortUriInput
import page.ooooo.geoshare.lib.inputs.WebInput
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ConversionState : State {
    override suspend fun transition(): State? = null

    interface HasError {
        val message: String
        val inputUriString: String
    }

    interface HasSmallLoadingIndicator {
        fun getSmallLoadingIndicator(): LoadingIndicator.Small
    }

    interface HasLargeLoadingIndicator {
        fun getLargeLoadingIndicator(): LoadingIndicator.Large
    }

    interface HasPermission {
        val permissionTitleResId: Int

        suspend fun grant(doNotAsk: Boolean): State
        suspend fun deny(doNotAsk: Boolean): State
    }

    interface HasResult {
        val inputUriString: String
        val points: Points
    }

    companion object {
        const val TAG = "ConversionState"
    }
}

class Initial : ConversionState

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
) : ConversionState {
    override suspend fun transition(): State {
        if (inputUriString.isEmpty()) {
            return ConversionFailed(
                stateContext.resources.getString(R.string.conversion_failed_missing_url),
                "",
            )
        }
        for (input in stateContext.inputs) {
            val uriString = input.uriPattern.find(inputUriString)?.groupOrNull()
            if (uriString != null) {
                val uri = Uri.parse(uriString, stateContext.uriQuote)
                return ReceivedUri(stateContext, inputUriString, input, uri, null)
            }
        }
        return ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_unsupported_service),
            inputUriString,
        )
    }
}

data class ReceivedUri(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input,
    val uri: Uri,
    val permission: Permission?,
) : ConversionState {
    override suspend fun transition(): State {
        if (input is ShortUriInput) {
            val shortUriString = input.shortUriPattern.find(uri.toString())?.value
            if (shortUriString != null) {
                val uri = Uri.parse(shortUriString, stateContext.uriQuote)
                return when (permission ?: stateContext.userPreferencesRepository.getValue(
                    ConnectionPermissionPreference
                )) {
                    Permission.ALWAYS -> GrantedUnshortenPermission(stateContext, inputUriString, input, uri)
                    Permission.ASK -> RequestedUnshortenPermission(stateContext, inputUriString, input, uri)
                    Permission.NEVER -> DeniedConnectionPermission(stateContext, inputUriString, input)
                }
            }
        }
        return UnshortenedUrl(stateContext, inputUriString, input, uri, permission)
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: ShortUriInput,
    val uri: Uri,
) : ConversionState, ConversionState.HasPermission {
    override val permissionTitleResId: Int = input.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return GrantedUnshortenPermission(stateContext, inputUriString, input, uri)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, inputUriString, input)
    }
}

data class GrantedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: ShortUriInput,
    val uri: Uri,
    val retry: NetworkTools.Retry? = null,
) : ConversionState, ConversionState.HasLargeLoadingIndicator {
    override suspend fun transition(): State {
        val url = uri.toUrl()
        if (url == null) {
            stateContext.log.e(null, "Unshorten: Failed to get URL for $uri")
            return ConversionFailed(
                stateContext.resources.getString(
                    R.string.conversion_failed_unshorten_error_with_reason,
                    stateContext.resources.getString(R.string.conversion_failed_reason_invalid_url),
                ),
                inputUriString,
            )
        }
        return try {
            val unshortenedUrlString = when (input.shortUriMethod) {
                ShortUriInput.Method.GET -> stateContext.networkTools.httpGetRedirectedUrlString(url, retry)
                ShortUriInput.Method.HEAD -> stateContext.networkTools.httpHeadLocationHeader(url, retry)
            }
            if (unshortenedUrlString != null) {
                val unshortenedUri = Uri.parse(unshortenedUrlString, stateContext.uriQuote).toAbsoluteUri(uri)
                stateContext.log.i(null, "Unshorten: Resolved short URI $uri to $unshortenedUri")
                UnshortenedUrl(stateContext, inputUriString, input, unshortenedUri, Permission.ALWAYS)
            } else {
                stateContext.log.w(null, "Unshorten: Missing location header for $url")
                ConversionFailed(
                    stateContext.resources.getString(
                        R.string.conversion_failed_unshorten_error_with_reason,
                        stateContext.resources.getString(R.string.conversion_failed_reason_missing_header),
                    ),
                    inputUriString,
                )
            }
        } catch (_: CancellationException) {
            ConversionFailed(stateContext.resources.getString(R.string.conversion_failed_cancelled), inputUriString)
        } catch (tr: RecoverableNetworkException) {
            GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                input,
                uri,
                retry = NetworkTools.Retry((retry?.count ?: 0) + 1, tr),
            )
        } catch (tr: UnrecoverableNetworkException) {
            ConversionFailed(
                stateContext.resources.getString(
                    R.string.conversion_failed_unshorten_error_with_reason,
                    stateContext.resources.getString(tr.messageResId),
                ),
                inputUriString,
            )
        }
    }

    override fun getLargeLoadingIndicator() = LoadingIndicator.Large(
        title = stateContext.resources.getString(input.loadingIndicatorTitleResId),
        description = retry?.let { retry ->
            stateContext.resources.getString(
                R.string.conversion_loading_indicator_description,
                retry.count + 1,
                NetworkTools.MAX_RETRIES + 1,
                stateContext.resources.getString(retry.tr.messageResId),
            )
        },
    )
}

data class DeniedConnectionPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input,
) : ConversionState {
    override suspend fun transition(): State =
        ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_connection_permission_denied),
            inputUriString,
        )
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input,
    val uri: Uri,
    val permission: Permission?,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ConversionState {
    override suspend fun transition(): State =
        withContext(dispatcher) { input.parseUri(uri, stateContext.uriQuote) }.run {
            if (points.lastOrNull()?.hasCoordinates() == true) {
                stateContext.log.i(null, "URI Pattern: Converted $uri to $points")
                ConversionSucceeded(stateContext, inputUriString, points)
            } else if (htmlUriString != null) {
                if (input is HtmlInput) {
                    when (permission
                        ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                        Permission.ALWAYS -> GrantedParseHtmlPermission(
                            stateContext, inputUriString, input, uri, points, htmlUriString
                        )

                        Permission.ASK -> RequestedParseHtmlPermission(
                            stateContext, inputUriString, input, uri, points, htmlUriString
                        )

                        Permission.NEVER -> DeniedParseHtmlPermission(stateContext, inputUriString, points)
                    }
                } else {
                    stateContext.log.e(null, "URI Pattern: Input doesn't support HTML parsing")
                    DeniedParseHtmlPermission(stateContext, inputUriString, points)
                }
            } else if (webUriString != null) {
                if (input is WebInput) {
                    when (permission
                        ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                        Permission.ALWAYS -> GrantedParseWebPermission(
                            stateContext, inputUriString, input, uri, points, webUriString
                        )

                        Permission.ASK -> RequestedParseWebPermission(
                            stateContext, inputUriString, input, uri, points, webUriString
                        )

                        Permission.NEVER -> DeniedParseHtmlPermission(stateContext, inputUriString, points)
                    }
                } else {
                    stateContext.log.e(null, "URI Pattern: Input doesn't support web parsing")
                    DeniedParseHtmlPermission(stateContext, inputUriString, points)
                }
            } else if (points.lastOrNull()?.hasName() == true) {
                stateContext.log.i(null, "URI Pattern: Converted $uri to $points")
                ConversionSucceeded(stateContext, inputUriString, points)
            } else {
                stateContext.log.i(null, "URI Pattern: Failed to parse $uri")
                ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_parse_url_error),
                    inputUriString,
                )
            }
        }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: HtmlInput,
    val uri: Uri,
    val pointsFromUri: Points,
    val htmlUriString: String,
) : ConversionState, ConversionState.HasPermission {
    override val permissionTitleResId: Int = input.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return GrantedParseHtmlPermission(stateContext, inputUriString, input, uri, pointsFromUri, htmlUriString)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return DeniedParseHtmlPermission(stateContext, inputUriString, pointsFromUri)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: HtmlInput,
    val uri: Uri,
    val pointsFromUri: Points,
    val htmlUriString: String,
    val retry: NetworkTools.Retry? = null,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ConversionState, ConversionState.HasLargeLoadingIndicator {
    override suspend fun transition(): State {
        val htmlUrl = Uri.parse(htmlUriString, stateContext.uriQuote).toUrl()
        if (htmlUrl == null) {
            stateContext.log.e(null, "HTML Pattern: Failed to get HTML URL for $uri")
            return ConversionFailed(
                stateContext.resources.getString(
                    R.string.conversion_failed_parse_html_error_with_reason,
                    stateContext.resources.getString(R.string.conversion_failed_reason_invalid_url),
                ),
                inputUriString
            )
        }
        stateContext.log.i(null, "HTML Pattern: Downloading $htmlUrl")
        return try {
            stateContext.networkTools.httpGetBodyAsByteReadChannel(
                htmlUrl,
                retry,
                dispatcher = Dispatchers.Default
            ) { channel ->
                input.parseHtml(
                    htmlUrl.toString(),
                    channel,
                    pointsFromUri,
                    uriQuote = stateContext.uriQuote,
                    log = stateContext.log,
                )
            }.run {
                if (points.lastOrNull()?.hasCoordinates() == true) {
                    stateContext.log.i(null, "HTML Pattern: Parsed $htmlUrl to $points")
                    ConversionSucceeded(stateContext, inputUriString, points)
                } else if (redirectUriString != null) {
                    stateContext.log.i(null, "HTML Pattern: Parsed $htmlUrl to redirect URI $redirectUriString")
                    val redirectUri = Uri.parse(redirectUriString, stateContext.uriQuote).toAbsoluteUri(uri)
                    ReceivedUri(stateContext, inputUriString, input, redirectUri, Permission.ALWAYS)
                } else if (webUriString != null) {
                    if (input is WebInput) {
                        stateContext.log.i(null, "HTML Pattern: URI $htmlUrl requires web parsing")
                        GrantedParseWebPermission(stateContext, inputUriString, input, uri, pointsFromUri, webUriString)
                    } else {
                        stateContext.log.e(null, "HTML Pattern: Input doesn't support web parsing")
                        DeniedParseHtmlPermission(stateContext, inputUriString, pointsFromUri)
                    }
                } else if (points.lastOrNull()?.hasName() == true) {
                    stateContext.log.i(null, "HTML Pattern: Parsed $htmlUrl to $points")
                    ConversionSucceeded(stateContext, inputUriString, points)
                } else {
                    stateContext.log.w(null, "HTML Pattern: Failed to parse $htmlUrl")
                    ConversionFailed(
                        stateContext.resources.getString(
                            R.string.conversion_failed_parse_html_error_with_reason,
                            stateContext.resources.getString(R.string.conversion_failed_reason_no_points),
                        ),
                        inputUriString,
                    )
                }
            }
        } catch (_: CancellationException) {
            ConversionFailed(stateContext.resources.getString(R.string.conversion_failed_cancelled), inputUriString)
        } catch (tr: RecoverableNetworkException) {
            GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                input,
                uri,
                pointsFromUri,
                htmlUriString,
                retry = NetworkTools.Retry((retry?.count ?: 0) + 1, tr),
            )
        } catch (tr: UnrecoverableNetworkException) {
            ConversionFailed(
                stateContext.resources.getString(
                    R.string.conversion_failed_parse_html_error_with_reason,
                    stateContext.resources.getString(tr.messageResId),
                ),
                inputUriString,
            )
        }
    }

    override fun getLargeLoadingIndicator() = LoadingIndicator.Large(
        title = stateContext.resources.getString(input.loadingIndicatorTitleResId),
        description = retry?.let { retry ->
            stateContext.resources.getString(
                R.string.conversion_loading_indicator_description,
                retry.count + 1,
                NetworkTools.MAX_RETRIES + 1,
                stateContext.resources.getString(retry.tr.messageResId),
            )
        },
    )
}

data class DeniedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val points: Points,
) : ConversionState {
    override suspend fun transition() = if (points.lastOrNull()?.let { it.hasCoordinates() || it.hasName() } == true) {
        ConversionSucceeded(stateContext, inputUriString, points)
    } else {
        ConversionFailed(
            stateContext.resources.getString(R.string.conversion_failed_connection_permission_denied),
            inputUriString,
        )
    }
}

data class RequestedParseWebPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: WebInput,
    val uri: Uri,
    val pointsFromUri: Points,
    val webUriString: String,
) : ConversionState, ConversionState.HasPermission {
    override val permissionTitleResId: Int = input.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.ALWAYS)
        }
        return GrantedParseWebPermission(stateContext, inputUriString, input, uri, pointsFromUri, webUriString)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermissionPreference, Permission.NEVER)
        }
        return DeniedParseHtmlPermission(stateContext, inputUriString, pointsFromUri)
    }
}

/**
 * When GrantedParseWebPermission is the current state, the UI should load [webUriString] in a WebView and call
 * [onUrlChange] once the page loaded in the WebView changes its URL. A page usually changes its URL by calling
 * JavaScript `history.pushState()`.
 *
 * Transitioning this waits for [onUrlChange] to be called within [timeout]. If it doesn't happen, it returns a failure.
 *
 * To allow communication between [transition] and [onUrlChange], this state contains a mutable private state
 * [currentUrlString]. This doesn't feel elegant and should be implemented differently, when we figure out how.
 */
data class GrantedParseWebPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: WebInput,
    val uri: Uri,
    val pointsFromUri: Points,
    val webUriString: String,
    val timeout: Duration = 60.seconds,
) : ConversionState, ConversionState.HasLargeLoadingIndicator {
    private val currentUrlString = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    override suspend fun transition(): State =
        try {
            val urlString = currentUrlString
                .filterNotNull()
                .timeout(timeout)
                .first()
            val matchingUriString = input.uriPattern.find(urlString)?.value
            matchingUriString?.let { uriString ->
                input.parseUri(Uri.parse(uriString, stateContext.uriQuote), stateContext.uriQuote)
            }.run {
                if (this?.points != null && this.points.isNotEmpty()) {
                    stateContext.log.i(ConversionState.TAG, "Parsed web URL $matchingUriString to $points")
                    ConversionSucceeded(stateContext, inputUriString, points)
                } else {
                    stateContext.log.w(ConversionState.TAG, "Failed to parse web URL $webUriString")
                    ConversionFailed(
                        stateContext.resources.getString(
                            R.string.conversion_failed_parse_html_error_with_reason,
                            stateContext.resources.getString(R.string.conversion_failed_reason_no_points),
                        ),
                        inputUriString,
                    )
                }
            }
        } catch (_: TimeoutCancellationException) {
            stateContext.log.e(ConversionState.TAG, "Parse web: Timed out")
            ConversionFailed(
                stateContext.resources.getString(
                    R.string.conversion_failed_parse_html_error_with_reason,
                    stateContext.resources.getString(R.string.conversion_failed_reason_timeout),
                ),
                inputUriString,
            )
        } catch (_: CancellationException) {
            ConversionFailed(stateContext.resources.getString(R.string.conversion_failed_cancelled), inputUriString)
        }

    fun onUrlChange(urlString: String) {
        currentUrlString.value = urlString
    }

    override fun getLargeLoadingIndicator() = LoadingIndicator.Large(
        title = stateContext.resources.getString(input.loadingIndicatorTitleResId),
    )
}

data class ConversionSucceeded(
    val stateContext: ConversionStateContext,
    override val inputUriString: String,
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
                                CachedPurchase(productId = it.product.id, token = it.token)
                            )
                            true
                        }
                    }
                }
                .timeout(billingStatusTimeout)
                .first()
        } catch (_: TimeoutCancellationException) {
            // If billing status didn't appear, try to read it from cache
            stateContext.log.w(null, "Automation: Billing status didn't appear within $billingStatusTimeout")
            stateContext.userPreferencesRepository.getValue(CachedPurchasePreference)
                ?.let { cachedPurchase ->
                    stateContext.billing.products.firstOrNull { product -> cachedPurchase.productId == product.id }
                        ?.let { product ->
                            stateContext.log.w(null, "Automation: Found cached billing status")
                            BillingStatus.Purchased(
                                product,
                                expired = false,
                                refundable = true,
                                token = cachedPurchase.token,
                            )
                        }
                }
                ?: run {
                    stateContext.log.w(null, "Automation: Didn't find cached billing status")
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
                return ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay)
            }
            return ActionReady(inputUriString, points, action, isAutomation = true)
        }
        return null
    }
}

data class ConversionFailed(
    override val message: String,
    override val inputUriString: String,
) : ConversionState, ConversionState.HasError

data class ActionWaiting(
    val stateContext: ConversionStateContext,
    override val inputUriString: String,
    override val points: Points,
    val action: Action<*>,
    @Suppress("SameParameterValue") val isAutomation: Boolean,
    val delay: Duration,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = try {
        if (delay.isPositive()) {
            delay(delay)
        }
        ActionReady(inputUriString, points, action, isAutomation)
    } catch (_: CancellationException) {
        ActionFinished(inputUriString, points, action, isAutomation)
    }
}

data class ActionReady(
    override val inputUriString: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = when (action) {
        is BasicAction -> BasicActionReady(inputUriString, points, action, isAutomation)
        is FileAction -> FileUriRequested(inputUriString, points, action, isAutomation)
        is LocationAction -> LocationRationaleRequested(inputUriString, points, action, isAutomation)
    }
}

data class BasicActionReady(
    override val inputUriString: String,
    override val points: Points,
    val action: BasicAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class FileActionReady(
    override val inputUriString: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
    val uri: android.net.Uri,
) : ConversionState, ConversionState.HasResult

data class LocationActionReady(
    override val inputUriString: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point,
) : ConversionState, ConversionState.HasResult

data class ActionRan(
    override val inputUriString: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
    val success: Boolean?,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = when (success) {
        true -> ActionSucceeded(inputUriString, points, action, isAutomation)
        false -> ActionFailed(inputUriString, points, action, isAutomation)
        else -> ActionFinished(inputUriString, points, action, isAutomation)
    }
}

data class ActionSucceeded(
    override val inputUriString: String,
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
        return ActionFinished(inputUriString, points, action, isAutomation)
    }
}

data class ActionFailed(
    override val inputUriString: String,
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
        return ActionFinished(inputUriString, points, action, isAutomation)
    }
}

data class ActionFinished(
    override val inputUriString: String,
    override val points: Points,
    val action: Action<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class FileUriRequested(
    override val inputUriString: String,
    override val points: Points,
    val action: FileAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationRationaleRequested(
    override val inputUriString: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationRationaleShown(
    override val inputUriString: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasPermission, ConversionState.HasResult {
    @StringRes
    override val permissionTitleResId = R.string.conversion_succeeded_location_rationale_dialog_title

    override suspend fun grant(doNotAsk: Boolean): State =
        LocationRationaleConfirmed(inputUriString, points, action, isAutomation)

    override suspend fun deny(doNotAsk: Boolean): State =
        ActionFinished(inputUriString, points, action, isAutomation)
}

data class LocationRationaleConfirmed(
    override val inputUriString: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasResult

data class LocationPermissionReceived(
    val stateContext: ConversionStateContext,
    override val inputUriString: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ConversionState, ConversionState.HasSmallLoadingIndicator, ConversionState.HasResult {
    override fun getSmallLoadingIndicator() = LoadingIndicator.Small(
        stateContext.resources.getString(R.string.conversion_succeeded_location_loading_indicator_title)
    )
}

data class LocationReceived(
    override val inputUriString: String,
    override val points: Points,
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point?,
) : ConversionState, ConversionState.HasResult {
    override suspend fun transition(): State = if (location == null) {
        LocationFindingFailed(inputUriString, points, action, isAutomation)
    } else {
        LocationActionReady(inputUriString, points, action, isAutomation = isAutomation, location = location)
    }
}

data class LocationFindingFailed(
    override val inputUriString: String,
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
        return ActionFinished(inputUriString, points, action, isAutomation)
    }
}

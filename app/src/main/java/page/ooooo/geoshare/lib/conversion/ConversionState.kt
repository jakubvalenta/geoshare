package page.ooooo.geoshare.lib.conversion

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.BillingCachedProductIdPreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.ParseHtmlResult
import page.ooooo.geoshare.lib.inputs.ParseUriResult
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.BasicAction
import page.ooooo.geoshare.lib.outputs.BasicAutomation
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.outputs.LocationAutomation
import page.ooooo.geoshare.lib.outputs.NoopAutomation
import page.ooooo.geoshare.lib.point.Point
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ConversionState"

interface ConversionState : State {
    override suspend fun transition(): State? = null

    interface HasPermission : ConversionState, State.PermissionState

    interface HasLoadingIndicator : ConversionState {
        val loadingIndicator: LoadingIndicator
    }

    interface HasResult : ConversionState {
        val inputUriString: String
        val points: ImmutableList<Point>
    }

    interface HasError : ConversionState {
        val errorMessageResId: Int
        val inputUriString: String
    }
}

class Initial : ConversionState

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
) : ConversionState {
    override suspend fun transition(): State {
        if (inputUriString.isEmpty()) {
            return ConversionFailed(R.string.conversion_failed_missing_url, "")
        }
        for (input in stateContext.inputs) {
            input.uriPattern.find(inputUriString)?.value?.let { uriString ->
                val uri = Uri.parse(uriString, stateContext.uriQuote)
                return ReceivedUri(stateContext, inputUriString, input, uri, null)
            }
        }
        return ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString)
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
        if (input is Input.HasShortUri) {
            input.shortUriPattern.find(uri.toString())?.value?.let { uriString ->
                val uri = Uri.parse(uriString, stateContext.uriQuote)
                return when (permission ?: stateContext.userPreferencesRepository.getValue(
                    ConnectionPermissionPreference
                )) {
                    Permission.ALWAYS -> GrantedUnshortenPermission(
                        stateContext, inputUriString, input, uri
                    )

                    Permission.ASK -> RequestedUnshortenPermission(
                        stateContext, inputUriString, input, uri
                    )

                    Permission.NEVER -> DeniedConnectionPermission(
                        stateContext, inputUriString, input
                    )
                }
            }
        }
        return UnshortenedUrl(stateContext, inputUriString, input, uri, permission)
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input.HasShortUri,
    val uri: Uri,
) : ConversionState.HasPermission {
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
    val input: Input.HasShortUri,
    val uri: Uri,
    val retry: NetworkTools.Retry? = null,
) : ConversionState.HasLoadingIndicator {
    override suspend fun transition(): State {
        val url = uri.toUrl()
        if (url == null) {
            stateContext.log.e(null, "Unshorten: Failed to get URL for $uri")
            return ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
        }
        return try {
            val locationHeader = when (input.shortUriMethod) {
                Input.ShortUriMethod.GET -> stateContext.networkTools.getRedirectUrlString(url, retry)
                Input.ShortUriMethod.HEAD -> stateContext.networkTools.requestLocationHeader(url, retry)
            }
            if (locationHeader != null) {
                val unshortenedUri = Uri.parse(locationHeader, stateContext.uriQuote).toAbsoluteUri(uri)
                stateContext.log.i(null, "Unshorten: Resolved short URI $uri to $unshortenedUri")
                UnshortenedUrl(stateContext, inputUriString, input, unshortenedUri, Permission.ALWAYS)
            } else {
                stateContext.log.w(null, "Unshorten: Missing location header for $url")
                ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
            }
        } catch (_: CancellationException) {
            ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (tr: NetworkTools.RecoverableException) {
            GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                input,
                uri,
                retry = NetworkTools.Retry((retry?.count ?: 0) + 1, tr),
            )
        } catch (tr: NetworkTools.UnrecoverableException) {
            if (tr.cause is IOException) {
                ConversionFailed(R.string.conversion_failed_unshorten_connection_error, inputUriString)
            } else {
                ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
            }
        }
    }

    override val loadingIndicator = LoadingIndicator.Large(
        titleResId = input.loadingIndicatorTitleResId,
        description = {
            retry?.let { retry ->
                stringResource(
                    R.string.conversion_loading_indicator_description,
                    retry.count + 1,
                    NetworkTools.MAX_RETRIES + 1,
                    stringResource(retry.tr.messageResId),
                )
            }
        },
    )
}

data class DeniedConnectionPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input,
) : ConversionState {
    override suspend fun transition(): State =
        ConversionFailed(R.string.conversion_failed_connection_permission_denied, inputUriString)
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input,
    val uri: Uri,
    val permission: Permission?,
) : ConversionState {
    override suspend fun transition(): State =
        when (val res = input.parseUri(uri)) {
            is ParseUriResult.Succeeded -> {
                stateContext.log.i(null, "URI Pattern: Converted $uri to ${res.points}")
                ConversionSucceeded(stateContext, inputUriString, res.points)
            }

            is ParseUriResult.SucceededAndSupportsHtmlParsing -> {
                if (input is Input.HasHtml) {
                    when (permission
                        ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                        Permission.ALWAYS -> GrantedParseHtmlPermission(
                            stateContext, inputUriString, input, uri, res.points, res.htmlUriString
                        )

                        Permission.ASK -> RequestedParseHtmlPermission(
                            stateContext, inputUriString, input, uri, res.points, res.htmlUriString
                        )

                        Permission.NEVER -> DeniedParseHtmlPermission(stateContext, inputUriString, res.points)
                    }
                } else {
                    stateContext.log.e(null, "URI Pattern: Input doesn't support HTML parsing")
                    DeniedParseHtmlPermission(stateContext, inputUriString, res.points)
                }
            }

            is ParseUriResult.SucceededAndSupportsWebParsing -> {
                if (input is Input.HasWeb) {
                    when (permission
                        ?: stateContext.userPreferencesRepository.getValue(ConnectionPermissionPreference)) {
                        Permission.ALWAYS -> GrantedParseWebPermission(
                            stateContext, inputUriString, input, uri, res.points, res.webUriString
                        )

                        Permission.ASK -> RequestedParseWebPermission(
                            stateContext, inputUriString, input, uri, res.points, res.webUriString
                        )

                        Permission.NEVER -> DeniedParseHtmlPermission(stateContext, inputUriString, res.points)
                    }
                } else {
                    stateContext.log.e(null, "URI Pattern: Input doesn't support web parsing")
                    DeniedParseHtmlPermission(stateContext, inputUriString, res.points)
                }
            }

            is ParseUriResult.Failed -> {
                stateContext.log.i(null, "URI Pattern: Failed to parse $uri")
                ConversionFailed(R.string.conversion_failed_parse_url_error, inputUriString)
            }
        }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input.HasHtml,
    val uri: Uri,
    val pointsFromUri: ImmutableList<Point>,
    val htmlUriString: String,
) : ConversionState.HasPermission {
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
    val input: Input.HasHtml,
    val uri: Uri,
    val pointsFromUri: ImmutableList<Point>,
    val htmlUriString: String,
    val retry: NetworkTools.Retry? = null,
) : ConversionState.HasLoadingIndicator {
    override suspend fun transition(): State {
        val htmlUrl = Uri.parse(htmlUriString, stateContext.uriQuote).toUrl()
        if (htmlUrl == null) {
            stateContext.log.e(null, "HTML Pattern: Failed to get HTML URL for $uri")
            return ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        }
        stateContext.log.i(null, "HTML Pattern: Downloading $htmlUrl")
        return try {
            val res = stateContext.networkTools.getSource(htmlUrl, retry) { channel ->
                input.parseHtml(htmlUrl.toString(), channel, pointsFromUri, stateContext.log)
            }
            when (res) {
                is ParseHtmlResult.Succeeded -> {
                    stateContext.log.i(null, "HTML Pattern: Parsed $htmlUrl to ${res.points}")
                    ConversionSucceeded(stateContext, inputUriString, res.points)
                }

                is ParseHtmlResult.RequiresRedirect -> {
                    stateContext.log.i(
                        null, "HTML Pattern: Parsed $htmlUrl to redirect URI ${res.redirectUriString}"
                    )
                    val redirectUri = Uri.parse(res.redirectUriString, stateContext.uriQuote).toAbsoluteUri(uri)
                    ReceivedUri(stateContext, inputUriString, input, redirectUri, Permission.ALWAYS)
                }

                is ParseHtmlResult.RequiresWebParsing -> {
                    if (input is Input.HasWeb) {
                        stateContext.log.i(null, "HTML Pattern: URI $htmlUrl requires web parsing")
                        GrantedParseWebPermission(
                            stateContext, inputUriString, input, uri, pointsFromUri, res.webUriString
                        )
                    } else {
                        stateContext.log.e(null, "HTML Pattern: Input doesn't support web parsing")
                        ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
                    }
                }

                is ParseHtmlResult.Failed -> {
                    stateContext.log.w(null, "HTML Pattern: Failed to parse $htmlUrl")
                    ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
                }
            }
        } catch (_: CancellationException) {
            ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (tr: NetworkTools.RecoverableException) {
            GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                input,
                uri,
                pointsFromUri,
                htmlUriString,
                retry = NetworkTools.Retry((retry?.count ?: 0) + 1, tr),
            )
        } catch (tr: NetworkTools.UnrecoverableException) {
            if (tr.cause is IOException) {
                ConversionFailed(R.string.conversion_failed_parse_html_connection_error, inputUriString)
            } else {
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
            }
        }
    }

    override val loadingIndicator = LoadingIndicator.Large(
        titleResId = input.loadingIndicatorTitleResId,
        description = {
            retry?.let { retry ->
                stringResource(
                    R.string.conversion_loading_indicator_description,
                    retry.count + 1,
                    NetworkTools.MAX_RETRIES + 1,
                    stringResource(retry.tr.messageResId),
                )
            }
        },
    )
}

data class DeniedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val points: ImmutableList<Point>,
) : ConversionState {
    override suspend fun transition() = if (points.lastOrNull()?.let { it.hasCoordinates() || it.hasName() } == true) {
        ConversionSucceeded(stateContext, inputUriString, points)
    } else {
        ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
    }
}

data class RequestedParseWebPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input.HasWeb,
    val uri: Uri,
    val pointsFromUri: ImmutableList<Point>,
    val webUriString: String,
) : ConversionState.HasPermission {
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
    val input: Input.HasWeb,
    val uri: Uri,
    val pointsFromUri: ImmutableList<Point>,
    val webUriString: String,
    val timeout: Duration = 30.seconds,
) : ConversionState.HasLoadingIndicator {
    private val currentUrlString = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    override suspend fun transition(): State =
        try {
            val urlString = currentUrlString
                .filterNotNull()
                .timeout(timeout)
                .first()
            val matchingUriString = input.uriPattern.find(urlString)?.value
            when (
                val res = matchingUriString?.let { uriString ->
                    input.parseUri(Uri.parse(uriString, stateContext.uriQuote))
                }
            ) {
                is ParseUriResult.Succeeded -> {
                    stateContext.log.i(TAG, "Parsed web URL $matchingUriString to ${res.points}")
                    ConversionSucceeded(stateContext, inputUriString, res.points)
                }

                is ParseUriResult.SucceededAndSupportsHtmlParsing -> {
                    stateContext.log.i(TAG, "Parsed web URL $matchingUriString to ${res.points}")
                    ConversionSucceeded(stateContext, inputUriString, res.points)
                }

                is ParseUriResult.SucceededAndSupportsWebParsing,
                    -> {
                    stateContext.log.i(TAG, "Parsed web URL $matchingUriString to ${res.points}")
                    ConversionSucceeded(stateContext, inputUriString, res.points)
                }

                is ParseUriResult.Failed, null -> {
                    stateContext.log.w(TAG, "Failed to parse web URL $webUriString")
                    ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
                }
            }
        } catch (_: TimeoutCancellationException) {
            stateContext.log.e(TAG, "Parse web: Timed out")
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        } catch (_: CancellationException) {
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        }

    fun onUrlChange(urlString: String) {
        currentUrlString.value = urlString
    }

    override val loadingIndicator = LoadingIndicator.Large(
        titleResId = input.loadingIndicatorTitleResId,
        description = { null },
    )
}

data class ConversionSucceeded(
    val stateContext: ConversionStateContext,
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val billingStatusTimeout: Duration = 3.seconds,
) : ConversionState.HasResult {
    @OptIn(FlowPreview::class)
    override suspend fun transition(): State? {
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

                        is BillingStatus.NotPurchased -> true

                        is BillingStatus.Purchased -> {
                            // If billing status appeared within timeout, cache it
                            stateContext.userPreferencesRepository.setValue(
                                BillingCachedProductIdPreference,
                                it.product.id,
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
            stateContext.userPreferencesRepository.getValue(BillingCachedProductIdPreference)
                ?.let { productId -> stateContext.billing.products.firstOrNull { product -> productId == product.id } }
                .let { product ->
                    if (product != null) {
                        stateContext.log.w(null, "Automation: Found cached billing status")
                        BillingStatus.Purchased(product, refundable = true)
                    } else {
                        stateContext.log.w(null, "Automation: Didn't find cached billing status")
                        BillingStatus.Loading()
                    }
                }
        }

        if (billingStatus is BillingStatus.Purchased && stateContext.billing.features.contains(AutomationFeature)) {
            if (automation is Automation.HasDelay) {
                val delay = stateContext.userPreferencesRepository.getValue(AutomationDelayPreference)
                return ActionWaiting(stateContext, inputUriString, points, null, automation, delay)
            }
            return ActionReady(inputUriString, points, null, automation)
        }
        return null
    }
}

data class ConversionFailed(
    @param:StringRes override val errorMessageResId: Int,
    override val inputUriString: String,
) : ConversionState.HasError

data class ActionWaiting(
    val stateContext: ConversionStateContext,
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: Action,
    val delay: Duration,
) : ConversionState.HasResult {
    override suspend fun transition(): State = try {
        if (delay.isPositive()) {
            delay(delay)
        }
        ActionReady(inputUriString, points, i, action)
    } catch (_: CancellationException) {
        ActionFinished(inputUriString, points, action)
    }
}

data class ActionReady(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: Action,
) : ConversionState.HasResult {
    override suspend fun transition(): State = when (action) {
        is BasicAutomation -> BasicActionReady(inputUriString, points, i, action)
        is BasicAction -> BasicActionReady(inputUriString, points, i, action)
        is LocationAutomation -> LocationRationaleRequested(inputUriString, points, i, action)
        is LocationAction -> LocationRationaleRequested(inputUriString, points, i, action)
    }
}

data class BasicActionReady(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: BasicAction,
) : ConversionState.HasResult

data class LocationActionReady(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: LocationAction,
    val location: Point,
) : ConversionState.HasResult

data class ActionRan(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val action: Action,
    val success: Boolean?,
) : ConversionState.HasResult {
    override suspend fun transition(): State = when (success) {
        true -> ActionSucceeded(inputUriString, points, action)
        false -> ActionFailed(inputUriString, points, action)
        else -> ActionFinished(inputUriString, points, action)
    }
}

data class ActionSucceeded(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val action: Action,
) : ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(inputUriString, points, action)
    }
}

data class ActionFailed(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val action: Action,
) : ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(inputUriString, points, action)
    }
}

data class ActionFinished(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val action: Action,
) : ConversionState.HasResult

data class LocationRationaleRequested(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: LocationAction,
) : ConversionState.HasResult

data class LocationRationaleShown(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: LocationAction,
) : ConversionState.HasPermission, ConversionState.HasResult {
    @StringRes
    override val permissionTitleResId = R.string.conversion_succeeded_location_rationale_dialog_title

    override suspend fun grant(doNotAsk: Boolean): State =
        LocationRationaleConfirmed(inputUriString, points, i, action)

    override suspend fun deny(doNotAsk: Boolean): State = ActionFinished(inputUriString, points, action)
}

data class LocationRationaleConfirmed(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: LocationAction,
) : ConversionState.HasResult

data class LocationPermissionReceived(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: LocationAction,
) : ConversionState.HasResult, ConversionState.HasLoadingIndicator {
    override val loadingIndicator = LoadingIndicator.Small(
        messageResId = R.string.conversion_succeeded_location_loading_indicator_title,
    )
}

data class LocationReceived(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val i: Int?,
    val action: LocationAction,
    val location: Point?,
) : ConversionState.HasResult {
    override suspend fun transition(): State = if (location == null) {
        LocationFindingFailed(inputUriString, points, action)
    } else {
        LocationActionReady(inputUriString, points, i, action, location)
    }
}

data class LocationFindingFailed(
    override val inputUriString: String,
    override val points: ImmutableList<Point>,
    val action: LocationAction,
) : ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(inputUriString, points, action)
    }
}

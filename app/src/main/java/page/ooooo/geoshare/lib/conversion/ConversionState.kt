package page.ooooo.geoshare.lib.conversion

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.AutomationUserPreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermission
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ConversionState : State {
    override suspend fun transition(): State? = null

    interface HasPermission : ConversionState, State.PermissionState

    interface HasLoadingIndicator : ConversionState {
        val loadingIndicatorTitleResId: Int

        @Composable
        fun loadingIndicatorDescription(): String?
    }

    interface HasResult : ConversionState {
        val inputUriString: String
        val position: Position
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
            val m = input.uriPattern.matcher(inputUriString)
            if (m.find()) {
                val uri = Uri.parse(m.group(), stateContext.uriQuote)
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
            val m = input.shortUriPattern.matcher(uri.toString())
            if (m.matches()) {
                val uri = Uri.parse(m.group(), stateContext.uriQuote)
                return when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermission)) {
                    Permission.ALWAYS -> GrantedUnshortenPermission(
                        stateContext,
                        inputUriString,
                        input,
                        uri
                    )

                    Permission.ASK -> RequestedUnshortenPermission(
                        stateContext,
                        inputUriString,
                        input,
                        uri
                    )

                    Permission.NEVER -> DeniedConnectionPermission(
                        stateContext,
                        inputUriString,
                        input
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
            stateContext.userPreferencesRepository.setValue(ConnectionPermission, Permission.ALWAYS)
        }
        return GrantedUnshortenPermission(stateContext, inputUriString, input, uri)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermission, Permission.NEVER)
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
    override val loadingIndicatorTitleResId: Int = input.loadingIndicatorTitleResId

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

    @Composable
    override fun loadingIndicatorDescription(): String? = retry?.let { retry ->
        stringResource(
            R.string.conversion_loading_indicator_description,
            retry.count + 1,
            NetworkTools.MAX_RETRIES + 1,
            stringResource(retry.tr.messageResId),
        )
    }
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
    override suspend fun transition(): State {
        val (positionFromUri, htmlUriString) = input.parseUri(uri)
        stateContext.log.i(null, "URI Pattern: Converted $uri to $positionFromUri")
        if (!positionFromUri.points.isNullOrEmpty()) {
            return ConversionSucceeded(stateContext, inputUriString, positionFromUri)
        }
        if (positionFromUri.q.isNullOrEmpty() && htmlUriString == null) {
            stateContext.log.i(null, "URI Pattern: Failed to parse $uri")
            return ConversionFailed(R.string.conversion_failed_parse_url_error, inputUriString)
        }
        if (input is Input.HasHtml && htmlUriString != null) {
            return when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(
                    stateContext, inputUriString, input, uri, positionFromUri, htmlUriString,
                )

                Permission.ASK -> RequestedParseHtmlPermission(
                    stateContext, inputUriString, input, uri, positionFromUri, htmlUriString,
                )

                Permission.NEVER -> ParseHtmlFailed(stateContext, inputUriString, positionFromUri)
            }
        }
        return ParseHtmlFailed(stateContext, inputUriString, positionFromUri)
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input.HasHtml,
    val uri: Uri,
    val positionFromUri: Position,
    val htmlUriString: String,
) : ConversionState.HasPermission {
    override val permissionTitleResId: Int = input.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlPermission(
            stateContext,
            inputUriString,
            input,
            uri,
            positionFromUri,
            htmlUriString,
        )
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermission, Permission.NEVER)
        }
        return ParseHtmlFailed(stateContext, inputUriString, positionFromUri)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val input: Input.HasHtml,
    val uri: Uri,
    val positionFromUri: Position,
    val htmlUriString: String,
    val retry: NetworkTools.Retry? = null,
) : ConversionState.HasLoadingIndicator {
    override val loadingIndicatorTitleResId: Int = input.loadingIndicatorTitleResId

    override suspend fun transition(): State {
        val htmlUrl = Uri.parse(htmlUriString, stateContext.uriQuote).toUrl()
        if (htmlUrl == null) {
            stateContext.log.e(null, "HTML Pattern: Failed to get HTML URL for $uri")
            return ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        }
        stateContext.log.i(null, "HTML Pattern: Downloading $htmlUrl")
        return try {
            val (positionFromHtml, redirectUriString) = stateContext.networkTools.getSource(htmlUrl, retry) { channel ->
                input.parseHtml(channel)
            }
            if (!positionFromHtml.points.isNullOrEmpty()) {
                stateContext.log.i(null, "HTML Pattern: Parsed $htmlUrl to $positionFromHtml")
                // TODO Copy main point name from `positionFromUri` to `positionFromHtml`
                ConversionSucceeded(stateContext, inputUriString, positionFromHtml)
            } else if (redirectUriString != null) {
                stateContext.log.i(
                    null, "HTML Redirect Pattern: Parsed $htmlUrl to redirect URI $redirectUriString"
                )
                val redirectUri = Uri.parse(redirectUriString, stateContext.uriQuote).toAbsoluteUri(uri)
                ReceivedUri(
                    stateContext,
                    inputUriString,
                    input,
                    redirectUri,
                    Permission.ALWAYS,
                )
            } else {
                stateContext.log.w(null, "HTML Pattern: Failed to parse $htmlUrl")
                ParseHtmlFailed(stateContext, inputUriString, positionFromUri)
            }
        } catch (_: CancellationException) {
            ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (tr: NetworkTools.RecoverableException) {
            GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                input,
                uri,
                positionFromUri,
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

    @Composable
    override fun loadingIndicatorDescription(): String? = retry?.let { retry ->
        stringResource(
            R.string.conversion_loading_indicator_description,
            retry.count + 1,
            NetworkTools.MAX_RETRIES + 1,
            stringResource(retry.tr.messageResId),
        )
    }
}

data class ParseHtmlFailed(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val position: Position,
) : ConversionState {
    override suspend fun transition() =
        if (!position.points.isNullOrEmpty() || !position.q.isNullOrEmpty()) {
            ConversionSucceeded(stateContext, inputUriString, position)
        } else {
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        }
}

data class ConversionSucceeded(
    val stateContext: ConversionStateContext,
    override val inputUriString: String,
    override val position: Position,
) : ConversionState.HasResult {
    override suspend fun transition(): State? =
        stateContext.userPreferencesRepository.getValue(AutomationUserPreference).let { automation ->
            when (automation) {
                is NoopAutomation ->
                    null

                is Automation.HasDelay ->
                    ActionWaiting(stateContext, inputUriString, position, null, automation, automation.delay)

                else ->
                    ActionReady(inputUriString, position, null, automation)
            }
        }
}

data class ConversionFailed(
    @param:StringRes override val errorMessageResId: Int,
    override val inputUriString: String,
) : ConversionState.HasError

data class ActionWaiting(
    val stateContext: ConversionStateContext,
    override val inputUriString: String,
    override val position: Position,
    val i: Int?,
    val action: Action,
    val delay: Duration,
) : ConversionState.HasResult {
    override suspend fun transition(): State =
        try {
            if (delay.isPositive()) {
                delay(delay)
            }
            ActionReady(inputUriString, position, i, action)
        } catch (_: CancellationException) {
            ActionFinished(inputUriString, position, action)
        }
}

data class ActionReady(
    override val inputUriString: String,
    override val position: Position,
    val i: Int?,
    val action: Action,
) : ConversionState.HasResult {
    override suspend fun transition(): State =
        when (action) {
            is BasicAutomation -> BasicActionReady(inputUriString, position, i, action)
            is BasicAction -> BasicActionReady(inputUriString, position, i, action)
            is LocationAutomation -> LocationRationaleRequested(inputUriString, position, i, action)
            is LocationAction -> LocationRationaleRequested(inputUriString, position, i, action)
        }
}

data class BasicActionReady(
    override val inputUriString: String,
    override val position: Position,
    val i: Int?,
    val action: BasicAction,
) : ConversionState.HasResult

data class LocationActionReady(
    override val inputUriString: String,
    override val position: Position,
    val i: Int?,
    val action: LocationAction,
    val location: Point?,
) : ConversionState.HasResult

data class ActionRan(
    override val inputUriString: String,
    override val position: Position,
    val action: Action,
    val success: Boolean?,
) : ConversionState.HasResult {
    override suspend fun transition(): State =
        when (success) {
            true -> ActionSucceeded(inputUriString, position, action)

            false -> ActionFailed(inputUriString, position, action)

            else -> ActionFinished(inputUriString, position, action)
        }
}

data class ActionSucceeded(
    override val inputUriString: String,
    override val position: Position,
    val action: Action,
) : ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(inputUriString, position, action)
    }
}

data class ActionFailed(
    override val inputUriString: String,
    override val position: Position,
    val action: Action,
) : ConversionState.HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionFinished(inputUriString, position, action)
    }
}

data class ActionFinished(
    override val inputUriString: String,
    override val position: Position,
    val action: Action,
) : ConversionState.HasResult

data class LocationRationaleRequested(
    override val inputUriString: String,
    override val position: Position,
    val i: Int?,
    val action: LocationAction,
) : ConversionState.HasResult

data class LocationRationaleShown(
    override val inputUriString: String,
    override val position: Position,
    val i: Int?,
    val action: LocationAction,
) : ConversionState.HasPermission, ConversionState.HasResult {
    @StringRes
    override val permissionTitleResId = R.string.conversion_location_permission_dialog_title

    override suspend fun grant(doNotAsk: Boolean): State =
        LocationRationaleConfirmed(inputUriString, position, i, action)

    override suspend fun deny(doNotAsk: Boolean): State =
        ActionFailed(inputUriString, position, action)
}

data class LocationRationaleConfirmed(
    override val inputUriString: String,
    override val position: Position,
    val i: Int?,
    val action: LocationAction,
) : ConversionState.HasResult

data class LocationPermissionReceived(
    override val inputUriString: String,
    override val position: Position,
    val i: Int?,
    val action: LocationAction,
) : ConversionState.HasResult

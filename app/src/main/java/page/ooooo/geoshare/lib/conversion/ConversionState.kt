package page.ooooo.geoshare.lib.conversion

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.io.buffered
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.AutomationUserPreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermission
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.position.Position
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

open class ConversionState : State {
    override suspend fun transition(): State? = null
}

interface HasLoadingIndicator {
    val loadingIndicatorTitleResId: Int

    @Composable
    fun loadingIndicatorDescription(): String?
}

interface HasResult {
    val inputUriString: String
    val position: Position
}

interface HasError {
    val errorMessageResId: Int
    val inputUriString: String
}

data class ConversionRunContext(
    val context: Context,
    val clipboard: Clipboard,
    val saveGpxLauncher: ActivityResultLauncher<Intent>,
)

class Initial : ConversionState()

data class ReceivedIntent(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val intent: Intent,
) : ConversionState() {
    override suspend fun transition(): State {
        val inputUriString = stateContext.intentTools.getIntentUriString(intent)
            ?: return ConversionFailed(R.string.conversion_failed_missing_url, "")
        return ReceivedUriString(stateContext, runContext, inputUriString)
    }
}

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
) : ConversionState() {
    override suspend fun transition(): State {
        for (input in stateContext.inputs) {
            val m = input.uriPattern.matcher(inputUriString)
            if (m.find()) {
                val uri = Uri.parse(m.group(), stateContext.uriQuote)
                return ReceivedUri(stateContext, runContext, inputUriString, input, uri, null)
            }
        }
        return ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString)
    }
}

data class ReceivedUri(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val input: Input,
    val uri: Uri,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        if (input is Input.HasShortUri) {
            val m = input.shortUriPattern.matcher(uri.toString())
            if (m.matches()) {
                val uri = Uri.parse(m.group(), stateContext.uriQuote)
                return when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermission)) {
                    Permission.ALWAYS -> GrantedUnshortenPermission(
                        stateContext,
                        runContext,
                        inputUriString,
                        input,
                        uri
                    )

                    Permission.ASK -> RequestedUnshortenPermission(
                        stateContext,
                        runContext,
                        inputUriString,
                        input,
                        uri
                    )

                    Permission.NEVER -> DeniedConnectionPermission(
                        stateContext,
                        runContext,
                        inputUriString,
                        input
                    )
                }
            }
        }
        return UnshortenedUrl(stateContext, runContext, inputUriString, input, uri, permission)
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val input: Input.HasShortUri,
    val uri: Uri,
) : ConversionState(), PermissionState {
    override val permissionTitleResId: Int = input.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermission, Permission.ALWAYS)
        }
        return GrantedUnshortenPermission(stateContext, runContext, inputUriString, input, uri)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermission, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, runContext, inputUriString, input)
    }
}

data class GrantedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val input: Input.HasShortUri,
    val uri: Uri,
    val retry: NetworkTools.Retry? = null,
) : ConversionState(), HasLoadingIndicator {
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
                UnshortenedUrl(stateContext, runContext, inputUriString, input, unshortenedUri, Permission.ALWAYS)
            } else {
                stateContext.log.w(null, "Unshorten: Missing location header for $url")
                ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
            }
        } catch (_: CancellationException) {
            ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (tr: NetworkTools.RecoverableException) {
            GrantedUnshortenPermission(
                stateContext,
                runContext,
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
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val input: Input,
) : ConversionState() {
    override suspend fun transition(): State =
        ConversionFailed(R.string.conversion_failed_connection_permission_denied, inputUriString)
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val input: Input,
    val uri: Uri,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val conversionUriResult = input.conversionUriPattern.match(uri)
        // TODO Simplify result checking
        stateContext.log.i(null, "URI Pattern: Converted $uri to ${conversionUriResult.position}")
        if (!conversionUriResult.position.points.isNullOrEmpty()) {
            return ConversionSucceeded(stateContext, runContext, inputUriString, conversionUriResult.position)
        }
        if (conversionUriResult.position.q.isNullOrEmpty() && conversionUriResult.url == null) {
            stateContext.log.i(null, "URI Pattern: Failed to parse $uri")
            return ConversionFailed(R.string.conversion_failed_parse_url_error, inputUriString)
        }
        if (input is Input.HasHtml) {
            return when (permission ?: stateContext.userPreferencesRepository.getValue(ConnectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(
                    stateContext, runContext, inputUriString, input, uri, conversionUriResult
                )

                Permission.ASK -> RequestedParseHtmlPermission(
                    stateContext, runContext, inputUriString, input, uri, conversionUriResult
                )

                Permission.NEVER -> ParseHtmlFailed(stateContext, runContext, inputUriString, conversionUriResult)
            }
        }
        return ParseHtmlFailed(stateContext, runContext, inputUriString, conversionUriResult)
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val input: Input.HasHtml,
    val uri: Uri,
    val conversionUriResult: ConversionPattern.Result,
) : ConversionState(), PermissionState {
    override val permissionTitleResId: Int = input.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlPermission(
            stateContext,
            runContext,
            inputUriString,
            input,
            uri,
            conversionUriResult,
        )
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(ConnectionPermission, Permission.NEVER)
        }
        return ParseHtmlFailed(stateContext, runContext, inputUriString, conversionUriResult)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val input: Input.HasHtml,
    val uri: Uri,
    val conversionUriResult: ConversionPattern.Result,
    val retry: NetworkTools.Retry? = null,
) : ConversionState(), HasLoadingIndicator {
    override val loadingIndicatorTitleResId: Int = input.loadingIndicatorTitleResId

    override suspend fun transition(): State {
        val htmlUrl = conversionUriResult.url
        if (htmlUrl == null) {
            stateContext.log.e(null, "HTML Pattern: Failed to get HTML URL for $uri")
            return ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        }
        stateContext.log.i(null, "HTML Pattern: Downloading $htmlUrl")
        return try {
            stateContext.networkTools.getSource(htmlUrl, retry) { source ->
                // TODO Delete SourceCache
                val conversionHtmlResult = input.conversionHtmlPattern.match(source.buffered())
                val positionFromHtml = conversionHtmlResult?.position
                // TODO Simplify result checking
                if (!positionFromHtml?.points.isNullOrEmpty()) {
                    stateContext.log.i(null, "HTML Pattern: Parsed $htmlUrl to $positionFromHtml")
                    ConversionSucceeded(stateContext, runContext, inputUriString, positionFromHtml)
                } else {
                    val redirectUriString = conversionHtmlResult?.url?.toString()
                    if (redirectUriString != null) {
                        stateContext.log.i(
                            null, "HTML Redirect Pattern: Parsed $htmlUrl to redirect URI $redirectUriString"
                        )
                        val redirectUri = Uri.parse(redirectUriString, stateContext.uriQuote).toAbsoluteUri(uri)
                        ReceivedUri(
                            stateContext,
                            runContext,
                            inputUriString,
                            input,
                            redirectUri,
                            Permission.ALWAYS,
                        )
                    } else {
                        stateContext.log.w(null, "HTML Pattern: Failed to parse $htmlUrl")
                        ParseHtmlFailed(stateContext, runContext, inputUriString, conversionUriResult)
                    }
                }
            }
        } catch (_: CancellationException) {
            ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (tr: NetworkTools.RecoverableException) {
            GrantedParseHtmlPermission(
                stateContext,
                runContext,
                inputUriString,
                input,
                uri,
                conversionUriResult,
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
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val conversionUriResult: ConversionPattern.Result,
) : ConversionState() {
    override suspend fun transition() =
        if (!conversionUriResult.position.points.isNullOrEmpty() || !conversionUriResult.position.q.isNullOrEmpty()) {
            ConversionSucceeded(stateContext, runContext, inputUriString, conversionUriResult.position)
        } else {
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        }
}

data class ConversionSucceeded(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    override val inputUriString: String,
    override val position: Position,
) : ConversionState(), HasResult {
    override suspend fun transition(): State =
        stateContext.userPreferencesRepository.getValue(AutomationUserPreference).let { automation ->
            when (automation) {
                is Automation.HasDelay -> AutomationWaiting(
                    stateContext,
                    runContext,
                    inputUriString,
                    position,
                    automation,
                )

                else -> AutomationReady(stateContext, runContext, inputUriString, position, automation)
            }
        }
}

data class ConversionFailed(
    @param:StringRes override val errorMessageResId: Int,
    override val inputUriString: String,
) : ConversionState(), HasError

data class AutomationWaiting(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    override val inputUriString: String,
    override val position: Position,
    val automation: Automation.HasDelay,
) : ConversionState(), HasResult {
    override suspend fun transition(): State =
        try {
            if (automation.delay.isPositive()) {
                delay(automation.delay)
            }
            AutomationReady(stateContext, runContext, inputUriString, position, automation)
        } catch (_: CancellationException) {
            AutomationFinished(inputUriString, position, automation)
        }
}

data class AutomationReady(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    override val inputUriString: String,
    override val position: Position,
    val automation: Automation,
) : ConversionState(), HasResult {
    override suspend fun transition(): State =
        automation.getAction(position, stateContext.uriQuote).let { outputAction ->
            when (outputAction?.run(stateContext.intentTools, runContext)) {
                true if automation is Automation.HasSuccessMessage ->
                    AutomationSucceeded(inputUriString, position, automation)

                false if automation is Automation.HasErrorMessage ->
                    AutomationFailed(inputUriString, position, automation)

                else -> AutomationFinished(inputUriString, position, automation)
            }
        }
}

data class AutomationSucceeded(
    override val inputUriString: String,
    override val position: Position,
    val automation: Automation.HasSuccessMessage,
) : ConversionState(), HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return AutomationFinished(inputUriString, position, automation)
    }
}

data class AutomationFailed(
    override val inputUriString: String,
    override val position: Position,
    val automation: Automation.HasErrorMessage,
) : ConversionState(), HasResult {
    override suspend fun transition(): State {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return AutomationFinished(inputUriString, position, automation)
    }
}

data class AutomationFinished(
    override val inputUriString: String,
    override val position: Position,
    val automation: Automation,
) : ConversionState(), HasResult

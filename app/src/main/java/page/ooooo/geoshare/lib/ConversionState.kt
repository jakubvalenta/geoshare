package page.ooooo.geoshare.lib

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.automation
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.lib.converters.ShortUriMethod
import page.ooooo.geoshare.lib.converters.UrlConverter
import page.ooooo.geoshare.lib.outputs.Automation
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
        for (urlConverter in stateContext.urlConverters) {
            val m = urlConverter.uriPattern.matcher(inputUriString)
            if (m.find()) {
                val uri = Uri.parse(m.group(), stateContext.uriQuote)
                return ReceivedUri(stateContext, runContext, inputUriString, urlConverter, uri, null)
            }
        }
        return ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString)
    }
}

data class ReceivedUri(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val urlConverter: UrlConverter,
    val uri: Uri,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        if (urlConverter is UrlConverter.WithShortUriPattern) {
            val m = urlConverter.shortUriPattern.matcher(uri.toString())
            if (m.matches()) {
                val uri = Uri.parse(m.group(), stateContext.uriQuote)
                return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                    Permission.ALWAYS -> GrantedUnshortenPermission(
                        stateContext,
                        runContext,
                        inputUriString,
                        urlConverter,
                        uri
                    )

                    Permission.ASK -> RequestedUnshortenPermission(
                        stateContext,
                        runContext,
                        inputUriString,
                        urlConverter,
                        uri
                    )

                    Permission.NEVER -> DeniedConnectionPermission(
                        stateContext,
                        runContext,
                        inputUriString,
                        urlConverter
                    )
                }
            }
        }
        return UnshortenedUrl(stateContext, runContext, inputUriString, urlConverter, uri, permission)
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithShortUriPattern,
    val uri: Uri,
) : ConversionState(), PermissionState {
    override val permissionTitleResId: Int = urlConverter.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedUnshortenPermission(stateContext, runContext, inputUriString, urlConverter, uri)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, runContext, inputUriString, urlConverter)
    }
}

data class GrantedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithShortUriPattern,
    val uri: Uri,
    val retry: NetworkTools.Retry? = null,
) : ConversionState(), HasLoadingIndicator {
    override val loadingIndicatorTitleResId: Int = urlConverter.loadingIndicatorTitleResId

    override suspend fun transition(): State {
        val url = uri.toUrl()
        if (url == null) {
            stateContext.log.e(null, "Unshorten: Failed to get URL for $uri")
            return ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
        }
        val locationHeader = try {
            when (urlConverter.shortUriMethod) {
                ShortUriMethod.GET -> stateContext.networkTools.getRedirectUrlString(url, retry)
                ShortUriMethod.HEAD -> stateContext.networkTools.requestLocationHeader(url, retry)
            }
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (tr: NetworkTools.RecoverableException) {
            return GrantedUnshortenPermission(
                stateContext,
                runContext,
                inputUriString,
                urlConverter,
                uri,
                retry = NetworkTools.Retry((retry?.count ?: 0) + 1, tr),
            )
        } catch (tr: NetworkTools.UnrecoverableException) {
            return if (tr.cause is IOException) {
                ConversionFailed(R.string.conversion_failed_unshorten_connection_error, inputUriString)
            } else {
                ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
            }
        }
        if (locationHeader == null) {
            stateContext.log.w(null, "Unshorten: Missing location header for $url")
            return ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
        }
        val unshortenedUri = Uri.parse(locationHeader, stateContext.uriQuote).toAbsoluteUri(uri)
        stateContext.log.i(null, "Unshorten: Resolved short URI $uri to $unshortenedUri")
        return UnshortenedUrl(stateContext, runContext, inputUriString, urlConverter, unshortenedUri, Permission.ALWAYS)
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
    val urlConverter: UrlConverter,
) : ConversionState() {
    override suspend fun transition(): State =
        ConversionFailed(R.string.conversion_failed_connection_permission_denied, inputUriString)
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val urlConverter: UrlConverter,
    val uri: Uri,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val positionFromUri = if (urlConverter is UrlConverter.WithUriPattern) {
            val conversionMatchers = urlConverter.conversionUriPattern.matches(uri)
            if (conversionMatchers == null) {
                stateContext.log.i(null, "URI Pattern: Failed to parse $uri")
                return ConversionFailed(R.string.conversion_failed_parse_url_error, inputUriString)
            }
            val positionFromUrl = conversionMatchers.toPosition()
            stateContext.log.i(null, "URI Pattern: Converted $uri to $positionFromUrl")
            if (positionFromUrl.points?.isNotEmpty() == true) {
                return ConversionSucceeded(stateContext, runContext, inputUriString, positionFromUrl)
            }
            positionFromUrl
        } else {
            null
        }
        if (urlConverter is UrlConverter.WithHtmlPattern) {
            return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(
                    stateContext, runContext, inputUriString, urlConverter, uri, positionFromUri
                )

                Permission.ASK -> RequestedParseHtmlPermission(
                    stateContext, runContext, inputUriString, urlConverter, uri, positionFromUri
                )

                Permission.NEVER -> ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri)
            }
        }
        return ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri)
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithHtmlPattern,
    val uri: Uri,
    val positionFromUri: Position?,
) : ConversionState(), PermissionState {
    override val permissionTitleResId: Int = urlConverter.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlPermission(
            stateContext,
            runContext,
            inputUriString,
            urlConverter,
            uri,
            positionFromUri
        )
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val runContext: ConversionRunContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithHtmlPattern,
    val uri: Uri,
    val positionFromUri: Position?,
    val retry: NetworkTools.Retry? = null,
) : ConversionState(), HasLoadingIndicator {
    override val loadingIndicatorTitleResId: Int = urlConverter.loadingIndicatorTitleResId

    override suspend fun transition(): State {
        val htmlUrl = urlConverter.getHtmlUrl(uri)
        if (htmlUrl == null) {
            stateContext.log.e(null, "HTML Pattern: Failed to get HTML URL for $uri")
            return ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        }
        val html = try {
            stateContext.log.i(null, "HTML Pattern: Downloading $htmlUrl")
            stateContext.networkTools.getText(htmlUrl, retry)
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (tr: NetworkTools.RecoverableException) {
            return GrantedParseHtmlPermission(
                stateContext,
                runContext,
                inputUriString,
                urlConverter,
                uri,
                positionFromUri,
                retry = NetworkTools.Retry((retry?.count ?: 0) + 1, tr),
            )
        } catch (tr: NetworkTools.UnrecoverableException) {
            return if (tr.cause is IOException) {
                ConversionFailed(R.string.conversion_failed_parse_html_connection_error, inputUriString)
            } else {
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
            }
        }
        urlConverter.conversionHtmlPattern?.matches(html)?.toPosition()?.let { position ->
            stateContext.log.i(null, "HTML Pattern: parsed $htmlUrl to $position")
            return ConversionSucceeded(stateContext, runContext, inputUriString, position)
        }
        urlConverter.conversionHtmlRedirectPattern?.matches(html)?.toUrlString()?.let { redirectUriString ->
            stateContext.log.i(
                null,
                "HTML Redirect Pattern: parsed $htmlUrl to redirect URI $redirectUriString"
            )
            val redirectUri = Uri.parse(redirectUriString, stateContext.uriQuote).toAbsoluteUri(uri)
            return ReceivedUri(
                stateContext,
                runContext,
                inputUriString,
                urlConverter,
                redirectUri,
                Permission.ALWAYS
            )
        }
        stateContext.log.w(null, "HTML Pattern: Failed to parse $htmlUrl")
        return ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri)
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
    val positionFromUri: Position?,
) : ConversionState() {
    override suspend fun transition() =
        if (positionFromUri != null && (!positionFromUri.points.isNullOrEmpty() || !positionFromUri.q.isNullOrEmpty())) {
            ConversionSucceeded(stateContext, runContext, inputUriString, positionFromUri)
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
        stateContext.userPreferencesRepository.getValue(automation).let { automation ->
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

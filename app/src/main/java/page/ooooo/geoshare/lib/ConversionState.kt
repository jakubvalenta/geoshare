package page.ooooo.geoshare.lib

import android.content.Intent
import androidx.annotation.StringRes
import kotlinx.coroutines.CancellationException
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.lib.converters.ShortUriMethod
import page.ooooo.geoshare.lib.converters.UrlConverter
import java.io.IOException
import java.net.MalformedURLException

open class ConversionState : State {
    override suspend fun transition(): State? = null
}

interface HasLoadingIndicator {
    val loadingIndicatorTitleResId: Int
}

interface HasResult {
    val inputUriString: String
    val position: Position
}

interface HasError {
    val errorMessageResId: Int
    val inputUriString: String
}

class Initial : ConversionState()

data class ReceivedIntent(
    val stateContext: ConversionStateContext,
    val intent: Intent,
) : ConversionState() {
    override suspend fun transition(): State {
        val inputUriString = stateContext.intentTools.getIntentUriString(intent)
            ?: return ConversionFailed(R.string.conversion_failed_missing_url, "")
        return ReceivedUriString(stateContext, inputUriString)
    }
}

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
) : ConversionState() {
    override suspend fun transition(): State {
        for (urlConverter in stateContext.urlConverters) {
            val m = urlConverter.uriPattern.matcher(inputUriString)
            if (m.find()) {
                val uri = Uri.parse(m.group(), stateContext.uriQuote)
                return ReceivedUri(stateContext, inputUriString, urlConverter, uri, null)
            }
        }
        return ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString)
    }
}

data class ReceivedUri(
    val stateContext: ConversionStateContext,
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
                    Permission.ALWAYS -> GrantedUnshortenPermission(stateContext, inputUriString, urlConverter, uri)
                    Permission.ASK -> RequestedUnshortenPermission(stateContext, inputUriString, urlConverter, uri)
                    Permission.NEVER -> DeniedConnectionPermission(stateContext, inputUriString, urlConverter)
                }
            }
        }
        return UnshortenedUrl(stateContext, inputUriString, urlConverter, uri, permission)
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithShortUriPattern,
    val uri: Uri,
) : ConversionState(), PermissionState {
    override val permissionTitleResId: Int = urlConverter.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedUnshortenPermission(stateContext, inputUriString, urlConverter, uri)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, inputUriString, urlConverter)
    }
}

data class GrantedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithShortUriPattern,
    val uri: Uri,
) : ConversionState(), HasLoadingIndicator {
    override val loadingIndicatorTitleResId: Int = urlConverter.loadingIndicatorTitleResId

    override suspend fun transition(): State {
        val locationHeader = try {
            val url = uri.toUrl()
            when (urlConverter.shortUriMethod) {
                ShortUriMethod.GET -> stateContext.networkTools.getRedirectUrlString(url)
                ShortUriMethod.HEAD -> stateContext.networkTools.requestLocationHeader(url)
            }
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (_: MalformedURLException) {
            return ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
        } catch (_: IOException) {
            // Catches SocketTimeoutException and UnexpectedResponseCodeException too.
            return ConversionFailed(R.string.conversion_failed_unshorten_connection_error, inputUriString)
        } catch (_: Exception) {
            return ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
        }
        if (locationHeader == null) {
            stateContext.log.w(null, "Unshorten: Missing location header")
            return ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString)
        }
        val unshortenedUri = Uri.parse(locationHeader, stateContext.uriQuote).toAbsoluteUri(uri)
        stateContext.log.i(null, "Unshorten: Resolved short URI $uri to $unshortenedUri")
        return UnshortenedUrl(stateContext, inputUriString, urlConverter, unshortenedUri, Permission.ALWAYS)
    }
}

data class DeniedConnectionPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter,
) : ConversionState() {
    override suspend fun transition(): State =
        ConversionFailed(R.string.conversion_failed_connection_permission_denied, inputUriString)
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
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
                return ConversionSucceeded(inputUriString, positionFromUrl)
            }
            positionFromUrl
        } else {
            null
        }
        if (urlConverter is UrlConverter.WithHtmlPattern) {
            return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    urlConverter,
                    uri,
                    positionFromUri
                )

                Permission.ASK -> RequestedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    urlConverter,
                    uri,
                    positionFromUri
                )

                Permission.NEVER -> ParseHtmlFailed(inputUriString, positionFromUri)
            }
        }
        return ParseHtmlFailed(inputUriString, positionFromUri)
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
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
        return GrantedParseHtmlPermission(stateContext, inputUriString, urlConverter, uri, positionFromUri)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return ParseHtmlFailed(inputUriString, positionFromUri)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithHtmlPattern,
    val uri: Uri,
    val positionFromUri: Position?,
) : ConversionState(), HasLoadingIndicator {
    override val loadingIndicatorTitleResId: Int = urlConverter.loadingIndicatorTitleResId

    override suspend fun transition(): State {
        val html = try {
            stateContext.log.i(null, "HTML Pattern: Downloading $uri")
            stateContext.networkTools.getText(uri.toUrl())
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled, inputUriString)
        } catch (_: MalformedURLException) {
            return ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        } catch (_: IOException) {
            // Catches SocketTimeoutException and UnexpectedResponseCodeException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_connection_error, inputUriString)
        } catch (_: Exception) {
            return ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
        }
        urlConverter.conversionHtmlPattern?.find(html)?.toPosition()?.let { position ->
            stateContext.log.i(null, "HTML Pattern: parsed $uri to $position")
            return@transition ConversionSucceeded(inputUriString, position)
        }
        urlConverter.conversionHtmlRedirectPattern?.find(html)?.toUrlString()?.let { redirectUriString ->
            stateContext.log.i(null, "HTML Redirect Pattern: parsed $uri to redirect URI $redirectUriString")
            val redirectUri = Uri.parse(redirectUriString, stateContext.uriQuote).toAbsoluteUri(uri)
            return@transition ReceivedUri(stateContext, inputUriString, urlConverter, redirectUri, Permission.ALWAYS)
        }
        stateContext.log.w(null, "HTML Pattern: Failed to parse $uri")
        return ParseHtmlFailed(inputUriString, positionFromUri)
    }
}

data class ParseHtmlFailed(
    val inputUriString: String,
    val positionFromUri: Position?,
) : ConversionState() {
    override suspend fun transition() = if (
        positionFromUri != null &&
        (!positionFromUri.points.isNullOrEmpty() || !positionFromUri.q.isNullOrEmpty())
    ) {
        ConversionSucceeded(inputUriString, positionFromUri)
    } else {
        ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString)
    }
}

data class ConversionSucceeded(
    override val inputUriString: String,
    override val position: Position,
) : ConversionState(), HasResult

data class ConversionFailed(
    @param:StringRes override val errorMessageResId: Int,
    override val inputUriString: String,
) : ConversionState(), HasError

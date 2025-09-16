package page.ooooo.geoshare.lib

import android.content.Intent
import androidx.annotation.StringRes
import kotlinx.coroutines.CancellationException
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectionPermission
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
}

class Initial : ConversionState()

data class ReceivedIntent(
    val stateContext: ConversionStateContext,
    val intent: Intent,
) : ConversionState() {
    override suspend fun transition(): State {
        val inputUriString = stateContext.intentTools.getIntentUriString(intent)
            ?: return ConversionFailed(R.string.conversion_failed_missing_url)
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
        return ConversionFailed(R.string.conversion_failed_unsupported_service)
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
                val uriString = if (urlConverter.shortUriReplacement != null) {
                    m.replaceFirst(urlConverter.shortUriReplacement)
                } else {
                    m.group()
                }
                val uri = Uri.parse(uriString, stateContext.uriQuote)
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
            stateContext.networkTools.requestLocationHeader(uri.toUrl())
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled)
        } catch (_: MalformedURLException) {
            return ConversionFailed(R.string.conversion_failed_unshorten_error)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(R.string.conversion_failed_unshorten_connection_error)
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(R.string.conversion_failed_unshorten_error)
        }
        if (locationHeader == null) {
            stateContext.log.w(null, "Missing location URL")
            return ConversionFailed(R.string.conversion_failed_unshorten_error)
        }
        val unshortenedUri = Uri.parse(locationHeader, stateContext.uriQuote).toAbsoluteUri(uri)
        stateContext.log.i(null, "Resolved short URL $uri to $unshortenedUri")
        return UnshortenedUrl(stateContext, inputUriString, urlConverter, unshortenedUri, Permission.ALWAYS)
    }
}

data class DeniedConnectionPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter,
) : ConversionState() {
    override suspend fun transition(): State = ConversionFailed(R.string.conversion_failed_connection_permission_denied)
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter,
    val uri: Uri,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        if (urlConverter is UrlConverter.WithUriPattern) {
            val conversionMatchers = urlConverter.conversionUriPattern.matches(uri)
            if (conversionMatchers == null) {
                stateContext.log.i(null, "URL could not be converted $uri")
                return ConversionFailed(R.string.conversion_failed_parse_url_error)
            }
            val position = Position(
                conversionMatchers.groupOrNull("lat"),
                conversionMatchers.groupOrNull("lon"),
                conversionMatchers.groupOrNull("q"),
                conversionMatchers.groupOrNull("z"),
            )
            if (position.lat != null && position.lon != null) {
                stateContext.log.i(null, "URL converted to position with coordinates $uri > $position")
                return ConversionSucceeded(inputUriString, position)
            }
            if (position.q != null) {
                if (urlConverter is UrlConverter.WithHtmlPattern) {
                    stateContext.log.i(
                        null,
                        "URL converted to position with place query; coordinates can be retrieved by parsing HTML $uri > $position"
                    )
                    return when (permission
                        ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                        Permission.ALWAYS -> GrantedParseHtmlToGetCoordsPermission(
                            stateContext, inputUriString, urlConverter, uri, position
                        )

                        Permission.ASK -> RequestedParseHtmlToGetCoordsPermission(
                            stateContext, inputUriString, urlConverter, uri, position
                        )

                        Permission.NEVER -> DeniedParseHtmlToGetCoordsPermission(
                            inputUriString, position
                        )
                    }
                }
                stateContext.log.i(null, "URL converted to position with place query $uri > $position")
                return ConversionSucceeded(inputUriString, position)
            }
        }
        if (urlConverter is UrlConverter.WithHtmlPattern) {
            stateContext.log.i(null, "URL will be downloaded and its HTML parsed $uri")
            return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(stateContext, inputUriString, urlConverter, uri)
                Permission.ASK -> RequestedParseHtmlPermission(stateContext, inputUriString, urlConverter, uri)
                Permission.NEVER -> DeniedConnectionPermission(stateContext, inputUriString, urlConverter)
            }
        }
        stateContext.log.i(null, "URL converter supports neither URI nor HTML pattern $uri")
        return ConversionFailed(R.string.conversion_failed_parse_url_error)
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithHtmlPattern,
    val uri: Uri,
) : ConversionState(), PermissionState {
    override val permissionTitleResId: Int = urlConverter.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlPermission(stateContext, inputUriString, urlConverter, uri)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, inputUriString, urlConverter)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithHtmlPattern,
    val uri: Uri,
) : ConversionState(), HasLoadingIndicator {
    override val loadingIndicatorTitleResId: Int = urlConverter.loadingIndicatorTitleResId

    override suspend fun transition(): State {
        val html = try {
            stateContext.networkTools.getText(uri.toUrl())
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled)
        } catch (_: MalformedURLException) {
            return ConversionFailed(R.string.conversion_failed_parse_html_error)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_connection_error)
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_error)
        }
        urlConverter.conversionHtmlPattern?.matches(html)?.let { conversionMatchers ->
            val position = Position(
                conversionMatchers.groupOrNull("lat"),
                conversionMatchers.groupOrNull("lon"),
                conversionMatchers.groupOrNull("q"),
                conversionMatchers.groupOrNull("z")
            )
            stateContext.log.i(null, "HTML parsed $position")
            return@transition ConversionSucceeded(inputUriString, position)
        }
        urlConverter.conversionHtmlRedirectPattern?.matches(html)?.let { conversionMatchers ->
            val redirectUriString = conversionMatchers.groupOrNull("url")
            if (redirectUriString != null) {
                stateContext.log.w(null, "HTML contains a redirect to $redirectUriString")
                val redirectUri = Uri.parse(redirectUriString, stateContext.uriQuote).toAbsoluteUri(uri)
                return@transition ReceivedUri(
                    stateContext,
                    inputUriString,
                    urlConverter,
                    redirectUri,
                    Permission.ALWAYS,
                )
            }
        }
        stateContext.log.w(null, "HTML could not be parsed")
        return ConversionFailed(R.string.conversion_failed_parse_html_error)
    }
}

data class RequestedParseHtmlToGetCoordsPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithHtmlPattern,
    val uri: Uri,
    val positionFromUrl: Position,
) : ConversionState(), PermissionState {
    override val permissionTitleResId: Int = urlConverter.permissionTitleResId

    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlToGetCoordsPermission(stateContext, inputUriString, urlConverter, uri, positionFromUrl)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl)
    }
}

data class GrantedParseHtmlToGetCoordsPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter.WithHtmlPattern,
    val uri: Uri,
    val positionFromUrl: Position,
) : ConversionState(), HasLoadingIndicator {
    override val loadingIndicatorTitleResId: Int = urlConverter.loadingIndicatorTitleResId

    override suspend fun transition(): State {
        val html = try {
            stateContext.networkTools.getText(uri.toUrl())
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled)
        } catch (_: MalformedURLException) {
            return ConversionFailed(R.string.conversion_failed_parse_html_error)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_connection_error)
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_error)
        }
        urlConverter.conversionHtmlPattern?.matches(html)?.let { conversionMatchers ->
            val position = Position(
                conversionMatchers.groupOrNull("lat"),
                conversionMatchers.groupOrNull("lon"),
                conversionMatchers.groupOrNull("q"),
                conversionMatchers.groupOrNull("z")
            )
            stateContext.log.i(null, "HTML parsed $position")
            return@transition ConversionSucceeded(inputUriString, position)
        }
        urlConverter.conversionHtmlRedirectPattern?.matches(html)?.let { conversionMatchers ->
            val redirectUriString = conversionMatchers.groupOrNull("url")
            if (redirectUriString != null) {
                stateContext.log.w(null, "HTML contains a redirect to $redirectUriString")
                val redirectUri = Uri.parse(redirectUriString, stateContext.uriQuote).toAbsoluteUri(uri)
                return@transition ReceivedUri(
                    stateContext,
                    inputUriString,
                    urlConverter,
                    redirectUri,
                    Permission.ALWAYS,
                )
            }
        }
        stateContext.log.w(null, "HTML could not be parsed; returning position from URL")
        return ConversionSucceeded(inputUriString, positionFromUrl)
    }
}

data class DeniedParseHtmlToGetCoordsPermission(
    val inputUriString: String,
    val positionFromUrl: Position,
) : ConversionState() {
    override suspend fun transition(): State = ConversionSucceeded(inputUriString, positionFromUrl)
}

data class ConversionSucceeded(
    override val inputUriString: String,
    override val position: Position,
) : ConversionState(), HasResult

data class ConversionFailed(
    @param:StringRes override val errorMessageResId: Int,
) : ConversionState(), HasError

package page.ooooo.geoshare.lib

import android.content.Intent
import androidx.annotation.StringRes
import kotlinx.coroutines.CancellationException
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.lib.converters.ParseHtmlResult
import page.ooooo.geoshare.lib.converters.UrlConverter
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

open class ConversionState : State {
    override suspend fun transition(): State? = null
}

interface HasLoadingIndicator {
    val urlConverter: UrlConverter
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
    private val uriQuote: UriQuote = DefaultUriQuote(),
) : ConversionState() {
    override suspend fun transition(): State {
        val position = stateContext.intentTools.getIntentPosition(intent)
        if (position != null) {
            return ConversionSucceeded(intent.data.toString(), position)
        }
        val inputUriString = stateContext.intentTools.getIntentUriString(intent) ?: return ConversionFailed(
            R.string.conversion_failed_missing_url
        )
        return ReceivedUriString(stateContext, inputUriString, uriQuote)
    }
}

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    private val uriQuote: UriQuote = DefaultUriQuote(),
) : ConversionState() {
    override suspend fun transition(): State {
        val position = Position.fromGeoUriString(inputUriString, uriQuote)
        if (position != null) {
            return ConversionSucceeded(inputUriString, position)
        }
        val inputUriStringWithHttpsScheme = inputUriString.replace("^([a-z]+:)?(//)?(.)".toRegex(), "https://$3")
        val url = try {
            URL(inputUriStringWithHttpsScheme)
        } catch (_: MalformedURLException) {
            return ConversionFailed(R.string.conversion_failed_invalid_url)
        }
        return ReceivedUrl(stateContext, inputUriString, url, null)
    }
}

data class ReceivedUrl(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val urlConverter = stateContext.urlConverters.find { it.host.matches(url.host) } ?: return ConversionFailed(
            R.string.conversion_failed_unsupported_service
        )
        if (urlConverter.shortUrlHost?.matches(url.host) == true) {
            return UnshortenedUrl(stateContext, inputUriString, urlConverter, url, permission)
        }
        return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
            Permission.ALWAYS -> GrantedUnshortenPermission(stateContext, inputUriString, urlConverter, url)
            Permission.ASK -> RequestedUnshortenPermission(stateContext, inputUriString, urlConverter, url)
            Permission.NEVER -> DeniedConnectionPermission(stateContext, inputUriString, urlConverter)
        }
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedUnshortenPermission(stateContext, inputUriString, urlConverter, url)
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
    override val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), HasLoadingIndicator {
    override suspend fun transition(): State {
        val header = try {
            stateContext.networkTools.requestLocationHeader(url)
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
        return UnshortenedUrl(stateContext, inputUriString, urlConverter, header, Permission.ALWAYS)
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
    val url: URL,
    val permission: Permission?,
    private val uriQuote: UriQuote = DefaultUriQuote(),
) : ConversionState() {
    override suspend fun transition(): State {
        val position = urlConverter.pattern.matches(
            url.host,
            uriQuote.decode(url.path),
            getUrlQueryParams(url.query, uriQuote),
        )
        if (position != null) {
            if (position.lat != null && position.lon != null) {
                stateContext.log.i(null, "URL converted to position with coordinates $url > $position")
                return ConversionSucceeded(inputUriString, position)
            }
            if (position.q != null) {
                stateContext.log.i(
                    null,
                    "URL converted to position with place query; coordinates can be retrieved by parsing HTML $url > $position"
                )
                return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                    Permission.ALWAYS -> GrantedParseHtmlToGetCoordsPermission(
                        stateContext, inputUriString, urlConverter, url, position
                    )

                    Permission.ASK -> RequestedParseHtmlToGetCoordsPermission(
                        stateContext, inputUriString, urlConverter, url, position
                    )

                    Permission.NEVER -> DeniedParseHtmlToGetCoordsPermission(
                        inputUriString, position
                    )
                }
            }
            stateContext.log.i(null, "URL cannot be converted without parsing HTML $url")
            return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(stateContext, inputUriString, urlConverter, url)
                Permission.ASK -> RequestedParseHtmlPermission(stateContext, inputUriString, urlConverter, url)
                Permission.NEVER -> DeniedConnectionPermission(stateContext, inputUriString, urlConverter)
            }
        }
        stateContext.log.i(null, "URL could not be converted $url")
        return ConversionFailed(R.string.conversion_failed_parse_url_error)
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlPermission(stateContext, inputUriString, urlConverter, url)
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
    override val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), HasLoadingIndicator {
    override suspend fun transition(): State {
        val html = try {
            stateContext.networkTools.getText(url)
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_connection_error)
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_error)
        }
        return when (val parseHtmlResult = urlConverter.parseHtml?.invoke(html)) {
            is ParseHtmlResult.Parsed -> {
                stateContext.log.i(null, "HTML parsed ${parseHtmlResult.position}")
                return ConversionSucceeded(inputUriString, parseHtmlResult.position)
            }

            is ParseHtmlResult.Redirect -> {
                stateContext.log.w(null, "HTML contains a redirect to ${parseHtmlResult.url}")
                ReceivedUrl(stateContext, inputUriString, parseHtmlResult.url, Permission.ALWAYS)
            }

            null -> {
                stateContext.log.w(null, "HTML could not be parsed")
                return ConversionFailed(R.string.conversion_failed_parse_html_error)
            }
        }
    }
}

data class RequestedParseHtmlToGetCoordsPermission(
    val stateContext: ConversionStateContext,
    val inputUriString: String,
    val urlConverter: UrlConverter,
    val url: URL,
    val positionFromUrl: Position,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlToGetCoordsPermission(stateContext, inputUriString, urlConverter, url, positionFromUrl)
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
    override val urlConverter: UrlConverter,
    val url: URL,
    val positionFromUrl: Position,
) : ConversionState(), HasLoadingIndicator {
    override suspend fun transition(): State {
        val html = try {
            stateContext.networkTools.getText(url)
        } catch (_: CancellationException) {
            return ConversionFailed(R.string.conversion_failed_cancelled)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_connection_error)
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(R.string.conversion_failed_parse_html_error)
        }
        return when (val parseHtmlResult = urlConverter.parseHtml?.invoke(html)) {
            is ParseHtmlResult.Parsed -> {
                stateContext.log.i(null, "HTML parsed ${parseHtmlResult.position}")
                return ConversionSucceeded(inputUriString, parseHtmlResult.position)
            }

            is ParseHtmlResult.Redirect -> {
                stateContext.log.w(null, "HTML contains a redirect to ${parseHtmlResult.url}")
                ReceivedUrl(stateContext, inputUriString, parseHtmlResult.url, Permission.ALWAYS)
            }

            null -> {
                stateContext.log.w(null, "HTML could not be parsed; returning position from URL")
                return ConversionSucceeded(inputUriString, positionFromUrl)
            }
        }
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

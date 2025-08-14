package page.ooooo.geoshare.lib

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toUri
import kotlinx.coroutines.CancellationException
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.lib.converters.ParseHtmlResult
import page.ooooo.geoshare.lib.converters.ParseUrlResult
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
    val inputUri: String
    val geoUri: String
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
        val geoUri = stateContext.intentTools.getIntentGeoUri(intent)
        if (geoUri != null) {
            return ConversionSucceeded(intent.data.toString(), geoUri)
        }
        val inputUri = stateContext.intentTools.getIntentUriString(intent) ?: return ConversionFailed(
            R.string.conversion_failed_missing_url
        )
        return ReceivedUrlString(stateContext, inputUri, null)
    }
}

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    private val parseUri: (String) -> Uri = { s -> s.toUri() },
) : ConversionState() {
    override suspend fun transition(): State {
        val uri = parseUri(inputUri)
        if (uri.scheme == "geo") {
            return ConversionSucceeded(inputUri, inputUri)
        }
        return ReceivedUrlString(stateContext, inputUri, null)
    }
}

data class ReceivedUrlString(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val inputUriWithHttpsScheme = inputUri.replace("^([a-z]+:)?(//)?(.)".toRegex(), "https://$3")
        val url = try {
            URL(inputUriWithHttpsScheme)
        } catch (_: MalformedURLException) {
            return ConversionFailed(R.string.conversion_failed_invalid_url)
        }
        return ReceivedUrl(stateContext, inputUri, url, permission)
    }
}

data class ReceivedUrl(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val urlConverter = stateContext.urlConverters.find { it.isSupportedUrl(url) } ?: return ConversionFailed(
            R.string.conversion_failed_unsupported_service
        )
        if (!urlConverter.isShortUrl(url)) {
            return UnshortenedUrl(stateContext, inputUri, urlConverter, url, permission)
        }
        return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
            Permission.ALWAYS -> GrantedUnshortenPermission(stateContext, inputUri, urlConverter, url)
            Permission.ASK -> RequestedUnshortenPermission(stateContext, inputUri, urlConverter, url)
            Permission.NEVER -> DeniedConnectionPermission(stateContext, inputUri, urlConverter)
        }
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedUnshortenPermission(stateContext, inputUri, urlConverter, url)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, inputUri, urlConverter)
    }
}

data class GrantedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val inputUri: String,
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
        return UnshortenedUrl(stateContext, inputUri, urlConverter, header, Permission.ALWAYS)
    }
}

data class DeniedConnectionPermission(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    val urlConverter: UrlConverter,
) : ConversionState() {
    override suspend fun transition(): State =
        ConversionFailed(R.string.conversion_failed_connection_permission_denied)
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    val urlConverter: UrlConverter,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        return when (val parseUrlResult = urlConverter.parseUrl(url)) {
            is ParseUrlResult.Parsed -> ConversionSucceeded(
                inputUri,
                parseUrlResult.geoUriBuilder.toString(),
            )

            is ParseUrlResult.RequiresHtmlParsing -> when (permission
                ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(stateContext, inputUri, urlConverter, url)
                Permission.ASK -> RequestedParseHtmlPermission(stateContext, inputUri, urlConverter, url)
                Permission.NEVER -> DeniedConnectionPermission(stateContext, inputUri, urlConverter)
            }

            is ParseUrlResult.RequiresHtmlParsingToGetCoords -> when (permission
                ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUri,
                    urlConverter,
                    url,
                    parseUrlResult.geoUriBuilder.toString(),
                )

                Permission.ASK -> RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUri,
                    urlConverter,
                    url,
                    parseUrlResult.geoUriBuilder.toString(),
                )

                Permission.NEVER -> DeniedParseHtmlToGetCoordsPermission(
                    inputUri,
                    parseUrlResult.geoUriBuilder.toString(),
                )
            }

            null -> ConversionFailed(R.string.conversion_failed_parse_url_error)
        }
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlPermission(stateContext, inputUri, urlConverter, url)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, inputUri, urlConverter)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val inputUri: String,
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
        return when (val parseHtmlResult = urlConverter.parseHtml(html)) {
            is ParseHtmlResult.Parsed -> ConversionSucceeded(inputUri, parseHtmlResult.geoUriBuilder.toString())
            is ParseHtmlResult.Redirect -> ReceivedUrl(stateContext, inputUri, parseHtmlResult.url, Permission.ALWAYS)
            null -> return ConversionFailed(R.string.conversion_failed_parse_html_error)
        }
    }
}

data class RequestedParseHtmlToGetCoordsPermission(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    val urlConverter: UrlConverter,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlToGetCoordsPermission(stateContext, inputUri, urlConverter, url, geoUriFromUrl)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedParseHtmlToGetCoordsPermission(inputUri, geoUriFromUrl)
    }
}

data class GrantedParseHtmlToGetCoordsPermission(
    val stateContext: ConversionStateContext,
    val inputUri: String,
    override val urlConverter: UrlConverter,
    val url: URL,
    val geoUriFromUrl: String,
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
        return when (val parseHtmlResult = urlConverter.parseHtml(html)) {
            is ParseHtmlResult.Parsed -> ConversionSucceeded(inputUri, parseHtmlResult.geoUriBuilder.toString())
            is ParseHtmlResult.Redirect -> ReceivedUrl(stateContext, inputUri, parseHtmlResult.url, Permission.ALWAYS)
            null -> ConversionSucceeded(inputUri, geoUriFromUrl)
        }
    }
}

data class DeniedParseHtmlToGetCoordsPermission(
    val inputUri: String,
    val geoUriFromUrl: String,
) : ConversionState() {
    override suspend fun transition(): State = ConversionSucceeded(inputUri, geoUriFromUrl)
}

data class ConversionSucceeded(
    override val inputUri: String,
    override val geoUri: String,
) : ConversionState(), HasResult

data class ConversionFailed(
    @param:StringRes override val errorMessageResId: Int,
) : ConversionState(), HasError

package page.ooooo.geoshare.lib

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
    val intentData: Uri?
    val geoUri: String
}

interface HasError {
    val intentData: Uri?
    val errorMessageResId: Int
}

class Initial : ConversionState()

data class ReceivedIntent(
    val stateContext: ConversionStateContext,
    val intent: Intent,
) : ConversionState() {
    override suspend fun transition(): State {
        val geoUri = stateContext.intentTools.getIntentGeoUri(intent)
        val intentData = intent.data
        if (geoUri != null) {
            return ConversionSucceeded(intentData, geoUri)
        }
        val urlString = stateContext.intentTools.getIntentUrlString(intent) ?: return ConversionFailed(
            intentData, R.string.conversion_failed_missing_url
        )
        return ReceivedUrlString(stateContext, intentData, urlString, null)
    }
}

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    val uriString: String,
    private val parseUri: (String) -> Uri = { s -> s.toUri() },
) : ConversionState() {
    override suspend fun transition(): State {
        val uri = parseUri(uriString)
        if (uri.scheme == "geo") {
            return ConversionSucceeded(intentData, uriString)
        }
        return ReceivedUrlString(stateContext, intentData, uriString, null)
    }
}

data class ReceivedUrlString(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    val urlString: String,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val urlStringWithHttpsScheme = urlString.replace("^([a-z]+:)?(//)?(.)".toRegex(), "https://$3")
        val url = try {
            URL(urlStringWithHttpsScheme)
        } catch (_: MalformedURLException) {
            return ConversionFailed(intentData, R.string.conversion_failed_invalid_url)
        }
        return ReceivedUrl(stateContext, intentData, url, permission)
    }
}

data class ReceivedUrl(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val urlConverter = stateContext.urlConverters.find { it.isSupportedUrl(url) } ?: return ConversionFailed(
            intentData, R.string.conversion_failed_unsupported_service
        )
        if (!urlConverter.isShortUrl(url)) {
            return UnshortenedUrl(stateContext, intentData, urlConverter, url, permission)
        }
        return when (permission ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
            Permission.ALWAYS -> GrantedUnshortenPermission(stateContext, intentData, urlConverter, url)
            Permission.ASK -> RequestedUnshortenPermission(stateContext, intentData, urlConverter, url)
            Permission.NEVER -> DeniedConnectionPermission(stateContext, intentData, urlConverter)
        }
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedUnshortenPermission(stateContext, intentData, urlConverter, url)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, intentData, urlConverter)
    }
}

data class GrantedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    override val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), HasLoadingIndicator {
    override suspend fun transition(): State {
        val header = try {
            stateContext.networkTools.requestLocationHeader(url)
        } catch (_: CancellationException) {
            return ConversionFailed(intentData, R.string.conversion_failed_cancelled)
        } catch (_: MalformedURLException) {
            return ConversionFailed(intentData, R.string.conversion_failed_unshorten_error)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(intentData, R.string.conversion_failed_unshorten_connection_error)
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(intentData, R.string.conversion_failed_unshorten_error)
        }
        return UnshortenedUrl(stateContext, intentData, urlConverter, header, Permission.ALWAYS)
    }
}

data class DeniedConnectionPermission(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    val urlConverter: UrlConverter,
) : ConversionState() {
    override suspend fun transition(): State =
        ConversionFailed(intentData, R.string.conversion_failed_connection_permission_denied)
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    val urlConverter: UrlConverter,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        return when (val parseUrlResult = urlConverter.parseUrl(url)) {
            is ParseUrlResult.Parsed -> ConversionSucceeded(
                intentData,
                parseUrlResult.geoUriBuilder.toString(),
            )

            is ParseUrlResult.RequiresHtmlParsing -> when (permission
                ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(stateContext, intentData, urlConverter, url)
                Permission.ASK -> RequestedParseHtmlPermission(stateContext, intentData, urlConverter, url)
                Permission.NEVER -> DeniedConnectionPermission(stateContext, intentData, urlConverter)
            }

            is ParseUrlResult.RequiresHtmlParsingToGetCoords -> when (permission
                ?: stateContext.userPreferencesRepository.getValue(connectionPermission)) {
                Permission.ALWAYS -> GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    intentData,
                    urlConverter,
                    url,
                    parseUrlResult.geoUriBuilder.toString(),
                )

                Permission.ASK -> RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    intentData,
                    urlConverter,
                    url,
                    parseUrlResult.geoUriBuilder.toString(),
                )

                Permission.NEVER -> DeniedParseHtmlToGetCoordsPermission(
                    intentData,
                    parseUrlResult.geoUriBuilder.toString(),
                )
            }

            null -> ConversionFailed(intentData, R.string.conversion_failed_parse_url_error)
        }
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlPermission(stateContext, intentData, urlConverter, url)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedConnectionPermission(stateContext, intentData, urlConverter)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    override val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), HasLoadingIndicator {
    override suspend fun transition(): State {
        val html = try {
            stateContext.networkTools.getText(url)
        } catch (_: CancellationException) {
            return ConversionFailed(intentData, R.string.conversion_failed_cancelled)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(intentData, R.string.conversion_failed_parse_html_connection_error)
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(intentData, R.string.conversion_failed_parse_html_error)
        }
        return when (val parseHtmlResult = urlConverter.parseHtml(html)) {
            is ParseHtmlResult.Parsed -> ConversionSucceeded(intentData, parseHtmlResult.geoUriBuilder.toString())
            is ParseHtmlResult.Redirect -> ReceivedUrl(stateContext, intentData, parseHtmlResult.url, Permission.ALWAYS)
            null -> return ConversionFailed(intentData, R.string.conversion_failed_parse_html_error)
        }
    }
}

data class RequestedParseHtmlToGetCoordsPermission(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    val urlConverter: UrlConverter,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.ALWAYS)
        }
        return GrantedParseHtmlToGetCoordsPermission(stateContext, intentData, urlConverter, url, geoUriFromUrl)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(connectionPermission, Permission.NEVER)
        }
        return DeniedParseHtmlToGetCoordsPermission(intentData, geoUriFromUrl)
    }
}

data class GrantedParseHtmlToGetCoordsPermission(
    val stateContext: ConversionStateContext,
    val intentData: Uri?,
    override val urlConverter: UrlConverter,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState(), HasLoadingIndicator {
    override suspend fun transition(): State {
        val html = try {
            stateContext.networkTools.getText(url)
        } catch (_: CancellationException) {
            return ConversionFailed(intentData, R.string.conversion_failed_cancelled)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(intentData, R.string.conversion_failed_parse_html_connection_error)
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(intentData, R.string.conversion_failed_parse_html_error)
        }
        return when (val parseHtmlResult = urlConverter.parseHtml(html)) {
            is ParseHtmlResult.Parsed -> ConversionSucceeded(intentData, parseHtmlResult.geoUriBuilder.toString())
            is ParseHtmlResult.Redirect -> ReceivedUrl(stateContext, intentData, parseHtmlResult.url, Permission.ALWAYS)
            null -> ConversionSucceeded(intentData, geoUriFromUrl)
        }
    }
}

data class DeniedParseHtmlToGetCoordsPermission(
    val intentData: Uri?,
    val geoUriFromUrl: String,
) : ConversionState() {
    override suspend fun transition(): State = ConversionSucceeded(intentData, geoUriFromUrl)
}

data class ConversionSucceeded(
    override val intentData: Uri?,
    override val geoUri: String,
) : ConversionState(), HasResult

data class ConversionFailed(
    override val intentData: Uri?,
    override val errorMessageResId: Int,
) : ConversionState(), HasError

data class AcceptedSharing(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    override val intentData: Uri?,
    override val geoUri: String,
) : ConversionState(), HasResult {
    override suspend fun transition(): State = if (stateContext.xiaomiTools.isBackgroundStartActivityPermissionGranted(
            context
        )
    ) {
        GrantedSharePermission(stateContext, context, intentData, geoUri)
    } else {
        RequestedSharePermission(stateContext, context, settingsLauncherWrapper, intentData, geoUri)
    }
}

data class RequestedSharePermission(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    override val intentData: Uri?,
    override val geoUri: String,
) : ConversionState(), PermissionState, HasResult {
    override suspend fun grant(doNotAsk: Boolean): State =
        if (stateContext.xiaomiTools.showPermissionEditor(context, settingsLauncherWrapper)) {
            ShowedSharePermissionEditor(stateContext, context, settingsLauncherWrapper, intentData, geoUri)
        } else {
            SharingFailed(stateContext, intentData, geoUri, R.string.sharing_failed_xiaomi_permission_show_editor_error)
        }

    override suspend fun deny(doNotAsk: Boolean): State =
        SharingFailed(stateContext, intentData, geoUri, R.string.sharing_failed_xiaomi_permission_denied)
}

data class ShowedSharePermissionEditor(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    override val intentData: Uri?,
    override val geoUri: String,
) : ConversionState(), PermissionState, HasResult {
    /**
     * Share again after the permission editor has been closed.
     */
    override suspend fun grant(doNotAsk: Boolean): State =
        AcceptedSharing(stateContext, context, settingsLauncherWrapper, intentData, geoUri)

    override suspend fun deny(doNotAsk: Boolean): State {
        throw NotImplementedError("It is not possible to deny sharing again after the permission editor has been closed")
    }
}

data class GrantedSharePermission(
    val stateContext: ConversionStateContext,
    val context: Context,
    override val intentData: Uri?,
    override val geoUri: String,
) : ConversionState(), HasResult {
    override suspend fun transition(): State? = try {
        context.startActivity(stateContext.intentTools.createChooser(geoUri.toUri()))
        SharingSucceeded(stateContext, intentData, geoUri)
    } catch (_: ActivityNotFoundException) {
        SharingFailed(stateContext, intentData, geoUri, R.string.sharing_failed_activity_not_found)
    }
}

data class SharingSucceeded(
    val stateContext: ConversionStateContext,
    override val intentData: Uri?,
    override val geoUri: String,
) : ConversionState(), HasResult

data class SharingFailed(
    val stateContext: ConversionStateContext,
    override val intentData: Uri?,
    override val geoUri: String,
    override val errorMessageResId: Int,
) : ConversionState(), HasResult, HasError

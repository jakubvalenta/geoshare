package page.ooooo.geoshare.lib

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.platform.Clipboard
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectToGooglePermission
import page.ooooo.geoshare.lib.converters.UrlConverter
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

open class ConversionState : State {
    override suspend fun transition(): State? = null
}

class Initial : ConversionState()

data class ReceivedIntent(
    val stateContext: ConversionStateContext,
    val intent: Intent,
) : ConversionState() {
    override suspend fun transition(): State {
        if (stateContext.intentTools.isProcessed(intent)) {
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_nothing_to_do
            )
        }
        val geoUri = stateContext.intentTools.getIntentGeoUri(intent)
        if (geoUri != null) {
            return ConversionSucceeded(geoUri)
        }
        val urlString = stateContext.intentTools.getIntentUrlString(intent)
            ?: return ConversionFailed(
                stateContext,
                R.string.conversion_failed_missing_url
            )
        return ReceivedUrlString(stateContext, urlString, null)
    }
}

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val uriString: String,
    private val parseUri: (String) -> Uri = { s -> Uri.parse(s) },
) : ConversionState() {
    override suspend fun transition(): State {
        val uri = parseUri(uriString)
        if (uri.scheme == "geo") {
            return ConversionSucceeded(uriString)
        }
        return ReceivedUrlString(stateContext, uriString, null)
    }
}

data class ReceivedUrlString(
    val stateContext: ConversionStateContext,
    val urlString: String,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val urlStringWithHttpsScheme = urlString.replace(
            "^([a-z]+:)?(//)?(.)".toRegex(),
            "https://$3",
        )
        val url = try {
            URL(urlStringWithHttpsScheme)
        } catch (_: MalformedURLException) {
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_invalid_url
            )
        }
        return ReceivedUrl(stateContext, url, permission)
    }
}

data class ReceivedUrl(
    val stateContext: ConversionStateContext,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val urlConverter = stateContext.urlConverters.find { it.isSupportedUrl(url) }
        if (urlConverter == null) {
            return ConversionFailed(stateContext, R.string.conversion_failed_unsupported_service)
        }
        val isShortUrl = urlConverter.isShortUrl(url)
        if (!isShortUrl) {
            return UnshortenedUrl(stateContext, urlConverter, url, permission)
        }
        return when (permission
            ?: stateContext.userPreferencesRepository.getValue(
                connectToGooglePermission
            )) {
            Permission.ALWAYS -> GrantedUnshortenPermission(stateContext, urlConverter, url)

            Permission.ASK -> RequestedUnshortenPermission(stateContext, urlConverter, url)

            Permission.NEVER -> DeniedUnshortenPermission(stateContext)
        }
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.ALWAYS,
            )
        }
        return GrantedUnshortenPermission(stateContext, urlConverter, url)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.NEVER,
            )
        }
        return DeniedUnshortenPermission(stateContext)
    }
}

data class GrantedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val urlConverter: UrlConverter,
    val url: URL,
) : ConversionState() {
    override suspend fun transition(): State {
        val header = try {
            stateContext.networkTools.requestLocationHeader(url)
        } catch (_: MalformedURLException) {
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_unshorten_error
            )
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_unshorten_connection_error
            )
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_unshorten_error
            )
        }
        return UnshortenedUrl(stateContext, urlConverter, header, Permission.ALWAYS)
    }
}

class DeniedUnshortenPermission(
    val stateContext: ConversionStateContext,
) : ConversionState() {
    override suspend fun transition(): State = ConversionFailed(
        stateContext,
        R.string.conversion_failed_unshorten_permission_denied,
    )
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val urlConverter: UrlConverter,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val geoUriBuilderFromUrl =
            urlConverter.parseUrl(url)
                ?: return ConversionFailed(
                    stateContext,
                    R.string.conversion_failed_parse_url_error
                )
        val geoUriFromUrl = geoUriBuilderFromUrl.toString()
        if (geoUriBuilderFromUrl.coords.lat == "0" && geoUriBuilderFromUrl.coords.lon == "0") {
            return when (permission
                ?: stateContext.userPreferencesRepository.getValue(
                    connectToGooglePermission
                )) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(
                    stateContext,
                    urlConverter,
                    url,
                    geoUriFromUrl,
                )

                Permission.ASK -> RequestedParseHtmlPermission(
                    stateContext,
                    urlConverter,
                    url,
                    geoUriFromUrl,
                )

                Permission.NEVER -> DeniedParseHtmlPermission(geoUriFromUrl)
            }
        }
        return ConversionSucceeded(geoUriFromUrl)
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val urlConverter: UrlConverter,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.ALWAYS,
            )
        }
        return GrantedParseHtmlPermission(stateContext, urlConverter, url, geoUriFromUrl)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.NEVER,
            )
        }
        return DeniedParseHtmlPermission(geoUriFromUrl)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val urlConverter: UrlConverter,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState() {
    override suspend fun transition(): State {
        val html = try {
            stateContext.networkTools.getText(url)
        } catch (_: IOException) {
            // Catches SocketTimeoutException too.
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_parse_html_connection_error,
            )
        } catch (_: Exception) {
            // Catches UnexpectedResponseCodeException too.
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_parse_html_error,
            )
        }
        val parseHtmlResult = urlConverter.parseHtml(html)
        return when (parseHtmlResult) {
            is UrlConverter.ParseHtmlResult.Parsed -> ConversionSucceeded(
                parseHtmlResult.geoUriBuilder.toString(),
            )

            is UrlConverter.ParseHtmlResult.Redirect -> ReceivedUrl(
                stateContext,
                parseHtmlResult.url,
                Permission.ALWAYS,
            )

            null -> ConversionSucceeded(geoUriFromUrl)
        }
    }
}

data class DeniedParseHtmlPermission(
    val geoUriFromUrl: String,
) : ConversionState() {
    override suspend fun transition(): State = ConversionSucceeded(geoUriFromUrl)
}

data class ConversionSucceeded(val geoUri: String) : ConversionState()

data class ConversionFailed(
    val stateContext: ConversionStateContext,
    val messageResId: Int,
) : ConversionState() {
    override suspend fun transition(): State? {
        stateContext.onMessage(Message(messageResId, Message.Type.ERROR))
        return null
    }
}

data class AcceptedSharing(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    val geoUri: String,
) : ConversionState() {
    override suspend fun transition(): State =
        if (stateContext.xiaomiTools.isBackgroundStartActivityPermissionGranted(
                context
            )
        ) {
            GrantedSharePermission(
                stateContext,
                context,
                geoUri,
            )
        } else {
            RequestedSharePermission(
                stateContext,
                context,
                settingsLauncherWrapper,
                geoUri,
            )
        }
}

data class RequestedSharePermission(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    val geoUri: String,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State =
        if (stateContext.xiaomiTools.showPermissionEditor(
                context,
                settingsLauncherWrapper,
            )
        ) {
            ShowedSharePermissionEditor(
                stateContext,
                context,
                settingsLauncherWrapper,
                geoUri,
            )
        } else {
            SharingFailed(
                stateContext,
                R.string.sharing_failed_xiaomi_permission_show_editor_error
            )
        }

    override suspend fun deny(doNotAsk: Boolean): State =
        SharingFailed(
            stateContext,
            R.string.sharing_failed_xiaomi_permission_denied
        )
}

data class ShowedSharePermissionEditor(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    val geoUri: String,
) : ConversionState(), PermissionState {
    /**
     * Share again after the permission editor has been closed.
     */
    override suspend fun grant(doNotAsk: Boolean): State =
        AcceptedSharing(
            stateContext,
            context,
            settingsLauncherWrapper,
            geoUri,
        )

    override suspend fun deny(doNotAsk: Boolean): State {
        throw NotImplementedError("It is not possible to deny sharing again after the permission editor has been closed")
    }
}

data class GrantedSharePermission(
    val stateContext: ConversionStateContext,
    val context: Context,
    val geoUri: String,
) : ConversionState() {
    override suspend fun transition(): State? = try {
        stateContext.intentTools.share(context, Intent.ACTION_VIEW, geoUri)
        SharingSucceeded(stateContext, R.string.sharing_succeeded)
    } catch (_: ActivityNotFoundException) {
        SharingFailed(
            stateContext,
            R.string.sharing_failed_activity_not_found,
        )
    }
}

data class SharingSucceeded(
    val stateContext: ConversionStateContext,
    val messageResId: Int,
) : ConversionState() {
    override suspend fun transition(): State? {
        stateContext.onMessage(Message(messageResId, Message.Type.SUCCESS))
        return null
    }
}

data class SharingFailed(
    val stateContext: ConversionStateContext,
    val messageResId: Int,
) : ConversionState() {
    override suspend fun transition(): State? {
        stateContext.onMessage(Message(messageResId, Message.Type.ERROR))
        return null
    }
}

data class AcceptedCopying(
    val stateContext: ConversionStateContext,
    val clipboard: Clipboard,
    val geoUri: String,
) : ConversionState() {
    override suspend fun transition(): State {
        stateContext.clipboardTools.setPlainText(clipboard, "geo: URI", geoUri)
        return CopyingFinished(stateContext)
    }
}

data class CopyingFinished(val stateContext: ConversionStateContext) : ConversionState() {
    override suspend fun transition(): State? {
        val systemHasClipboardEditor =
            stateContext.getBuildVersionSdkInt() >= Build.VERSION_CODES.TIRAMISU
        if (!systemHasClipboardEditor) {
            stateContext.onMessage(Message(R.string.copying_finished, Message.Type.SUCCESS))
        }
        return null
    }
}

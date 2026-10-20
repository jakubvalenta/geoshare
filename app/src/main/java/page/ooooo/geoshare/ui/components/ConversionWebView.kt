package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CompletableDeferred
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.data.network.WebViewNetworkException
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val JAVA_SCRIPT_INTERFACE_NAME = "Android"
private const val TAG = "ConversionWebView"

@Composable
fun ConversionWebView(
    url: String,
    allowedUrlPattern: Regex,
    pendingResult: CompletableDeferred<String>,
    unsafeClientJavascript: String,
    extendWebSettings: (settings: WebSettings) -> Unit,
    shouldInterceptRequest: (requestUrlString: String) -> Boolean,
    // Set window size minus a common browser chrome size, so the numbers seem real, in case a web page checks
    sizePx: Size = Size(1080 - 2f, 1920f - 277f),
    readInterval: Duration = 500.milliseconds,
) {
    // As an extra layer of security, allow only specific URLs to be loaded in the WebView
    if (!allowedUrlPattern.matches(url)) {
        pendingResult.completeExceptionally(IllegalStateException("Page blocked"))
        return
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val size = with(density) { sizePx.toDpSize() }

    // Render a placeholder in Preview, because WebView is not supported there
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        Box(
            Modifier
                .requiredSize(size)
                .background(Color(0x80FFFFFF)),
            contentAlignment = Alignment.Center,
        ) {
            Text("WebView: $url")
        }
        return
    }

    AndroidView(
        factory = {
            WebView(context).apply {
                // Set layout params width and height, otherwise the web page is invisible for some reason
                layoutParams = ViewGroup.LayoutParams(sizePx.width.roundToInt(), sizePx.height.roundToInt())

                // Set background to prevent a visible white rectangle before the URL loads
                setBackgroundColor(0x00000000)

                // Consume and suppress all touch events
                @SuppressLint("ClickableViewAccessibility")
                setOnTouchListener { _, _ -> true }

                // Don't allow cookies
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(false)
                cookieManager.setAcceptThirdPartyCookies(this, false)

                // Allow JavaScript and configure its security
                settings.allowContentAccess = false
                settings.allowFileAccess = false
                @SuppressLint("SetJavaScriptEnabled")
                settings.javaScriptEnabled = true

                // Don't set a custom user agent by default, because it makes Google Maps return an error page. If a
                // particular Input requires a custom user agent, it can override extendWebSettings.
                extendWebSettings(settings)

                webChromeClient = object : WebChromeClient() {
                    /**
                     * Returns true for messages that should be excluded from logcat
                     */
                    override fun onConsoleMessage(cm: ConsoleMessage) =
                        (cm.messageLevel() != ConsoleMessage.MessageLevel.ERROR &&
                            cm.messageLevel() != ConsoleMessage.MessageLevel.WARNING) ||
                            cm.message().startsWith("Mixed Content")
                }

                addJavascriptInterface(
                    object {
                        @Suppress("unused")
                        @JavascriptInterface
                        fun onSuccess(result: String) {
                            Log.d(TAG, "Client JavaScript succeeded $result")
                            pendingResult.complete(result)
                        }

                        @Suppress("unused")
                        @JavascriptInterface
                        fun onFailure() {
                            Log.w(TAG, "Client JavaScript failed")
                            pendingResult.completeExceptionally(WebViewNetworkException())
                        }
                    },
                    JAVA_SCRIPT_INTERFACE_NAME,
                )

                webViewClient = object : WebViewClient() {
                    /**
                     * Runs client JavaScript repeatedly every [readInterval]
                     */
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        view?.evaluateJavascript(
                            // language=JavaScript
                            """
                                (() => {
                                    const read = $unsafeClientJavascript;
                                    function readAndCallback() {
                                        try {
                                            switch (location.protocol) {
                                                case 'about:':
                                                    // Try again if the page is about:blank
                                                    break;
                                                case 'chrome-error:':
                                                    $JAVA_SCRIPT_INTERFACE_NAME.onFailure();
                                                    break;
                                                default:
                                                    const result = read();
                                                    if (result !== null && result !== undefined) {
                                                        $JAVA_SCRIPT_INTERFACE_NAME.onSuccess(result);
                                                    }
                                            }
                                        } catch (ex) {
                                            console.error("Client JavaScript exception", ex);
                                        }
                                    }
                                    readAndCallback();
                                    setInterval(readAndCallback, ${readInterval.inWholeMilliseconds});
                                })();
                            """.trimIndent(),
                            null,
                        )
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): WebResourceResponse? {
                        request?.url?.toString()?.let { requestUrlString ->
                            if (shouldInterceptRequest(requestUrlString)) {
                                return WebResourceResponse("text/plain", "utf-8", null)
                            }
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "Allowed request to $requestUrlString")
                            }
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                loadUrl(url)
            }
        },
        modifier = Modifier.requiredSize(size),
        update = { webView ->
            webView.apply {
                if (this.url != url) {
                    loadUrl(url)
                }
            }
        },
        onReset = { webView ->
            webView.apply {
                clearHistory()
            }
        },
        onRelease = { webView ->
            webView.apply {
                // Destroy the WebView to stop it making network connections in the background and to free memory. Some
                // of the method calls might be unnecessary, but we do it all just it case. Notice that we don't call
                // pauseTimers(), because that for some reason causes the page to not load properly when recreating the
                // WebView later. We have observed this behavior, and it is also described at
                // https://stackoverflow.com/a/17458577.

                stopLoading()

                // Neutralize callbacks before any teardown
                webViewClient = WebViewClient()
                webChromeClient = null
                removeJavascriptInterface(JAVA_SCRIPT_INTERFACE_NAME)

                onPause()
                removeAllViews()
                destroy()
            }
        },
    )
}

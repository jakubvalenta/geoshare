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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import page.ooooo.geoshare.lib.network.WebViewNetworkException
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val JAVA_SCRIPT_INTERFACE_NAME = "Android"
private const val TAG = "ConversionWebView"

@OptIn(FlowPreview::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ConversionWebView(
    unsafeUrl: String,
    unsafeExtractionJavascript: String,
    pendingExtractionResult: CompletableDeferred<String>,
    extendWebSettings: (settings: WebSettings) -> Unit,
    shouldInterceptRequest: (requestUrlString: String) -> Boolean,
    // Set window size minus a common browser chrome size, so the numbers seem real, in case a web page checks
    sizePx: Size = Size(1080 - 2f, 1920f - 277f),
    extractionInterval: Duration = 1.seconds,
    settleTimeout: Duration = 3.seconds,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val size = with(density) { sizePx.toDpSize() }

    // As an extra layer of security, allow only specific URLs to be loaded in the WebView. These URLs should be more
    // strict than the patterns in Input (for example only HTTPS should be allowed) and they should not change often.
    val allowedUrlPatterns = listOf(
        // language=RegExp
        """^https://(?:www|maps)\.google(?:\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]\S+$""",
        // language=RegExp
        """^https://map\.baidu\.com[/?#]\S+$""",
        // language=RegExp
        """^https://www\.example\.com[/?#]\S+$""",
    )
    val safeUrl = remember(unsafeUrl) {
        allowedUrlPatterns.firstNotNullOfOrNull { pattern -> Regex(pattern).matchEntire(unsafeUrl)?.value }
    }
    val extractionResultFlow = remember(safeUrl) { MutableStateFlow<String?>(null) }

    /**
     * Stores the extraction result by completing the [pendingExtractionResult] deferred variable.
     *
     * It stores the result only after it hasn't changed in a while, because the first extraction often doesn't lead to
     * the final result. For example when extracting the page URL, the first URL is not the final one. It can take the
     * page JavaScript a few seconds to set the final page URL.
     */
    LaunchedEffect(extractionResultFlow) {
        extractionResultFlow
            .filterNotNull()
            .distinctUntilChanged()
            .debounce(settleTimeout)
            .collect { extractionResult ->
                Log.i(TAG, "Extraction settled at $extractionResult")
                pendingExtractionResult.complete(extractionResult)
            }
    }

    // Render a placeholder in Preview, because WebView is not supported there
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        Box(
            Modifier
                .requiredSize(size)
                .background(Color(0x80FFFFFF)),
            contentAlignment = Alignment.Center,
        ) {
            Text("WebView: $unsafeUrl")
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
                settings.javaScriptEnabled = true
                // Notice that we don't set custom user agent, because it makes Google Maps serve an error page. An
                // Input can set a user agent in extendWebSettings, if needed.
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
                        fun onExtractSuccess(extractionResult: String) {
                            Log.d(TAG, "Extracted $extractionResult")
                            extractionResultFlow.value = extractionResult
                        }

                        @Suppress("unused")
                        @JavascriptInterface
                        fun onExtractFailure() {
                            Log.w(TAG, "Extraction failed")
                            pendingExtractionResult.completeExceptionally(WebViewNetworkException())
                        }
                    },
                    JAVA_SCRIPT_INTERFACE_NAME,
                )

                webViewClient = object : WebViewClient() {
                    /**
                     * Runs extraction repeatedly every [extractionInterval]
                     */
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        view?.evaluateJavascript(
                            // language=JavaScript
                            """
                                (() => {
                                    const extract = $unsafeExtractionJavascript;
                                    function extractAndCallback() {
                                        try {
                                            switch (location.protocol) {
                                                case 'about:':
                                                    // Try again if the page is about:blank
                                                    break;
                                                case 'chrome-error:':
                                                    $JAVA_SCRIPT_INTERFACE_NAME.onExtractFailure();
                                                    break;
                                                default:
                                                    const extractionResult = extract();
                                                    if (extractionResult !== null && extractionResult !== undefined) {
                                                        $JAVA_SCRIPT_INTERFACE_NAME.onExtractSuccess(extractionResult);
                                                    }
                                            }
                                        } catch (ex) {
                                            console.error("Extraction exception", ex);
                                        }
                                    }
                                    extractAndCallback();
                                    window.setInterval(extractAndCallback, ${extractionInterval.inWholeMilliseconds});
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
                            // In development, you can log requests with Log.d(TAG, "Allowed request $requestUrlString")
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                if (safeUrl != null) {
                    loadUrl(safeUrl)
                }
            }
        },
        modifier = Modifier.requiredSize(size),
        update = { webView ->
            webView.apply {
                if (safeUrl != null && url != safeUrl) {
                    loadUrl(safeUrl)
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
                removeJavascriptInterface("Android")

                onPause()
                removeAllViews()
                destroy()
            }
        },
    )
}

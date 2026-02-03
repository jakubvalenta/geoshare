package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val TAG = "InvisibleWebView"

@OptIn(FlowPreview::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InvisibleWebView(
    url: String,
    onUrlChange: (urlString: String) -> Unit,
    shouldInterceptRequest: (requestUrlString: String) -> Boolean,
    urlChangeDebounceTimeout: Duration = 3.seconds,
) {
    val context = LocalContext.current

    val currentUrlStringFlow = remember(url) { MutableStateFlow<String?>(null) }

    // Call URL change callback if the URL hasn't changed for a while, because:
    // 1. First URL change is often not the final URL and we don't want to start parsing an intermediate URL.
    // 2. Quick URL changes cause ConversionState transition to crash, because each new transition cancels any running
    //    transition.
    LaunchedEffect(currentUrlStringFlow) {
        currentUrlStringFlow
            .filterNotNull()
            .distinctUntilChanged()
            .debounce(urlChangeDebounceTimeout)
            .collect { urlString ->
                Log.d(TAG, "URL hasn't changed in $urlChangeDebounceTimeout, calling callback")
                onUrlChange(urlString)
            }
    }

    AndroidView(
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(1080, 1920)
                setBackgroundColor(0x00000000) // Prevent a visible white rectangle before the URL loads

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(false)
                cookieManager.setAcceptThirdPartyCookies(this, false)

                settings.allowContentAccess = false
                settings.allowFileAccess = false
                settings.javaScriptEnabled = true

                // TODO Security
                addJavascriptInterface(
                    object {
                        @Suppress("unused")
                        @JavascriptInterface
                        fun onUrlChange(urlString: String) {
                            Log.d(TAG, "URL changed to $urlString")
                            currentUrlStringFlow.value = urlString
                        }
                    },
                    "Android",
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        view?.evaluateJavascript(
                            @Suppress("SpellCheckingInspection")
                            """
                                (function () {
                                    const origPushState = history.pushState;
                                    const origReplaceState = history.replaceState;

                                    history.pushState = function() {
                                        origPushState.apply(this, arguments);
                                        Android.onUrlChange(window.location.href);
                                    };

                                    history.replaceState = function() {
                                        origReplaceState.apply(this, arguments);
                                        Android.onUrlChange(window.location.href);
                                    };

                                    window.addEventListener('popstate', function() {
                                        Android.onUrlChange(window.location.href);
                                    });
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
                                Log.d(TAG, "Cancelled request to $requestUrlString")
                                return WebResourceResponse("text/plain", "utf-8", null)
                            }
                            Log.d(TAG, "Allowed request to $requestUrlString")
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }
            }
        },
        modifier = Modifier.offset((-3000).dp, (-3000).dp),
        update = { webView -> webView.loadUrl(url) },
        onReset = { webView ->
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
        },
    )
}

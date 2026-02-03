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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import page.ooooo.geoshare.BuildConfig

private const val TAG = "InvisibleWebView"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InvisibleWebView(
    url: String,
    onUrlChange: (urlString: String) -> Unit,
    shouldInterceptRequest: (requestUrlString: String) -> Boolean,
) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(1080, 1920)
                setBackgroundColor(0x00000000) // Prevent a visible white rectangle before the URL loads

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(false)
                cookieManager.setAcceptThirdPartyCookies(this, false)

                settings.javaScriptEnabled = true

                // TODO Security
                addJavascriptInterface(
                    object {
                        @Suppress("unused")
                        @JavascriptInterface
                        fun onUrlChange(urlString: String) {
                            Log.d(TAG, "URL changed to $urlString")
                            onUrlChange(urlString)
                            // TODO Two onUrlChange emissions in short sequence cancel transition
                            // TODO Fix first onUrlChange emission being user's coordinates and only the second one the real ones
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

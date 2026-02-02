package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InvisibleWebView(
    url: String,
    shouldInterceptRequest: ((String) -> Boolean)? = null,
) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(1080, 1920)

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = false

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(false)
                cookieManager.setAcceptThirdPartyCookies(this, false)

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): WebResourceResponse? {
                        request?.url?.toString()?.let { requestUrl ->
                            shouldInterceptRequest?.let { interceptor ->
                                if (!interceptor(requestUrl)) {
                                    // Return empty response to cancel the request
                                    return WebResourceResponse("text/plain", "utf-8", null)
                                }
                            }
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

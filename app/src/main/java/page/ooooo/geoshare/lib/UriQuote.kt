package page.ooooo.geoshare.lib

import android.net.Uri
import java.net.URLDecoder
import java.net.URLEncoder

interface UriQuote {
    fun encode(s: String): String
    fun encode(s: String, allow: String): String
    fun decode(s: String): String
}

class DefaultUriQuote : UriQuote {
    override fun encode(s: String): String = Uri.encode(s)
    override fun encode(s: String, allow: String): String = Uri.encode(s, allow)
    override fun decode(s: String): String = Uri.decode(s)
}

class FakeUriQuote : UriQuote {
    override fun encode(s: String): String = URLEncoder.encode(s, "utf-8").replace("+", "%20")

    override fun encode(s: String, allow: String): String = allow.fold(URLEncoder.encode(s, "utf-8")) { res, char ->
        res.replace(URLEncoder.encode(char.toString(), "utf-8"), char.toString())
    }.let {
        if ('+' in allow) {
            it
        } else {
            it.replace("+", "%20")
        }
    }

    override fun decode(s: String): String = URLDecoder.decode(s, "utf-8")
}

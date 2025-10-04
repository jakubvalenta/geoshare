package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.*
import java.net.URL

enum class ShortUriMethod { GET, HEAD }

sealed class SupportedInput(val addedInVersionCode: Int) {
    class Uri(val uriString: String, addedInVersionCode: Int) : SupportedInput(addedInVersionCode)
    class Url(val urlString: String, addedInVersionCode: Int) : SupportedInput(addedInVersionCode)
    class Text(val descriptionResId: Int, addedInVersionCode: Int) : SupportedInput(addedInVersionCode)
}

sealed interface UrlConverter {
    val nameResId: Int
    val uriPattern: Pattern
    val supportedInputs: List<SupportedInput>

    interface WithShortUriPattern : UrlConverter {
        val shortUriPattern: Pattern
        val shortUriMethod: ShortUriMethod get() = ShortUriMethod.HEAD
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface WithUriPattern : UrlConverter {
        val conversionUriPattern: ConversionUriPattern<PositionRegex>
    }

    interface WithHtmlPattern : UrlConverter {
        val conversionHtmlPattern: ConversionHtmlPattern<PositionRegex>? get() = null
        val conversionHtmlRedirectPattern: ConversionHtmlPattern<RedirectRegex>? get() = null
        fun getHtmlUrl(uri: Uri): URL? = uri.toUrl()
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }
}

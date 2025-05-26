package page.ooooo.geoshare.lib

import java.net.URL

fun getUrlQueryParams(url: URL, uriQuote: UriQuote): Map<String, String> =
    if (url.query.isNullOrEmpty()) {
        emptyMap()
    } else {
        url.query.split('&').associate { rawParam ->
            val paramParts = rawParam.split('=')
            val paramName = paramParts.firstOrNull() ?: ""
            val rawParamValue = paramParts.drop(1).firstOrNull() ?: ""
            val paramValue = uriQuote.decode(rawParamValue)
            paramName to paramValue
        }
    }

package page.ooooo.geoshare.lib

fun getUrlQueryParams(query: String?, uriQuote: UriQuote): Map<String, String> =
    if (query.isNullOrEmpty()) {
        emptyMap()
    } else {
        query.split('&').associate { rawParam ->
            val paramParts = rawParam.split('=')
            val paramName = paramParts.firstOrNull() ?: ""
            val rawParamValue = paramParts.drop(1).firstOrNull() ?: ""
            val paramValue = uriQuote.decode(rawParamValue)
            paramName to paramValue
        }
    }

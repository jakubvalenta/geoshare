package page.ooooo.geoshare.lib.uri

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

typealias QueryParams = ImmutableMap<String, ImmutableList<String>>

fun String.toQueryParams(uriQuote: UriQuote): QueryParams =
    if (isNullOrEmpty()) {
        persistentMapOf()
    } else {
        split('&')
            .map { rawParam ->
                val paramParts = rawParam.split('=')
                val rawParamName = paramParts.firstOrNull().orEmpty()
                val rawParamValue = paramParts.drop(1).firstOrNull().orEmpty()
                val paramName = uriQuote.decode(rawParamName)
                val paramValue = uriQuote.decode(rawParamValue)
                paramName to paramValue
            }
            .toQueryParams()
    }

fun List<Pair<String, String>>.toQueryParams(): QueryParams =
    buildMap {
        this@toQueryParams.forEach { (paramName, paramValue) ->
            val paramValues = getOrPut(paramName) { mutableListOf() }
            paramValues.add(paramValue)
        }
    }
        .mapValues { (_, paramValues) -> paramValues.toImmutableList() }
        .toImmutableMap()

fun QueryParams.format(allow: String = ",", uriQuote: UriQuote): String {
    val plusAllowed = '+' in allow
    return flatMap { (paramName, paramValues) ->
        paramValues.map { paramValue ->
            buildString {
                append(uriQuote.encode(paramName, allow = allow))
                if (paramValue.isNotEmpty()) {
                    append("=")
                    val cleanValue = if (plusAllowed) {
                        paramValue.replace(' ', '+')
                    } else {
                        paramValue.replace('+', ' ')
                    }
                    append(uriQuote.encode(cleanValue, allow = allow))
                }
            }
        }
    }.joinToString("&")
}

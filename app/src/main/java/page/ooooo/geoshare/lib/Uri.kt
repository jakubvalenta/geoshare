package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.*
import java.net.MalformedURLException
import java.net.URL

/**
 * Like android.net.Uri but correctly sets path for non-hierarchical URIs, so it can be used for geo: URIs.
 */
@Immutable
data class Uri(
    val scheme: String = "",
    val host: String = "",
    val pathParts: ImmutableList<String> = persistentListOf(),
    val queryParams: ImmutableMap<String, String> = persistentMapOf(),
    val fragment: String = "",
    val uriQuote: UriQuote = DefaultUriQuote(),
) {
    companion object {
        fun parse(uriString: String, uriQuote: UriQuote = DefaultUriQuote()): Uri {
            val schemeSepIndex = uriString.indexOf(':').takeIf { it > -1 }
            val hostSepIndex =
                schemeSepIndex?.takeIf { uriString.length > it + 2 && uriString[it + 1] == '/' && uriString[it + 2] == '/' }
                    ?.let { it + 2 }
            val hostStartIndex = hostSepIndex?.let { it + 1 } ?: schemeSepIndex?.let { it + 1 } ?: 0
            val fragmentSepIndex = uriString.indexOf('#', hostStartIndex).takeIf { it > -1 }
            val queryEndIndex = fragmentSepIndex ?: uriString.length
            val querySepIndex = uriString.indexOf('?', hostStartIndex).takeIf { it > -1 && it < queryEndIndex }
            val pathEndIndex = querySepIndex ?: fragmentSepIndex ?: uriString.length
            val pathSepIndex = uriString.indexOf('/', hostStartIndex).takeIf { it > -1 && it < pathEndIndex }
            val scheme = if (schemeSepIndex != null) {
                // geo:foo, :foo
                uriString.take(schemeSepIndex)
            } else {
                // geo
                ""
            }
            val host = if (hostSepIndex != null) {
                // https://foo, https://foo/bar
                uriString.substring(hostSepIndex + 1, pathSepIndex ?: pathEndIndex)
            } else if (pathSepIndex != null) {
                if (schemeSepIndex != null) {
                    // geo:foo/bar
                    uriString.substring(schemeSepIndex + 1, pathSepIndex)
                } else {
                    // foo/bar
                    uriString.take(pathSepIndex)
                }
            } else {
                // geo:foo
                ""
            }
            val path = if (pathSepIndex != null) {
                // https://foo/bar, geo:foo/bar, foo/bar
                uriString.substring(pathSepIndex, pathEndIndex)
            } else if (hostSepIndex == null) {
                // geo:foo
                if (schemeSepIndex != null) {
                    uriString.substring(schemeSepIndex + 1, pathEndIndex)
                } else {
                    uriString.take(pathEndIndex)
                }
            } else {
                // https://foo
                ""
            }
            val query = if (querySepIndex != null) uriString.substring(querySepIndex + 1, queryEndIndex) else ""
            val fragment = if (fragmentSepIndex != null) uriString.substring(fragmentSepIndex + 1) else ""
            return Uri(
                scheme = scheme,
                host = host,
                pathParts = if (path != "") {
                    path.split('/').map { uriQuote.decode(it) }.toImmutableList()
                } else {
                    persistentListOf()
                },
                queryParams = parseQueryParams(query, uriQuote),
                fragment = uriQuote.decode(fragment),
                uriQuote = uriQuote,
            )
        }

        private fun parseQueryParams(query: String?, uriQuote: UriQuote): ImmutableMap<String, String> =
            if (query.isNullOrEmpty()) {
                persistentMapOf()
            } else {
                query.split('&').associate { rawParam ->
                    val paramParts = rawParam.split('=')
                    val paramName = paramParts.firstOrNull() ?: ""
                    val rawParamValue = paramParts.drop(1).firstOrNull() ?: ""
                    val paramValue = uriQuote.decode(rawParamValue)
                    paramName to paramValue
                }.toImmutableMap()
            }

        fun formatQueryParams(
            queryParams: ImmutableMap<String, String>,
            allow: String = ",",
            uriQuote: UriQuote,
        ): String {
            val plusAllowed = '+' in allow
            return queryParams.map {
                buildString {
                    append(it.key)
                    if (it.value.isNotEmpty()) {
                        append("=")
                        val cleanValue = if (plusAllowed) {
                            it.value.replace(' ', '+')
                        } else {
                            it.value.replace('+', ' ')
                        }
                        append(uriQuote.encode(cleanValue, allow = allow))
                    }
                }
            }.joinToString("&")
        }

        fun formatPath(path: String, allow: String = "!&+,/=@", uriQuote: UriQuote): String =
            uriQuote.encode(path, allow = allow)
    }

    constructor(
        scheme: String = "",
        host: String = "",
        path: String,
        queryParams: ImmutableMap<String, String> = persistentMapOf(),
        fragment: String = "",
        uriQuote: UriQuote = DefaultUriQuote(),
    ) : this(
        scheme = scheme,
        host = host,
        pathParts = path.split('/').map { uriQuote.decode(it) }.toImmutableList(),
        queryParams = queryParams,
        fragment = fragment,
        uriQuote = uriQuote,
    )

    val path: String get() = pathParts.joinToString("/")

    fun copy(
        scheme: String? = null,
        host: String? = null,
        path: String? = null,
        queryParams: ImmutableMap<String, String>? = null,
        fragment: String? = null,
        uriQuote: UriQuote? = null,
    ) = Uri(
        scheme = scheme ?: this.scheme,
        host = host ?: this.host,
        path = path ?: this.path,
        queryParams = queryParams ?: this.queryParams,
        fragment = fragment ?: this.fragment,
        uriQuote = uriQuote ?: this.uriQuote,
    )

    fun toAbsoluteUri(baseUri: Uri): Uri = if (host.isEmpty()) {
        if (path.startsWith("//")) {
            // Protocol-relative URL
            this.copy(scheme = baseUri.scheme)
        } else if (path.startsWith("/")) {
            // Absolute URL
            this.copy(scheme = baseUri.scheme, host = baseUri.host)
        } else {
            // Relative URL with only one part
            this.copy(scheme = baseUri.scheme, host = baseUri.host, path = "${baseUri.path}/$path")
        }
    } else if (scheme.isEmpty()) {
        // Relative URL with multiple parts
        this.copy(scheme = baseUri.scheme, host = baseUri.host, path = "${baseUri.path}/$host$path")
    } else {
        this
    }

    fun toUrl(): URL? = try {
        URL(
            if (host.isEmpty()) {
                path.trimStart('/').let { path ->
                    if (path.isEmpty()) {
                        throw MalformedURLException("Missing host or path")
                    }
                    this.copy(scheme = scheme.ifEmpty { "https" }, host = path, path = "")
                }
            } else {
                this.copy(scheme = scheme.ifEmpty { "https" })
            }.toString()
        )
    } catch (_: MalformedURLException) {
        null
    }

    override fun toString() = StringBuilder().apply {
        if (scheme.isNotEmpty()) {
            append("$scheme:")
            if (host.isNotEmpty()) {
                append("//")
            }
        }
        if (host.isNotEmpty()) {
            append(host)
        }
        append(formatPath(path, uriQuote = uriQuote))
        if (queryParams.isNotEmpty()) {
            append("?${formatQueryParams(queryParams, uriQuote = uriQuote)}")
        }
        if (fragment.isNotEmpty()) {
            append("#$fragment")
        }
    }.toString()
}

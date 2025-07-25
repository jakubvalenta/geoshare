package page.ooooo.geoshare.lib

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.jvm.Throws

class UnexpectedResponseCodeException : Exception("Unexpected response code")

class NetworkTools(private val log: ILog = DefaultLog()) {

    @Throws(
        IOException::class,
        MalformedURLException::class,
        SocketTimeoutException::class,
        UnexpectedResponseCodeException::class,
    )
    suspend fun requestLocationHeader(url: URL): URL =
        withContext(Dispatchers.IO) {
            connect(
                url,
                method = "HEAD",
                followRedirects = false,
                expectedResponseCodes = listOf(HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP),
            ) { connection ->
                val locationUrlString: String? = connection.getHeaderField("Location")
                val locationUrl = try {
                    URL(locationUrlString)
                } catch (e: MalformedURLException) {
                    log.w(null, "Invalid location URL $locationUrlString")
                    throw e
                }
                log.i(null, "Resolved short URL $url to $locationUrlString")
                locationUrl
            }
        }

    @Throws(
        IOException::class,
        SocketTimeoutException::class,
        UnexpectedResponseCodeException::class,
    )
    suspend fun getText(url: URL): String =
        withContext(Dispatchers.IO) {
            connect(url) { connection ->
                connection.getInputStream().reader().use { it.readText() }
            }
        }

    private fun <T> connect(
        url: URL,
        method: String = "GET",
        expectedResponseCodes: List<Int> = listOf(HttpURLConnection.HTTP_OK),
        followRedirects: Boolean = true,
        connectTimeout: Int = 15_000,
        readTimeout: Int = 30_000,
        // Set custom User-Agent, so that we don't receive Google Lite HTML,
        // which doesn't contain coordinates in case of Google Maps or maps link
        // in case of Google Search.
        userAgent: String =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36",
        block: (connection: HttpURLConnection) -> T,
    ): T {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.instanceFollowRedirects = followRedirects
        connection.connectTimeout = connectTimeout
        connection.readTimeout = readTimeout
        connection.setRequestProperty("User-Agent", userAgent)
        try {
            connection.connect()
            val responseCode = connection.responseCode
            if (expectedResponseCodes.contains(responseCode)) {
                return block(connection)
            }
            log.w(null, "Received HTTP code $responseCode for $url")
            throw UnexpectedResponseCodeException()
        } catch (e: SocketTimeoutException) {
            log.w(null, "Connection timeout for $url")
            throw e
        } catch (e: IOException) {
            log.w(null, "Read timeout for $url")
            throw e
        } finally {
            connection.disconnect()
        }
    }
}

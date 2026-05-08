package page.ooooo.geoshare.lib.network

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.CoroutineDispatcher
import java.net.URL

open class MockNetworkTools : NetworkTools() {
    override suspend fun httpHeadLocationHeader(
        url: URL,
        lastAttempt: Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = onRequestLocationHeader(url)

    open fun onRequestLocationHeader(url: URL): String {
        throw NotImplementedError()
    }

    override suspend fun httpGetRedirectedUrlString(
        url: URL,
        lastAttempt: Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = onGetRedirectUrlString(url)

    open fun onGetRedirectUrlString(url: URL): String {
        throw NotImplementedError()
    }

    override suspend fun <T> httpGetBodyAsByteReadChannel(
        url: URL,
        lastAttempt: Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
        block: suspend (source: ByteReadChannel) -> T,
    ): T = block(onHttpGetBodyAsByteReadChannel(url).byteInputStream().toByteReadChannel())

    open fun onHttpGetBodyAsByteReadChannel(url: URL): String {
        throw NotImplementedError()
    }

    override suspend fun httpGetBodyAsText(
        url: URL,
        lastAttempt: Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = onHttpGetBodyAsText(url)

    open fun onHttpGetBodyAsText(url: URL): String {
        throw NotImplementedError()
    }
}

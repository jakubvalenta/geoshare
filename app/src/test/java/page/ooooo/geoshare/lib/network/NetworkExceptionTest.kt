package page.ooooo.geoshare.lib.network

import android.content.res.Resources
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R

class NetworkExceptionTest {
    private val resources: Resources = mock {
        on { getString(R.string.network_exception_too_many_requests) } doReturn "Too many requests"
        on {
            getString(R.string.network_exception_too_many_requests_wait, 120)
        } doReturn "Too many requests, wait for 120s"
    }

    @Test
    fun tooManyRequestsNetworkException_getMessage_whenHeadersContainRetryAfter_returnsMessageWithWaitSeconds() {
        val response: HttpResponse = mock {
            on { headers } doReturn headersOf(HttpHeaders.RetryAfter to listOf("120"))
        }
        val ex = TooManyRequestsNetworkException(response, Throwable())
        assertEquals("Too many requests, wait for 120s", ex.getMessage(resources))
    }

    @Test
    fun tooManyRequestsNetworkException_getMessage_whenHeadersDoNotContainRetryAfter_returnsMessageWithoutWaitSeconds() {
        val response: HttpResponse = mock {
            on { headers } doReturn headersOf()
        }
        val ex = TooManyRequestsNetworkException(response, Throwable())
        assertEquals("Too many requests", ex.getMessage(resources))
    }

    @Test
    fun tooManyRequestsNetworkException_getMessage_whenHeadersContainInvalidRetryAfter_returnsMessageWithoutWaitSeconds() {
        val response: HttpResponse = mock {
            on { headers } doReturn headersOf(HttpHeaders.RetryAfter to listOf("spam"))
        }
        val ex = TooManyRequestsNetworkException(response, Throwable())
        assertEquals("Too many requests", ex.getMessage(resources))
    }
}

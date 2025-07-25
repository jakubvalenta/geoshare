package page.ooooo.geoshare

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.UnexpectedResponseCodeException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL

class NetworkToolsTest {

    private lateinit var networkTools: NetworkTools

    @Before
    fun before() {
        networkTools = NetworkTools(FakeLog())
    }

    @Test
    fun requestLocationHeader_301Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_PERM)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("https://maps.apple.com/place?address=Thomash%C3%B6he%2C+12053+Berlin%2C+Germany&coordinate=52.4737758%2C13.4373898")
        assertEquals(
            URL("https://maps.apple.com/place?address=Thomash%C3%B6he%2C+12053+Berlin%2C+Germany&coordinate=52.4737758%2C13.4373898"),
            networkTools.requestLocationHeader(mockUrl)
        )
    }

    @Test
    fun requestLocationHeader_302Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_TEMP)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")
        assertEquals(
            URL("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"),
            networkTools.requestLocationHeader(mockUrl)
        )
    }

    @Test
    fun requestLocationHeader_302ResponseMissingLocation() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_TEMP)
        var threw = false
        try {
            networkTools.requestLocationHeader(mockUrl)
        } catch (_: MalformedURLException) {
            threw = true
        }
        assertTrue(threw)
        Mockito.verify(mockConnection).setRequestMethod("HEAD")
        Mockito.verify(mockConnection).setInstanceFollowRedirects(false)
    }

    @Test
    fun requestLocationHeader_200Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_OK)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")
        var threw = false
        try {
            networkTools.requestLocationHeader(mockUrl)
        } catch (_: UnexpectedResponseCodeException) {
            threw = true
        }
        assertTrue(threw)
        Mockito.verify(mockConnection).setRequestMethod("HEAD")
        Mockito.verify(mockConnection).setInstanceFollowRedirects(false)
    }

    @Test
    fun requestLocationHeader_500Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")
        var threw = false
        try {
            networkTools.requestLocationHeader(mockUrl)
        } catch (_: UnexpectedResponseCodeException) {
            threw = true
        }
        assertTrue(threw)
        Mockito.verify(mockConnection).setRequestMethod("HEAD")
        Mockito.verify(mockConnection).setInstanceFollowRedirects(false)
    }

    @Test
    fun requestLocationHeader_invalidLocationUrl() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_TEMP)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("spam")
        var threw = false
        try {
            networkTools.requestLocationHeader(mockUrl)
        } catch (_: MalformedURLException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_ioException() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.connect())
            .thenThrow(IOException::class.java)
        var threw = false
        try {
            networkTools.requestLocationHeader(mockUrl)
        } catch (_: SocketTimeoutException) {
            // This empty SocketTimeoutException catch block prevents the
            // IOException catch block from catching SocketTimeoutException too.
        } catch (_: IOException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_socketTimeoutException() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.connect())
            .thenThrow(SocketTimeoutException::class.java)
        var threw = false
        try {
            networkTools.requestLocationHeader(mockUrl)
        } catch (_: SocketTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun getText_200Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_OK)
        Mockito.`when`(mockConnection.getInputStream())
            .thenReturn("test stream content".byteInputStream())
        assertEquals(
            "test stream content",
            networkTools.getText(mockUrl),
        )
        Mockito.verify(mockConnection).setRequestMethod("GET")
        Mockito.verify(mockConnection).setInstanceFollowRedirects(true)
    }

    @Test
    fun getText_500Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR)
        Mockito.`when`(mockConnection.getInputStream())
            .thenReturn("test stream content".byteInputStream())
        var threw = false
        try {
            networkTools.getText(mockUrl)
        } catch (_: UnexpectedResponseCodeException) {
            threw = true
        }
        assertTrue(threw)
        Mockito.verify(mockConnection).setRequestMethod("GET")
        Mockito.verify(mockConnection).setInstanceFollowRedirects(true)
    }

    @Test
    fun getText_ioException() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.connect())
            .thenThrow(IOException::class.java)
        var threw = false
        try {
            networkTools.getText(mockUrl)
        } catch (_: SocketTimeoutException) {
            // This empty SocketTimeoutException catch block prevents the
            // IOException catch block from catching SocketTimeoutException too.
        } catch (_: IOException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun getText_socketTimeoutException() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.connect())
            .thenThrow(SocketTimeoutException::class.java)
        var threw = false
        try {
            networkTools.getText(mockUrl)
        } catch (_: SocketTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }
}

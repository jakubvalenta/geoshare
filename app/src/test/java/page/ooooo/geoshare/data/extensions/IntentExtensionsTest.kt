package page.ooooo.geoshare.data.extensions

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.android.getUriString
import page.ooooo.geoshare.data.log.FakeLog

class IntentExtensionsTest {
    private val log = FakeLog

    @Test
    fun getUriString_whenActionIsViewAndDataIsNotNull_returnsDataAsString() {
        val uri: Uri = mock {
            on { toString() } doReturn "test uri"
        }
        val intent: Intent = mock {
            on { action } doReturn Intent.ACTION_VIEW
            on { data } doReturn uri
        }
        assertEquals("test uri", intent.getUriString(log))
    }

    @Test
    fun getUriString_whenActionIsViewAndDataIsNull_returnsNull() {
        val intent: Intent = mock {
            on { action } doReturn Intent.ACTION_VIEW
            on { data } doReturn null
        }
        assertNull(intent.getUriString(log))
    }

    @Test
    fun getUriString_whenActionIsSendAndExtraIsNotNull_returnsExtra() {
        val intent: Intent = mock {
            on { action } doReturn Intent.ACTION_SEND
            on { getStringExtra(Intent.EXTRA_TEXT) } doReturn "test EXTRA_TEXT"
        }
        assertEquals("test EXTRA_TEXT", intent.getUriString(log))
    }

    @Test
    fun getUriString_whenActionIsSendAndDataIsNull_returnsNull() {
        val intent: Intent = mock {
            on { action } doReturn Intent.ACTION_SEND
            on { getStringExtra(Intent.EXTRA_TEXT) } doReturn null
        }
        assertNull(intent.getUriString(log))
    }

    @Test
    fun getUriString_whenActionIsProcessTextAndExtraIsNotNull_returnsExtra() {
        val intent: Intent = mock {
            on { action } doReturn Intent.ACTION_PROCESS_TEXT
            on { getStringExtra(Intent.EXTRA_PROCESS_TEXT) } doReturn "test EXTRA_PROCESS_TEXT"
        }
        assertEquals("test EXTRA_PROCESS_TEXT", intent.getUriString(log))
    }

    @Test
    fun getUriString_whenActionIsProcessTextAndDataIsNull_returnsNull() {
        val intent: Intent = mock {
            on { action } doReturn Intent.ACTION_PROCESS_TEXT
            on { getStringExtra(Intent.EXTRA_TEXT) } doReturn null
        }
        assertNull(intent.getUriString(log))
    }

    @Test
    fun getUriString_whenActionIsUnsupported_returnsNull() {
        val intent: Intent = mock {
            on { action } doReturn Intent.ACTION_SENDTO
        }
        assertNull(intent.getUriString(log))
    }
}

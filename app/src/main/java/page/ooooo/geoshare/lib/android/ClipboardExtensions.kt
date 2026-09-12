package page.ooooo.geoshare.lib.android

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

suspend fun Clipboard.copy(text: String) =
    setClipEntry(ClipEntry(ClipData.newPlainText("Geographic coordinates", text)))

suspend fun Clipboard.paste(): String =
    getClipEntry()?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString().orEmpty()

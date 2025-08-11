package page.ooooo.geoshare

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.ClipboardTools

@AndroidEntryPoint
class CopyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val clipboard = LocalClipboard.current
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                ClipboardTools().setPlainText(clipboard, "geo: URI", intent.data?.toString() ?: "")
                val systemHasClipboardEditor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                if (!systemHasClipboardEditor) {
                    Toast.makeText(context, R.string.copying_finished, Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }
}

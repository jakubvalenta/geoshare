package page.ooooo.geoshare

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.ConversionFailed
import page.ooooo.geoshare.lib.ConversionSucceeded
import page.ooooo.geoshare.lib.CopyingFinished
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class CopyActivity : ComponentActivity() {

    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val clipboard = LocalClipboard.current
            val currentState by viewModel.currentState.collectAsStateWithLifecycle()
            val message by viewModel.message.collectAsStateWithLifecycle()

            AppTheme {
                ConversionScreen(viewModel = viewModel)
            }

            LaunchedEffect(intent) {
                viewModel.start(intent)
            }

            LaunchedEffect(message) {
                if (message != null) {
                    (message as Message).let {
                        Toast.makeText(
                            context,
                            it.resId,
                            if (it.type == Message.Type.SUCCESS) {
                                Toast.LENGTH_SHORT
                            } else {
                                Toast.LENGTH_LONG
                            },
                        ).show()
                    }
                    viewModel.dismissMessage()
                }
            }

            when (currentState) {
                is ConversionSucceeded -> {
                    viewModel.copy(clipboard)
                }

                is ConversionFailed -> {
                    finish()
                }

                is CopyingFinished -> {
                    // TODO Sometimes the Android Clipboard Editor doesn't appear. Possibly because we call finish() too fast.
                    finish()
                }
            }
        }
    }
}

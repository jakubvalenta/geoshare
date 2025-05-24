package page.ooooo.geoshare

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.ConversionFailed
import page.ooooo.geoshare.lib.ConversionSucceeded
import page.ooooo.geoshare.lib.ManagedActivityResultLauncherWrapper
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.SharingFailed
import page.ooooo.geoshare.lib.SharingSucceeded
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val currentState by viewModel.currentState.collectAsStateWithLifecycle()
            val message by viewModel.message.collectAsStateWithLifecycle()
            val settingsLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
            ) {
                viewModel.grant(doNotAsk = false)
            }

            AppTheme {
                ShareScreen(viewModel)
            }

            LaunchedEffect(intent) {
                viewModel.start(intent)
            }

            LaunchedEffect(message) {
                if (message != null) {
                    if (currentState !is SharingSucceeded) {
                        (message as Message).let {
                            Toast.makeText(
                                context,
                                context.resources.getString(it.resId, *it.formatArgs.toTypedArray()),
                                if (it.type == Message.Type.SUCCESS) {
                                    Toast.LENGTH_SHORT
                                } else {
                                    Toast.LENGTH_LONG
                                },
                            ).show()
                        }
                    }
                    viewModel.dismissMessage()
                }
            }

            when (currentState) {
                is ConversionSucceeded -> {
                    viewModel.share(
                        context,
                        ManagedActivityResultLauncherWrapper(settingsLauncher),
                    )
                }

                is ConversionFailed -> {
                    finish()
                }

                is SharingSucceeded -> {
                    finish()
                }

                is SharingFailed -> {
                    finish()
                }
            }
        }
    }
}

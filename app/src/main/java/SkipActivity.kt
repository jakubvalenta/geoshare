package page.ooooo.geoshare

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.IntentTools

@AndroidEntryPoint
class SkipActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IntentTools().getIntentOriginalUri(intent)?.let { originalUri ->
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = originalUri
                }
            )
            finish()
        }
    }
}

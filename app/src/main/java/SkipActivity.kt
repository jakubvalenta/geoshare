package page.ooooo.geoshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.lib.IntentTools

@AndroidEntryPoint
class SkipActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IntentTools().view(this, intent?.data)
        finish()
    }
}

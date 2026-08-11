package page.ooooo.geoshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.ui.BillingViewModel
import page.ooooo.geoshare.ui.MainNavigation
import page.ooooo.geoshare.ui.UserPreferenceViewModel
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val billingViewModel: BillingViewModel by viewModels()
    private val userPreferenceViewModel: UserPreferenceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPreferencesValues by userPreferenceViewModel.values.collectAsStateWithLifecycle()

            // Notice that the UI can flicker when starting the app, because we load the dynamic color preference
            // asynchronously, and then recompose the theme if the preference is on. We could cover that with a splash
            // screen, but it feels as an overkill, because we expect few users to turn the dynamic color preference on.
            AppTheme(dynamicColor = userPreferencesValues.dynamicColor) {
                MainNavigation(billingViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        billingViewModel.onResume(this)
    }
}

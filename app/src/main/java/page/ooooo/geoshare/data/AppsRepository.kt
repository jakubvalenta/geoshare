package page.ooooo.geoshare.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import page.ooooo.geoshare.data.di.ApplicationScope
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.Apps
import page.ooooo.geoshare.lib.android.queryAppDetails
import page.ooooo.geoshare.lib.android.queryApps
import javax.inject.Inject

class AppsRepository @Inject constructor(
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:ApplicationContext private val context: Context,
) {
    val apps: SharedFlow<Apps> =
        flow { emit(context.packageManager.queryApps()) }
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    val appDetails: SharedFlow<AppDetails> =
        apps.map { apps -> context.packageManager.queryAppDetails(apps) }
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)
}

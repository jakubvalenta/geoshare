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
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataTypes
import javax.inject.Inject

class AppsRepository @Inject constructor(
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:ApplicationContext private val context: Context,
) {
    val apps: SharedFlow<DataTypes> =
        flow { emit(AndroidTools.queryApps(context.packageManager)) }
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    val appDetails: SharedFlow<AppDetails> =
        apps.map { apps -> AndroidTools.queryAppDetails(context.packageManager, apps) }
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)
}

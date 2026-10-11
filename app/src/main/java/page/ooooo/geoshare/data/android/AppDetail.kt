package page.ooooo.geoshare.data.android

import android.graphics.drawable.Drawable

data class AppDetail(
    val packageName: String,
    val label: String,
    val icon: Drawable,
)

typealias AppDetails = Map<String, AppDetail?>

package page.ooooo.geoshare.lib.android

import androidx.compose.runtime.Immutable

@Immutable
data class App(val packageName: String, val dataTypes: Set<DataType>)

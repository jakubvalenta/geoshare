package page.ooooo.geoshare.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import kotlin.reflect.KProperty

class SavableDelegate<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    default: T,
) {
    private var state = mutableStateOf(savedStateHandle[key] ?: default)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return state.value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        savedStateHandle[key] = value
        state.value = value
    }
}

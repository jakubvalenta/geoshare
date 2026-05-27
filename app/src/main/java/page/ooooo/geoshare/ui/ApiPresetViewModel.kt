package page.ooooo.geoshare.ui

import android.content.res.Resources
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.ApiPresetRepository
import page.ooooo.geoshare.data.local.database.ApiPreset
import page.ooooo.geoshare.lib.Message
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class ApiPresetViewModel @Inject constructor(
    private val apiPresetRepository: ApiPresetRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val all: StateFlow<List<ApiPreset>> = apiPresetRepository.all
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
    val selected: StateFlow<ApiPreset?> = apiPresetRepository.selected
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null,
        )

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message

    /**
     * Dummy object to read default form values from.
     */
    private val default = ApiPreset()

    /**
     * Controls whether the list, insert, or update screen is displayed, so that the UI state survives process death.
     *
     * - null: list screen
     * - -1: insert screen
     * - other number: update screen for this object uid
     */
    var destination by savedStateHandle.saveable { mutableStateOf<Int?>(null) }

    /**
     * Navigate to the list, insert, or update screen; and reset or prefill the form.
     */
    suspend fun navigateTo(destination: Int?) {
        Log.d(TAG, "navigateTo($destination)")
        if (this.destination == destination) {
            // Do nothing, so that we don't overwrite values restored after process death for no reason
        } else if (destination == null || destination == -1) {
            withMutableSnapshot {
                this.destination = destination
                this.baseUrl = default.baseUrl
                this.authType = default.authType
                this.apiKey = default.apiKey
                this.apiKeyHeader = default.apiKeyHeader
            }
        } else {
            val item = apiPresetRepository.getByUid(destination)
            if (item != null) {
                withMutableSnapshot {
                    this.destination = destination
                    this.baseUrl = item.baseUrl
                    this.authType = item.authType
                    this.apiKey = item.apiKey
                    this.apiKeyHeader = item.apiKeyHeader
                }
            }
        }
    }

    // Form

    var baseUrl by savedStateHandle.saveable { mutableStateOf(default.baseUrl) }
    var authType by savedStateHandle.saveable { mutableStateOf(default.authType) }
    var apiKey by savedStateHandle.saveable { mutableStateOf(default.apiKey) }
    var apiKeyHeader by savedStateHandle.saveable { mutableStateOf(default.apiKeyHeader) }

    fun saveForm(resources: Resources) {
        destination?.let { destination ->
            if (destination == -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    apiPresetRepository.insert(
                        ApiPreset(
                            baseUrl = baseUrl,
                            authType = authType,
                            apiKey = apiKey,
                            apiKeyHeader = apiKeyHeader,
                        )
                    )
                    _message.value = Message(resources.getString(R.string.api_presets_message_inserted))
                    // Navigate after saving, because we reset form fields during navigation
                    navigateTo(null)
                }
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    val item = apiPresetRepository.getByUid(destination)
                    if (item != null) {
                        apiPresetRepository.update(
                            item.copy(
                                baseUrl = baseUrl,
                                authType = authType,
                                apiKey = apiKey,
                                apiKeyHeader = apiKeyHeader,
                            )
                        )
                        _message.value = Message(resources.getString(R.string.api_presets_message_updated))
                        // Navigate after saving, because we reset form fields during navigation
                        navigateTo(null)
                    }
                }
            }
        }
    }

    // Methods

    fun delete(resources: Resources) {
        destination?.let { destination ->
            if (destination != -1) {
                val item = all.value.firstOrNull { it.uid == destination }
                if (item != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        apiPresetRepository.delete(item)
                        _message.value = Message(resources.getString(R.string.api_presets_message_deleted))
                        navigateTo(null)
                    }
                }
            }
        }
    }

    fun select(uid: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            apiPresetRepository.select(uid)
        }
    }

    fun restoreInitialData(resources: Resources) {
        viewModelScope.launch(Dispatchers.IO) {
            apiPresetRepository.restoreInitialData()
            _message.value = Message(resources.getString(R.string.api_presets_message_factory_reset))
        }
    }

    fun dismissMessage() {
        _message.value = null
    }

    private companion object {
        private const val TAG = "ApiPresetViewModel"
    }
}

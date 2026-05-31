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
import page.ooooo.geoshare.data.ServerRepository
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.lib.Message
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class ServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val all: StateFlow<List<Server>> = serverRepository.all
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message

    /**
     * Dummy object to read default form values from.
     */
    private val default = Server()

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
                this.name = default.name
                this.urlTemplate = default.urlTemplate
                this.authType = default.authType
                this.apiKey = default.apiKey
                this.apiKeyHeader = default.apiKeyHeader
                this.challengeUrl = default.challengeUrl
                this.loginUrl = default.loginUrl
                this.registerUrl = default.registerUrl
            }
        } else {
            val item = serverRepository.getByUid(destination)
            if (item != null) {
                withMutableSnapshot {
                    this.destination = destination
                    this.name = item.name
                    this.urlTemplate = item.urlTemplate
                    this.authType = item.authType
                    this.apiKey = item.apiKey
                    this.apiKeyHeader = item.apiKeyHeader
                    this.challengeUrl = item.challengeUrl
                    this.loginUrl = item.loginUrl
                    this.registerUrl = item.registerUrl
                }
            }
        }
    }

    // Form

    var name by savedStateHandle.saveable { mutableStateOf(default.name) }
    var urlTemplate by savedStateHandle.saveable { mutableStateOf(default.urlTemplate) }
    var authType by savedStateHandle.saveable { mutableStateOf(default.authType) }
    var apiKey by savedStateHandle.saveable { mutableStateOf(default.apiKey) }
    var apiKeyHeader by savedStateHandle.saveable { mutableStateOf(default.apiKeyHeader) }
    var challengeUrl by savedStateHandle.saveable { mutableStateOf(default.challengeUrl) }
    var loginUrl by savedStateHandle.saveable { mutableStateOf(default.loginUrl) }
    var registerUrl by savedStateHandle.saveable { mutableStateOf(default.registerUrl) }

    fun saveForm(resources: Resources) {
        destination?.let { destination ->
            if (destination == -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    serverRepository.insert(
                        Server(
                            name = name,
                            urlTemplate = urlTemplate,
                            authType = authType,
                            apiKey = apiKey,
                            apiKeyHeader = apiKeyHeader,
                            challengeUrl = challengeUrl,
                            loginUrl = loginUrl,
                            registerUrl = registerUrl,
                        )
                    )
                    _message.value = Message(resources.getString(R.string.server_message_inserted))
                    // Navigate after saving, because we reset form fields during navigation
                    navigateTo(null)
                }
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    val item = serverRepository.getByUid(destination)
                    if (item != null) {
                        serverRepository.update(
                            item.copy(
                                name = name,
                                urlTemplate = urlTemplate,
                                authType = authType,
                                apiKey = apiKey,
                                apiKeyHeader = apiKeyHeader,
                                challengeUrl = challengeUrl,
                                loginUrl = loginUrl,
                                registerUrl = registerUrl,
                            )
                        )
                        _message.value = Message(resources.getString(R.string.server_message_updated))
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
                        serverRepository.delete(item)
                        _message.value = Message(resources.getString(R.string.server_message_deleted))
                        navigateTo(null)
                    }
                }
            }
        }
    }

    fun selectGoogleMaps(uid: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.unselectAllGoogleMapsAndSelect(uid)
        }
    }

    fun selectSearch(uid: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.unselectAllSearchAndSelect(uid)
        }
    }

    fun restoreInitialData(resources: Resources) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.restoreInitialData()
            _message.value = Message(resources.getString(R.string.server_message_factory_reset))
        }
    }

    fun dismissMessage() {
        _message.value = null
    }

    private companion object {
        private const val TAG = "ServerViewModel"
    }
}

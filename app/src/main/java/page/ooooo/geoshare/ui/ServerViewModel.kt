package page.ooooo.geoshare.ui

import android.content.res.Resources
import android.util.Log
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.ServerRepository
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
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
    val selectedServerGoogleMapsAddress: StateFlow<Server?> = serverRepository.selectedGoogleMapsAddress
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null,
        )
    val selectedServerGoogleMapsPlace: StateFlow<Server?> = serverRepository.selectedGoogleMapsPlace
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null,
        )
    val selectedServerSearch: StateFlow<Server?> = serverRepository.selectedSearch
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null,
        )

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message.asStateFlow()

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
    private val _destination = savedStateHandle.getMutableStateFlow<Int?>("serverDestination", null)
    val destination = _destination.asStateFlow()

    /**
     * Navigate to the list, insert, or update screen; and reset or prefill the form.
     */
    suspend fun navigateTo(destination: Int?) {
        Log.d(TAG, "navigateTo($destination)")
        if (_destination.value == destination) {
            // Do nothing, so that we don't overwrite values restored after process death for no reason
        } else if (destination == null || destination == -1) {
            withMutableSnapshot {
                _destination.value = destination
                _name.value = default.name
                _urlTemplate.value = default.urlTemplate
                _authType.value = default.authType
                _apiKey.value = default.apiKey
                _apiKeyHeader.value = default.apiKeyHeader
                _challengeUrl.value = default.challengeUrl
                _loginUrl.value = default.loginUrl
                _registerUrl.value = default.registerUrl
            }
        } else {
            val item = serverRepository.getByUid(destination)
            if (item != null) {
                withMutableSnapshot {
                    _destination.value = destination
                    _name.value = item.name
                    _urlTemplate.value = item.urlTemplate
                    _authType.value = item.authType
                    _apiKey.value = item.apiKey
                    _apiKeyHeader.value = item.apiKeyHeader
                    _challengeUrl.value = item.challengeUrl
                    _loginUrl.value = item.loginUrl
                    _registerUrl.value = item.registerUrl
                }
            }
        }
    }

    // Form

    private val _name = savedStateHandle.getMutableStateFlow("serverName", default.name)
    val name: StateFlow<String> = _name.asStateFlow()
    private val _urlTemplate = savedStateHandle.getMutableStateFlow("serverUrlTemplate", default.urlTemplate)
    val urlTemplate: StateFlow<String> = _urlTemplate.asStateFlow()
    private val _authType = savedStateHandle.getMutableStateFlow("serverAuthType", default.authType)
    val authType: StateFlow<ServerAuthType> = _authType.asStateFlow()
    private val _apiKey = savedStateHandle.getMutableStateFlow("serverApiKey", default.apiKey)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    private val _apiKeyHeader = savedStateHandle.getMutableStateFlow("serverApiKeyHeader", default.apiKeyHeader)
    val apiKeyHeader: StateFlow<String> = _apiKeyHeader.asStateFlow()
    private val _challengeUrl = savedStateHandle.getMutableStateFlow("serverChallengeUrl", default.challengeUrl)
    val challengeUrl: StateFlow<String> = _challengeUrl.asStateFlow()
    private val _loginUrl = savedStateHandle.getMutableStateFlow("serverLoginUrl", default.loginUrl)
    val loginUrl: StateFlow<String> = _loginUrl.asStateFlow()
    private val _registerUrl = savedStateHandle.getMutableStateFlow("serverRegisterUrl", default.registerUrl)
    val registerUrl: StateFlow<String> = _registerUrl.asStateFlow()

    fun saveForm(resources: Resources) {
        _destination.value?.let { destination ->
            if (destination == -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    serverRepository.insert(
                        Server(
                            name = _name.value,
                            urlTemplate = _urlTemplate.value,
                            authType = _authType.value,
                            apiKey = _apiKey.value,
                            apiKeyHeader = _apiKeyHeader.value,
                            challengeUrl = _challengeUrl.value,
                            loginUrl = _loginUrl.value,
                            registerUrl = _registerUrl.value,
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
                                name = _name.value,
                                urlTemplate = _urlTemplate.value,
                                authType = _authType.value,
                                apiKey = _apiKey.value,
                                apiKeyHeader = _apiKeyHeader.value,
                                challengeUrl = _challengeUrl.value,
                                loginUrl = _loginUrl.value,
                                registerUrl = _registerUrl.value,
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
        _destination.value?.let { destination ->
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

    fun selectServerGoogleMapsAddress(uid: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.unselectAllGoogleMapsAddressAndSelect(uid)
        }
    }

    fun selectServerGoogleMapsPlace(uid: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.unselectAllGoogleMapsPlaceAndSelect(uid)
        }
    }

    fun selectServerSearch(uid: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.unselectAllSearchAndSelect(uid)
        }
    }

    fun setApiKey(newApiKey: String) {
        _apiKey.value = newApiKey
    }

    fun setApiKeyHeader(newApiKeyHeader: String) {
        _apiKeyHeader.value = newApiKeyHeader
    }

    fun setAuthType(newAuthType: ServerAuthType) {
        _authType.value = newAuthType
    }

    fun setChallengeUrl(newChallengeUrl: String) {
        _challengeUrl.value = newChallengeUrl
    }

    fun setLoginUrl(newLoginUrl: String) {
        _loginUrl.value = newLoginUrl
    }

    fun setName(newName: String) {
        _name.value = newName
    }

    fun setRegisterUrl(newRegisterUrl: String) {
        _registerUrl.value = newRegisterUrl
    }

    fun setUrlTemplate(newUrlTemplate: String) {
        _urlTemplate.value = newUrlTemplate
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

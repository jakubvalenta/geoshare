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
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.LinkType
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.geo.Srs
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class LinkViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val all: StateFlow<List<Link>> = linkRepository.all
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message.asStateFlow()

    /**
     * Dummy object to read default form values from.
     */
    private val default = Link()

    /**
     * Controls whether the list, insert, or update screen is displayed, so that the UI state survives process death.
     *
     * - null: list screen
     * - -1: insert screen
     * - other number: update screen for this object uid
     */
    private val _destination = savedStateHandle.getMutableStateFlow<Int?>("linkDestination", null)
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
                _group.value = default.group
                _name.value = default.name
                _srs.value = default.srs
                _type.value = default.type
                _appEnabled.value = default.appEnabled
                _chipEnabled.value = default.chipEnabled
                _sheetEnabled.value = default.sheetEnabled
                _coordsUriTemplate.value = default.coordsUriTemplate
                _nameUriTemplate.value = default.nameUriTemplate
            }
        } else {
            val item = linkRepository.getByUid(destination)
            if (item != null) {
                withMutableSnapshot {
                    _destination.value = destination
                    _group.value = item.group
                    _name.value = item.name
                    _srs.value = item.srs
                    _type.value = item.type
                    _appEnabled.value = item.appEnabled
                    _chipEnabled.value = item.chipEnabled
                    _sheetEnabled.value = item.sheetEnabled
                    _coordsUriTemplate.value = item.coordsUriTemplate
                    _nameUriTemplate.value = item.nameUriTemplate
                }
            }
        }
    }

    // Form

    private val _group = savedStateHandle.getMutableStateFlow("linkGroup", default.group)
    val group: StateFlow<String> = _group.asStateFlow()
    private val _name = savedStateHandle.getMutableStateFlow("linkName", default.name)
    val name: StateFlow<String> = _name.asStateFlow()
    private val _srs = savedStateHandle.getMutableStateFlow("linkSrs", default.srs)
    val srs: StateFlow<Srs> = _srs.asStateFlow()
    private val _type = savedStateHandle.getMutableStateFlow("linkType", default.type)
    val type: StateFlow<LinkType> = _type.asStateFlow()
    private val _appEnabled = savedStateHandle.getMutableStateFlow("linkAppEnabled", default.appEnabled)
    val appEnabled: StateFlow<Boolean> = _appEnabled.asStateFlow()
    private val _chipEnabled = savedStateHandle.getMutableStateFlow("linkChipEnabled", default.chipEnabled)
    val chipEnabled: StateFlow<Boolean> = _chipEnabled.asStateFlow()
    private val _sheetEnabled = savedStateHandle.getMutableStateFlow("linkSheetEnabled", default.sheetEnabled)
    val sheetEnabled: StateFlow<Boolean> = _sheetEnabled.asStateFlow()
    private val _coordsUriTemplate = savedStateHandle.getMutableStateFlow(
        "linkCoordsUriTemplate", default.coordsUriTemplate
    )
    val coordsUriTemplate: StateFlow<String> = _coordsUriTemplate.asStateFlow()
    private val _nameUriTemplate = savedStateHandle.getMutableStateFlow("linkNameUriTemplate", default.nameUriTemplate)
    val nameUriTemplate: StateFlow<String> = _nameUriTemplate.asStateFlow()

    fun saveForm(resources: Resources) {
        _destination.value?.let { destination ->
            if (destination == -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    linkRepository.insert(
                        Link(
                            group = _group.value,
                            name = _name.value,
                            srs = _srs.value,
                            type = _type.value,
                            appEnabled = _appEnabled.value,
                            chipEnabled = _chipEnabled.value,
                            sheetEnabled = _sheetEnabled.value,
                            coordsUriTemplate = _coordsUriTemplate.value,
                            nameUriTemplate = _nameUriTemplate.value,
                        )
                    )
                    _message.value = Message(resources.getString(R.string.links_message_inserted))
                    // Navigate after saving, because we reset form fields during navigation
                    navigateTo(null)
                }
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    val item = linkRepository.getByUid(destination)
                    if (item != null) {
                        linkRepository.update(
                            item.copy(
                                group = _group.value,
                                name = _name.value,
                                srs = _srs.value,
                                type = _type.value,
                                appEnabled = _appEnabled.value,
                                chipEnabled = _chipEnabled.value,
                                sheetEnabled = _sheetEnabled.value,
                                coordsUriTemplate = _coordsUriTemplate.value,
                                nameUriTemplate = _nameUriTemplate.value,
                            )
                        )
                        _message.value = Message(resources.getString(R.string.links_message_updated))
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
                        linkRepository.delete(item)
                        _message.value = Message(resources.getString(R.string.links_message_deleted))
                        navigateTo(null)
                    }
                }
            }
        }
    }

    fun enable(uid: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.enable(uid)
        }
    }

    fun disable(uid: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.disable(uid)
        }
    }

    fun disableGroup(resources: Resources, group: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.disableGroup(group)
            _message.value = Message(resources.getString(R.string.links_message_disabled_group))
        }
    }

    fun setGroup(newGroup: String) {
        _group.value = newGroup
    }

    fun setName(newName: String) {
        _name.value = newName
    }

    fun setSrs(newSrs: Srs) {
        _srs.value = newSrs
    }

    fun setType(newType: LinkType) {
        _type.value = newType
    }

    fun setAppEnabled(newAppEnabled: Boolean) {
        _appEnabled.value = newAppEnabled
    }

    fun setSheetEnabled(newSheetEnabled: Boolean) {
        _sheetEnabled.value = newSheetEnabled
    }

    fun setChipEnabled(newChipEnabled: Boolean) {
        _chipEnabled.value = newChipEnabled
    }

    fun setNameUriTemplate(newNameUriTemplate: String) {
        _nameUriTemplate.value = newNameUriTemplate
    }

    fun setCoordsUriTemplate(newCoordsUriTemplate: String) {
        _coordsUriTemplate.value = newCoordsUriTemplate
    }

    fun restoreInitialData(resources: Resources) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.restoreInitialData()
            _message.value = Message(resources.getString(R.string.links_message_factory_reset))
        }
    }

    fun dismissMessage() {
        _message.value = null
    }

    private companion object {
        private const val TAG = "LinkViewModel"
    }
}

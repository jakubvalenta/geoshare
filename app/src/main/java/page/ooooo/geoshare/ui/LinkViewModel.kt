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
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.Message
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
    val message: StateFlow<Message?> = _message

    /**
     * Dummy Link to read default form values from.
     */
    private val defaultLink = Link()

    /**
     * Controls whether the list, insert, or update screen is displayed, so that the UI state survives process death.
     *
     * - null: list screen
     * - -1: insert screen
     * - other number: update screen for this link uid
     */
    var destination by savedStateHandle.saveable { mutableStateOf<Int?>(null) }

    /**
     * Navigate to the list, insert, or update screen; and reset or prefill the form.
     */
    suspend fun navigateTo(destination: Int?) {
        Log.d("LinkViewModel", "navigateTo($destination)")
        if (this.destination == destination) {
            // Do nothing, so that we don't overwrite values restored after process death for no reason
        } else if (destination == null || destination == -1) {
            withMutableSnapshot {
                this.destination = destination
                this.group = defaultLink.group
                this.name = defaultLink.name
                this.srs = defaultLink.srs
                this.type = defaultLink.type
                this.appEnabled = defaultLink.appEnabled
                this.chipEnabled = defaultLink.chipEnabled
                this.sheetEnabled = defaultLink.sheetEnabled
                this.coordsUriTemplate = defaultLink.coordsUriTemplate
                this.nameUriTemplate = defaultLink.nameUriTemplate
            }
        } else {
            val link = linkRepository.getByUid(destination)
            if (link != null) {
                withMutableSnapshot {
                    this.destination = destination
                    this.group = link.group
                    this.name = link.name
                    this.srs = link.srs
                    this.type = link.type
                    this.appEnabled = link.appEnabled
                    this.chipEnabled = link.chipEnabled
                    this.sheetEnabled = link.sheetEnabled
                    this.coordsUriTemplate = link.coordsUriTemplate
                    this.nameUriTemplate = link.nameUriTemplate
                }
            }
        }
    }

    // Form

    var group by savedStateHandle.saveable { mutableStateOf(defaultLink.group) }
    var name by savedStateHandle.saveable { mutableStateOf(defaultLink.name) }
    var srs by savedStateHandle.saveable { mutableStateOf(defaultLink.srs) }
    var type by savedStateHandle.saveable { mutableStateOf(defaultLink.type) }
    var appEnabled by savedStateHandle.saveable { mutableStateOf(defaultLink.appEnabled) }
    var chipEnabled by savedStateHandle.saveable { mutableStateOf(defaultLink.chipEnabled) }
    var sheetEnabled by savedStateHandle.saveable { mutableStateOf(defaultLink.sheetEnabled) }
    var coordsUriTemplate by savedStateHandle.saveable { mutableStateOf(defaultLink.coordsUriTemplate) }
    var nameUriTemplate by savedStateHandle.saveable { mutableStateOf(defaultLink.nameUriTemplate) }

    fun saveForm(resources: Resources) {
        destination?.let { destination ->
            if (destination == -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    linkRepository.insert(
                        Link(
                            group = group,
                            name = name,
                            srs = srs,
                            type = type,
                            appEnabled = appEnabled,
                            chipEnabled = chipEnabled,
                            sheetEnabled = sheetEnabled,
                            coordsUriTemplate = coordsUriTemplate,
                            nameUriTemplate = nameUriTemplate,
                        )
                    )
                    _message.value = Message(resources.getString(R.string.links_message_inserted))
                    // Navigate after saving, because we reset form fields during navigation
                    navigateTo(null)
                }
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    val link = linkRepository.getByUid(destination)
                    if (link != null) {
                        linkRepository.update(
                            link.copy(
                                group = group,
                                name = name,
                                srs = srs,
                                type = type,
                                appEnabled = appEnabled,
                                chipEnabled = chipEnabled,
                                sheetEnabled = sheetEnabled,
                                coordsUriTemplate = coordsUriTemplate,
                                nameUriTemplate = nameUriTemplate,
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
        destination?.let { destination ->
            if (destination != -1) {
                val link = all.value.firstOrNull { it.uid == destination }
                if (link != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        linkRepository.delete(link)
                        _message.value = Message(resources.getString(R.string.links_message_deleted))
                        navigateTo(null)
                    }
                }
            }
        }
    }

    /**
     * Enable [link] if [enabled] is true, or disable the [link] if [enabled] is false.
     *
     * Notice that unlike in [saveForm] or [delete], we don't set [message] here, because the user already gets feedback,
     * since there is a switch in the UI that immediately gets toggled.
     */
    fun toggle(link: Link, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.update(if (enabled) link.enable() else link.disable())
        }
    }

    fun disableGroup(group: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            TODO()
        }
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
}

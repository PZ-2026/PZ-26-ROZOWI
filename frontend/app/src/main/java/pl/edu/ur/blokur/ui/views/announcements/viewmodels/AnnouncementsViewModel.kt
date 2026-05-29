package pl.edu.ur.blokur.ui.views.announcements.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.services.AnnouncementService
import pl.edu.ur.blokur.ui.views.announcements.utils.AnnouncementsEvent
import pl.edu.ur.blokur.ui.views.announcements.utils.AnnouncementsState
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val announcementService: AnnouncementService
) : ViewModel() {

    private val _state = MutableStateFlow<AnnouncementsState>(AnnouncementsState.Loading)
    val state: StateFlow<AnnouncementsState> = _state.asStateFlow()

    private val _events = Channel<AnnouncementsEvent>()
    val events: Flow<AnnouncementsEvent> = _events.receiveAsFlow()

    init {
        loadAnnouncements()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _state.value = AnnouncementsState.Loading
            runCatching { announcementService.getAnnouncements() }
                .onSuccess { list ->
                    _state.value = if (list.isEmpty()) AnnouncementsState.Empty
                    else AnnouncementsState.Success(list)
                }
                .onFailure { e ->
                    _state.value = AnnouncementsState.Error(e.message ?: "Błąd ładowania ogłoszeń")
                    _events.send(AnnouncementsEvent.ShowError(e.message ?: "Błąd ładowania ogłoszeń"))
                }
        }
    }
}
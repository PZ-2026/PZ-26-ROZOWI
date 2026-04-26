package pl.edu.ur.blokur.ui.views.announcements.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import pl.edu.ur.blokur.ui.views.announcements.utils.AnnouncementsEvent
import pl.edu.ur.blokur.ui.views.announcements.utils.AnnouncementsState
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<AnnouncementsState>(AnnouncementsState.Empty)
    val state: StateFlow<AnnouncementsState> = _state.asStateFlow()

    private val _events = Channel<AnnouncementsEvent>()
    val events: Flow<AnnouncementsEvent> = _events.receiveAsFlow()
}
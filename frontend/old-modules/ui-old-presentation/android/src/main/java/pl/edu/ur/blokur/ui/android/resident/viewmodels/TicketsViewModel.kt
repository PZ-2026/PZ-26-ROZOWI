package pl.edu.ur.blokur.ui.android.resident.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import pl.edu.ur.blokur.ui.android.resident.states.TicketsEvent
import pl.edu.ur.blokur.ui.android.resident.states.TicketsState
import javax.inject.Inject

class TicketsViewModel @Inject constructor(

) : ViewModel() {
    private val _state = MutableStateFlow<TicketsState>(TicketsState.Loading)
    val state: StateFlow<TicketsState> = _state.asStateFlow()

    private val _events = Channel<TicketsEvent>()
    val events: Flow<TicketsEvent> = _events.receiveAsFlow()

    init {

    }



}
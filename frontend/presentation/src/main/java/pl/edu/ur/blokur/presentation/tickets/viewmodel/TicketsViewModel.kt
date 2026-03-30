package pl.edu.ur.blokur.presentation.tickets.viewmodel

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
import pl.edu.ur.blokur.domain.repository.TicketRepository
import pl.edu.ur.blokur.presentation.tickets.util.TicketsEvent
import pl.edu.ur.blokur.presentation.tickets.util.TicketsState
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _state = MutableStateFlow<TicketsState>(TicketsState.Loading)
    val state: StateFlow<TicketsState> = _state.asStateFlow()

    private val _events = Channel<TicketsEvent>()
    val events: Flow<TicketsEvent> = _events.receiveAsFlow()

    init {
        loadTickets()
    }

    private fun loadTickets() {
        viewModelScope.launch {
            runCatching { ticketRepository.getTickets() }
                .onSuccess { tickets -> _state.value = TicketsState.Data(tickets) }
                .onFailure { e -> _state.value = TicketsState.Error(e.message ?: "Błąd ładowania zgłoszeń") }
        }
    }

    fun onTicketClicked(ticketId: Int) {
        viewModelScope.launch {
            _events.send(TicketsEvent.NavigateToDetails(ticketId))
        }
    }

    fun onCreateTicketClicked() {
        viewModelScope.launch {
            _events.send(TicketsEvent.NavigateToCreate)
        }
    }
}
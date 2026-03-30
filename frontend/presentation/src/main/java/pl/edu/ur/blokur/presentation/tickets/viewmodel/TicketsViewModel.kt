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
import pl.edu.ur.blokur.presentation.tickets.util.TicketsListState
import pl.edu.ur.blokur.presentation.tickets.util.TicketsScreenEvent
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _state = MutableStateFlow<TicketsListState>(TicketsListState.Loading)
    val state: StateFlow<TicketsListState> = _state.asStateFlow()

    private val _events = Channel<TicketsScreenEvent>()
    val events: Flow<TicketsScreenEvent> = _events.receiveAsFlow()

    init {
        loadTickets()
    }

    private fun loadTickets() {
        viewModelScope.launch {
            runCatching { ticketRepository.getTickets() }
                .onSuccess { tickets -> _state.value = TicketsListState.Success(tickets) }
                .onFailure { e -> _state.value = TicketsListState.Error(e.message ?: "Błąd ładowania zgłoszeń") }
        }
    }

    fun onTicketClicked(ticketId: Int) {
        viewModelScope.launch {
            _events.send(TicketsScreenEvent.NavigateToDetails(ticketId))
        }
    }

    fun onCreateTicketClicked() {
        viewModelScope.launch {
            _events.send(TicketsScreenEvent.NavigateToCreate)
        }
    }
}
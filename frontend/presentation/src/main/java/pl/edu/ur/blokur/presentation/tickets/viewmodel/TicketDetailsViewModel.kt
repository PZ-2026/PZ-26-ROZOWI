package pl.edu.ur.blokur.presentation.tickets.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import pl.edu.ur.blokur.presentation.tickets.util.TicketDetailsEvent
import pl.edu.ur.blokur.presentation.tickets.util.TicketDetailsState
import javax.inject.Inject

@HiltViewModel
class TicketDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<TicketRoutes.Details>()

    private val _state = MutableStateFlow<TicketDetailsState>(TicketDetailsState.Loading)
    val state: StateFlow<TicketDetailsState> = _state.asStateFlow()

    private val _events = Channel<TicketDetailsEvent>()
    val events: Flow<TicketDetailsEvent> = _events.receiveAsFlow()

    init {
        loadTicket()
    }

    private fun loadTicket() {
        viewModelScope.launch {
            runCatching {
                val ticket = ticketRepository.getTicketById(route.ticketId)
                    ?: error("Nie znaleziono zgłoszenia #${route.ticketId}")
                val conservators = ticketRepository.getAvailableConservators()
                ticket to conservators
            }.onSuccess { (ticket, conservators) ->
                _state.value = TicketDetailsState.Data(ticket, availableConservators = conservators)
            }.onFailure { e ->
                _state.value = TicketDetailsState.Error(e.message ?: "Błąd ładowania zgłoszenia")
            }
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch {
            _events.send(TicketDetailsEvent.NavigateBack)
        }
    }
}
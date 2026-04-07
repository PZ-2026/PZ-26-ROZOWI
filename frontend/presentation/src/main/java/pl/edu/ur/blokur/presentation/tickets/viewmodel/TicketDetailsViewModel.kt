package pl.edu.ur.blokur.presentation.tickets.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.domain.repository.TicketRepository
import pl.edu.ur.blokur.presentation.tickets.TicketRoutes
import pl.edu.ur.blokur.presentation.tickets.util.ConservatorActionType
import pl.edu.ur.blokur.presentation.tickets.util.TicketDetailsListState
import pl.edu.ur.blokur.presentation.tickets.util.TicketDetailsScreenEvent
import javax.inject.Inject

@HiltViewModel
class TicketDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<TicketRoutes.Details>()

    private val _state = MutableStateFlow<TicketDetailsListState>(TicketDetailsListState.Loading)
    val state: StateFlow<TicketDetailsListState> = _state.asStateFlow()

    private val _events = Channel<TicketDetailsScreenEvent>()
    val events: Flow<TicketDetailsScreenEvent> = _events.receiveAsFlow()

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
                // Pobierz rolę bieżącego użytkownika z repozytorium / SharedPreferences
                // Na potrzeby mock — zakładamy że mock dostarcza currentUser
                val role = ticketRepository.getCurrentUserRole()
                _state.value = TicketDetailsListState.Success(
                    ticket = ticket,
                    availableConservators = conservators,
                    currentUserRole = role
                )
            }.onFailure { e ->
                _state.value = TicketDetailsListState.Error(e.message ?: "Błąd ładowania zgłoszenia")
            }
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch { _events.send(TicketDetailsScreenEvent.NavigateBack) }
    }

    fun onAssignConservator(conservatorId: Int, scheduledAt: String) {
        viewModelScope.launch {
            // Mock: w przyszłości API call
            _events.send(TicketDetailsScreenEvent.AssignConservator(conservatorId, scheduledAt))
            // Odśwież stan po przypisaniu
            loadTicket()
        }
    }

    fun onRejectTicket(reason: String) {
        viewModelScope.launch {
            _events.send(TicketDetailsScreenEvent.RejectTicket(reason))
            loadTicket()
        }
    }

    fun onConservatorAction(type: ConservatorActionType, comment: String, pause: Boolean = false) {
        viewModelScope.launch {
            _events.send(TicketDetailsScreenEvent.ConservatorAction(type, comment, pause))
            loadTicket()
        }
    }
}
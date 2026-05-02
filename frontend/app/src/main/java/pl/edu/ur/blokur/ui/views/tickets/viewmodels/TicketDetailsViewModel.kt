package pl.edu.ur.blokur.ui.views.tickets.viewmodels

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
import pl.edu.ur.blokur.services.TicketService
import pl.edu.ur.blokur.ui.views.tickets.TicketRoutes
import pl.edu.ur.blokur.ui.views.tickets.utils.ConservatorActionType
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketDetailsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketDetailsScreenEvent
import javax.inject.Inject

@HiltViewModel
class TicketDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ticketService: TicketService
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
                val ticket = ticketService.getTicketById(route.ticketId)
                    ?: error("Nie znaleziono zgłoszenia #${route.ticketId}")
                val conservators = ticketService.getAvailableConservators()
                ticket to conservators
            }.onSuccess { (ticket, conservators) ->
                val role = ticketService.getCurrentUserRole()
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

    fun onAssignConservator(conservatorId: String, plannedVisitAt: String) {
        val currentState = _state.value as? TicketDetailsListState.Success ?: return
        viewModelScope.launch {
            runCatching {
                ticketService.assignTicket(
                    ticketId = currentState.ticket.id,
                    conservatorId = conservatorId,
                    plannedVisitAt = plannedVisitAt
                )
            }.onSuccess {
                loadTicket()
                _events.send(TicketDetailsScreenEvent.ShowSnackbar)
            }.onFailure { e ->
                _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd przypisywania"))
            }
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
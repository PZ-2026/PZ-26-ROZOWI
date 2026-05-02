package pl.edu.ur.blokur.ui.views.tickets.viewmodels

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
import pl.edu.ur.blokur.dtos.TicketSummaryDto
import pl.edu.ur.blokur.services.TicketService
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketFilterState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsScreenEvent
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val ticketService: TicketService
) : ViewModel() {

    private val _state = MutableStateFlow<TicketsListState>(TicketsListState.Loading)
    val state: StateFlow<TicketsListState> = _state.asStateFlow()

    private val _events = Channel<TicketsScreenEvent>()
    val events: Flow<TicketsScreenEvent> = _events.receiveAsFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _state.value = TicketsListState.Loading
            runCatching {
                val tickets = ticketService.getTickets()
                val role = ticketService.getCurrentUserRole()
                tickets to role
            }.onSuccess { (tickets, role) ->
                val currentFilter = (_state.value as? TicketsListState.Success)?.filterState
                    ?: TicketFilterState()
                _state.value = TicketsListState.Success(
                    allTickets = tickets,
                    filteredTickets = applyFilter(tickets, currentFilter),
                    currentUserRole = role,
                    filterState = currentFilter
                )
            }.onFailure { e ->
                _state.value = TicketsListState.Error(e.message ?: "Błąd ładowania zgłoszeń")
            }
        }
    }

    fun onFilterChanged(newFilter: TicketFilterState) {
        val current = _state.value as? TicketsListState.Success ?: return
        _state.value = current.copy(
            filteredTickets = applyFilter(current.allTickets, newFilter),
            filterState = newFilter
        )
    }

    private fun applyFilter(
        tickets: List<TicketSummaryDto>,
        filter: TicketFilterState
    ): List<TicketSummaryDto> {
        return tickets.filter { ticket ->
            val matchesSearch = if (filter.searchQuery.isBlank()) true else {
                ticket.title.contains(filter.searchQuery, ignoreCase = true) ||
                ticket.ticketNumber.contains(filter.searchQuery, ignoreCase = true) ||
                ticket.categoryName.contains(filter.searchQuery, ignoreCase = true)
            }
            val matchesStatus = if (filter.selectedStatus.isBlank()) true else {
                ticket.status.name == filter.selectedStatus
            }
            matchesSearch && matchesStatus
        }
    }

    fun onTicketClicked(ticketId: String) {
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
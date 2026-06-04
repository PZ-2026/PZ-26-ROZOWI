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

    private var currentPage = 0
    private val pageSize = 20
    private val currentTickets = mutableListOf<TicketSummaryDto>()
    private var currentFilter = TicketFilterState()
    private var isFetching = false
    private var hasReachedEnd = false

    init {
        loadTickets(reset = true)
    }

    fun loadTickets(reset: Boolean = false) {
        if (isFetching || (hasReachedEnd && !reset)) return

        viewModelScope.launch {
            isFetching = true
            
            if (reset) {
                currentPage = 0
                currentTickets.clear()
                hasReachedEnd = false
                _state.value = TicketsListState.Loading
            } else {
                val currentState = _state.value as? TicketsListState.Success
                if (currentState != null) {
                    _state.value = currentState.copy(isFetchingNextPage = true)
                }
            }

            runCatching {
                val statusParam = if (currentFilter.selectedStatus.isBlank()) null else currentFilter.selectedStatus
                val searchParam = if (currentFilter.searchQuery.isBlank()) null else currentFilter.searchQuery
                
                val fetchedTickets = ticketService.getTickets(
                    status = statusParam,
                    search = searchParam,
                    page = currentPage,
                    size = pageSize
                )
                val role = ticketService.getCurrentUserRole()
                fetchedTickets to role
            }.onSuccess { (fetchedTickets, role) ->
                currentTickets.addAll(fetchedTickets)
                if (fetchedTickets.size < pageSize) {
                    hasReachedEnd = true
                }
                
                _state.value = TicketsListState.Success(
                    tickets = currentTickets.toList(),
                    currentUserRole = role,
                    filterState = currentFilter,
                    isFetchingNextPage = false,
                    hasReachedEnd = hasReachedEnd
                )
                
                currentPage++
                isFetching = false
            }.onFailure { e ->
                isFetching = false
                if (reset) {
                    _state.value = TicketsListState.Error(e.message ?: "Błąd ładowania zgłoszeń")
                } else {
                    val currentState = _state.value as? TicketsListState.Success
                    if (currentState != null) {
                        _state.value = currentState.copy(isFetchingNextPage = false)
                    }
                }
            }
        }
    }

    fun onFilterChanged(newFilter: TicketFilterState) {
        if (currentFilter != newFilter) {
            currentFilter = newFilter
            loadTickets(reset = true)
        }
    }

    fun loadNextPage() {
        loadTickets(reset = false)
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
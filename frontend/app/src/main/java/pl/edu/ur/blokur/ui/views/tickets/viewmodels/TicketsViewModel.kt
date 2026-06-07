package pl.edu.ur.blokur.ui.views.tickets.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.services.PropertyService
import pl.edu.ur.blokur.services.TicketService
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketFilterOptions
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketFilterState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsScreenEvent
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val ticketService: TicketService,
    private val propertyService: PropertyService
) : ViewModel() {

    private val _state = MutableStateFlow<TicketsListState>(TicketsListState.Loading)
    val state: StateFlow<TicketsListState> = _state.asStateFlow()

    private val _events = Channel<TicketsScreenEvent>()
    val events: Flow<TicketsScreenEvent> = _events.receiveAsFlow()

    private var currentFilter = TicketFilterState()
    private var filterOptions = TicketFilterOptions()
    private var isFetching = false
    private var filterOptionsLoaded = false

    init {
        loadTickets()
    }

    fun loadTickets() {
        if (isFetching) return

        viewModelScope.launch {
            isFetching = true
            val previousSuccess = _state.value as? TicketsListState.Success
            _state.value = TicketsListState.Loading

            runCatching {
                val role = ticketService.getCurrentUserRole()
                if (role == "ZARZADCA" && !filterOptionsLoaded) {
                    loadFilterOptions()
                }

                val fetchedTickets = ticketService.getTickets(
                    status = currentFilter.selectedStatus.blankOrNull(),
                    categoryId = currentFilter.categoryId.blankOrNull(),
                    buildingId = currentFilter.buildingId.blankOrNull(),
                    staircaseId = currentFilter.staircaseId.blankOrNull(),
                    assignedTo = currentFilter.assignedTo.blankOrNull(),
                    dateFrom = currentFilter.dateFrom.blankOrNull(),
                    dateTo = currentFilter.dateTo.blankOrNull(),
                    search = currentFilter.searchQuery.blankOrNull()
                )
                Triple(fetchedTickets, role, previousSuccess?.tickets)
            }.onSuccess { (fetchedTickets, role, _) ->
                _state.value = TicketsListState.Success(
                    tickets = fetchedTickets,
                    currentUserRole = role,
                    filterState = currentFilter,
                    filterOptions = filterOptions
                )
                isFetching = false
            }.onFailure { e ->
                isFetching = false
                _state.value = TicketsListState.Error(e.message ?: "Błąd ładowania zgłoszeń")
            }
        }
    }

    private suspend fun loadFilterOptions() {
        filterOptions = filterOptions.copy(isLoading = true)
        runCatching {
            coroutineScope {
                val categoriesDeferred = async { ticketService.getCategories() }
                val buildingsDeferred = async { propertyService.getBuildingTree() }
                val conservatorsDeferred = async { ticketService.getAvailableConservators() }
                Triple(
                    categoriesDeferred.await(),
                    buildingsDeferred.await(),
                    conservatorsDeferred.await()
                )
            }
        }.onSuccess { (categories, buildings, conservators) ->
            filterOptions = TicketFilterOptions(
                categories = categories,
                buildings = buildings,
                conservators = conservators,
                isLoading = false
            )
            filterOptionsLoaded = true
        }.onFailure { e ->
            filterOptions = filterOptions.copy(isLoading = false)
            _events.send(
                TicketsScreenEvent.ShowSnackbar(
                    e.message ?: "Nie udało się załadować opcji filtrów"
                )
            )
        }
    }

    fun onFilterChanged(newFilter: TicketFilterState) {
        if (currentFilter != newFilter) {
            currentFilter = newFilter
            loadTickets()
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

    private fun String.blankOrNull(): String? = if (isBlank()) null else this
}

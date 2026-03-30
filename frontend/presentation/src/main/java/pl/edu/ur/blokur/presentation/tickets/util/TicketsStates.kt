package pl.edu.ur.blokur.presentation.tickets.util

import pl.edu.ur.blokur.domain.model.AppUser
import pl.edu.ur.blokur.domain.model.Ticket

// ─── Ticket List ──────────────────────────────────────────────────────────────

sealed interface TicketsListState {
    data object Loading : TicketsListState
    data class Error(val message: String) : TicketsListState
    data class Success(val tickets: List<Ticket>) : TicketsListState
}

sealed interface TicketsScreenEvent {
    data class NavigateToDetails(val ticketId: Int) : TicketsScreenEvent
    data object NavigateToCreate : TicketsScreenEvent
}

// ─── Ticket Details ───────────────────────────────────────────────────────────

sealed interface TicketDetailsListState {
    data object Loading : TicketDetailsListState
    data class Error(val message: String) : TicketDetailsListState
    data class Success(
        val ticket: Ticket,
        val availableConservators: List<AppUser> = emptyList()
    ) : TicketDetailsListState
}

sealed interface TicketDetailsScreenEvent {
    data object NavigateBack : TicketDetailsScreenEvent
}

// ─── Create Ticket ────────────────────────────────────────────────────────────

data class CreateTicketFormState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: String = "",
    val isCategoryExpanded: Boolean = false
)

sealed interface CreateTicketSubmitState {
    data object Idle : CreateTicketSubmitState
    data object Submitting : CreateTicketSubmitState
    data object Success : CreateTicketSubmitState
    data class Error(val message: String) : CreateTicketSubmitState
}

sealed interface CreateTicketScreenEvent {
    data object NavigateBack : CreateTicketScreenEvent
}

// backward compat aliases (used in TicketDetailsViewModel via SavedStateHandle)
typealias TicketsState = TicketsListState
typealias TicketsEvent = TicketsScreenEvent
typealias TicketDetailsState = TicketDetailsListState
typealias TicketDetailsEvent = TicketDetailsScreenEvent
typealias CreateTicketState = CreateTicketSubmitState
typealias CreateTicketEvent = CreateTicketScreenEvent
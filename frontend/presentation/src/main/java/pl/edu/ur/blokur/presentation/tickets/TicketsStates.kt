package pl.edu.ur.blokur.presentation.tickets

import pl.edu.ur.blokur.domain.model.AppUser
import pl.edu.ur.blokur.domain.model.Ticket

sealed interface TicketsState {
    data object Loading : TicketsState
    data class Error(val message: String) : TicketsState
    data class Data(val tickets: List<Ticket>, val currentUser: AppUser? = null) : TicketsState
}

sealed interface TicketsEvent {
    data class NavigateToDetails(val ticketId: Int) : TicketsEvent
    data object NavigateToCreate : TicketsEvent
}

sealed interface TicketDetailsState {
    data object Loading : TicketDetailsState
    data class Error(val message: String) : TicketDetailsState
    data class Data(
        val ticket: Ticket,
        val currentUser: AppUser? = null,
        val availableConservators: List<AppUser> = emptyList()
    ) : TicketDetailsState
}

sealed interface TicketDetailsEvent {
    data object NavigateBack : TicketDetailsEvent
}

sealed interface CreateTicketState {
    data object Idle : CreateTicketState
    data object Submitting : CreateTicketState
    data object Success : CreateTicketState
    data class Error(val message: String) : CreateTicketState
}

sealed interface CreateTicketEvent {
    data object NavigateBack : CreateTicketEvent
}
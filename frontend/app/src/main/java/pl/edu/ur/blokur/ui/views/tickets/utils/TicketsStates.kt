package pl.edu.ur.blokur.ui.views.tickets.utils

import pl.edu.ur.blokur.dtos.AppUserDto
import pl.edu.ur.blokur.dtos.TicketDetailDto
import pl.edu.ur.blokur.dtos.TicketSummaryDto

sealed interface TicketsListState {
    data object Loading : TicketsListState
    data class Error(val message: String) : TicketsListState
    data class Success(val tickets: List<TicketSummaryDto>, val currentUserRole: String = "MIESZKANIEC") : TicketsListState
}

sealed interface TicketsScreenEvent {
    data class NavigateToDetails(val ticketId: String) : TicketsScreenEvent
    data object NavigateToCreate : TicketsScreenEvent
}

sealed interface TicketDetailsListState {
    data object Loading : TicketDetailsListState
    data class Error(val message: String) : TicketDetailsListState
    data class Success(
        val ticket: TicketDetailDto,
        val availableConservators: List<AppUserDto> = emptyList(),
        val currentUserRole: String = "MIESZKANIEC"
    ) : TicketDetailsListState
}

/** Akcje konserwatora — typ przekazywany do ConservatorActionSheet */
enum class ConservatorActionType { START, FINISH, PAUSE_OR_COMMENT }

sealed interface TicketDetailsScreenEvent {
    data object NavigateBack : TicketDetailsScreenEvent
    data class AssignConservator(val conservatorEmail: String, val scheduledAt: String) : TicketDetailsScreenEvent
    data class RejectTicket(val reason: String) : TicketDetailsScreenEvent
    data class ConservatorAction(val type: ConservatorActionType, val comment: String, val pause: Boolean = false) : TicketDetailsScreenEvent
    data object ShowSnackbar : TicketDetailsScreenEvent
}


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

typealias TicketsState = TicketsListState
typealias TicketsEvent = TicketsScreenEvent
typealias TicketDetailsState = TicketDetailsListState
typealias TicketDetailsEvent = TicketDetailsScreenEvent
typealias CreateTicketState = CreateTicketSubmitState
typealias CreateTicketEvent = CreateTicketScreenEvent
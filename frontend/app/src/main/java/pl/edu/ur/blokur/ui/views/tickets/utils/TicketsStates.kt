package pl.edu.ur.blokur.ui.views.tickets.utils

import pl.edu.ur.blokur.dtos.BuildingTreeNodeDto
import pl.edu.ur.blokur.dtos.CategoryDto
import pl.edu.ur.blokur.dtos.ConservatorDto
import pl.edu.ur.blokur.dtos.TicketDetailDto
import pl.edu.ur.blokur.dtos.TicketSummaryDto

sealed interface TicketsListState {
    data object Loading : TicketsListState
    data class Error(val message: String) : TicketsListState
    data class Success(
        val tickets: List<TicketSummaryDto>,
        val currentUserRole: String = "MIESZKANIEC",
        val filterState: TicketFilterState = TicketFilterState(),
        val filterOptions: TicketFilterOptions = TicketFilterOptions()
    ) : TicketsListState
}

data class TicketFilterState(
    val searchQuery: String = "",
    val selectedStatus: String = "",
    val categoryId: String = "",
    val buildingId: String = "",
    val staircaseId: String = "",
    val assignedTo: String = "",
    val dateFrom: String = "",
    val dateTo: String = ""
) {
    fun hasActiveFilters(): Boolean =
        searchQuery.isNotBlank() ||
            selectedStatus.isNotBlank() ||
            categoryId.isNotBlank() ||
            buildingId.isNotBlank() ||
            staircaseId.isNotBlank() ||
            assignedTo.isNotBlank() ||
            dateFrom.isNotBlank() ||
            dateTo.isNotBlank()
}

/** Słowniki do panelu filtrów (ładowane z API dla ZARZĄDCA). */
data class TicketFilterOptions(
    val categories: List<CategoryDto> = emptyList(),
    val buildings: List<BuildingTreeNodeDto> = emptyList(),
    val conservators: List<ConservatorDto> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface TicketsScreenEvent {
    data class NavigateToDetails(val ticketId: String) : TicketsScreenEvent
    data object NavigateToCreate : TicketsScreenEvent
    data class ShowSnackbar(val message: String) : TicketsScreenEvent
}

sealed interface TicketDetailsListState {
    data object Loading : TicketDetailsListState
    data class Error(val message: String) : TicketDetailsListState
    data class Success(
        val ticket: TicketDetailDto,
        val availableConservators: List<ConservatorDto> = emptyList(),
        val currentUserRole: String = "MIESZKANIEC",
        val comments: List<pl.edu.ur.blokur.dtos.TicketCommentDto> = emptyList(),
        val images: List<pl.edu.ur.blokur.dtos.TicketImageDto> = emptyList(),
        val isLoadingComments: Boolean = false,
        val isLoadingImages: Boolean = false,
        val isSendingComment: Boolean = false,
        val commentResetKey: Int = 0,
        val isUploadingImage: Boolean = false,
        val isDownloadingProtocol: Boolean = false
    ) : TicketDetailsListState
}

/** Akcje konserwatora i zarządcy przy przeglądzie zgłoszenia */
enum class ConservatorActionType { START, FINISH, PAUSE_OR_COMMENT, CLOSE_VERIFICATION }

sealed interface TicketDetailsScreenEvent {
    data object NavigateBack : TicketDetailsScreenEvent
    data class AssignConservator(val conservatorEmail: String, val scheduledAt: String) : TicketDetailsScreenEvent
    data class RejectTicket(val reason: String) : TicketDetailsScreenEvent
    data class ConservatorAction(val type: ConservatorActionType, val comment: String, val pause: Boolean = false) : TicketDetailsScreenEvent
    data class ShowSnackbar(val message: String) : TicketDetailsScreenEvent
    data class ShowError(val message: String) : TicketDetailsScreenEvent
}


data class CreateTicketFormState(
    val title: String = "",
    val description: String = "",
    val selectedCategoryId: String = "",
    val selectedCategoryName: String = "",
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
    data class ShowSuccess(val ticketNumber: String) : CreateTicketScreenEvent
}

typealias TicketsState = TicketsListState
typealias TicketsEvent = TicketsScreenEvent
typealias TicketDetailsState = TicketDetailsListState
typealias TicketDetailsEvent = TicketDetailsScreenEvent
typealias CreateTicketState = CreateTicketSubmitState
typealias CreateTicketEvent = CreateTicketScreenEvent

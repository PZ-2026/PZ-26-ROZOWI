package pl.edu.ur.blokur.application.dto

data class TicketDTO(
    val id: Int,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val category: TicketCategoryDTO,
    val author: AppUserDTO,
    val assignedTo: AppUserDTO?,
    val apartment: ApartmentDTO?,
    val staircase: StaircaseDTO?,
    val building: BuildingDTO?,
    val isDeleted: Boolean,
    val createdAt: String,
    val closedAt: String?,
    val images: List<String>,
    val history: List<TicketHistoryDTO>
)

data class TicketCategoryDTO(
    val id: Int,
    val name: String
)

data class AppUserDTO(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)

data class BuildingDTO(
    val id: Int,
    val estateName: String,
    val name: String,
    val address: String
)

data class StaircaseDTO(
    val id: Int,
    val label: String
)

data class ApartmentDTO(
    val id: Int,
    val number: String
)

enum class TicketStatus {
    NOWE, ZAPLANOWANO, W_REALIZACJI, WSTRZYMANO, ZAKONCZONE, ZAMKNIETE, ODRZUCONE
}

data class TicketHistoryDTO(
    val id: Int,
    val ticketId: Int,
    val status: TicketStatus,
    val changedBy: AppUserDTO,
    val comment: String?,
    val createdAt: String
)
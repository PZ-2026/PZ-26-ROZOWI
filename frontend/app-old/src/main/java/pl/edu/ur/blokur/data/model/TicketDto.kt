package pl.edu.ur.blokur.data.model

data class TicketDto(
    val id: Int,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val category: TicketCategoryDto,
    val author: AppUserDto,
    val assignedTo: AppUserDto?,
    val apartment: ApartmentDto?,
    val staircase: StaircaseDto?,
    val building: BuildingDto?,
    val isDeleted: Boolean,
    val createdAt: String,
    val closedAt: String?,
    val images: List<String>,
    val history: List<TicketHistoryDto>,
)

data class TicketCategoryDto(
    val id: Int,
    val name: String,
)

data class AppUserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
)

data class BuildingDto(
    val id: Int,
    val estateName: String,
    val name: String,
    val address: String,
)

data class StaircaseDto(
    val id: Int,
    val label: String,
)

data class ApartmentDto(
    val id: Int,
    val number: String,
)

enum class TicketStatus {
    NOWE,
    ZAPLANOWANO,
    W_REALIZACJI,
    WSTRZYMANO,
    ZAKONCZONE,
    ZAMKNIETE,
    ODRZUCONE,
}

data class TicketHistoryDto(
    val id: Int,
    val ticketId: Int,
    val status: TicketStatus,
    val changedBy: AppUserDto,
    val comment: String?,
    val createdAt: String,
)

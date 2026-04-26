package pl.edu.ur.blokur.dtos

enum class TicketStatus {
    NOWE, ZAPLANOWANO, W_REALIZACJI, WSTRZYMANO, ZAKONCZONE, ZAMKNIETE, ODRZUCONE
}

data class AppUserDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
) {
    val fullName: String get() = "$firstName $lastName"
}

data class TicketCategoryDto(
    val name: String
)

data class BuildingDto(
    val name: String,
    val address: String
)

data class StaircaseDto(
    val label: String
)

data class ApartmentDto(
    val number: String
)

data class TicketHistoryDto(
    val status: TicketStatus,
    val changedBy: AppUserDto,
    val comment: String?,
    val createdAt: String
)

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
    val history: List<TicketHistoryDto>
)

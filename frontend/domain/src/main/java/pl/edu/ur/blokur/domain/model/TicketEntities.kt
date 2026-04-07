package pl.edu.ur.blokur.domain.model

enum class TicketStatus {
    NOWE, ZAPLANOWANO, W_REALIZACJI, WSTRZYMANO, ZAKONCZONE, ZAMKNIETE, ODRZUCONE
}

data class AppUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
) {
    val fullName: String get() = "$firstName $lastName"
}

data class TicketCategory(
    val id: Int,
    val name: String
)

data class Building(
    val id: Int,
    val estateName: String,
    val name: String,
    val address: String
)

data class Staircase(
    val id: Int,
    val label: String
)

data class Apartment(
    val id: Int,
    val number: String
)

data class TicketHistory(
    val id: Int,
    val ticketId: Int,
    val status: TicketStatus,
    val changedBy: AppUser,
    val comment: String?,
    val createdAt: String
)

data class Ticket(
    val id: Int,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val category: TicketCategory,
    val author: AppUser,
    val assignedTo: AppUser?,
    val apartment: Apartment?,
    val staircase: Staircase?,
    val building: Building?,
    val isDeleted: Boolean,
    val createdAt: String,
    val closedAt: String?,
    val images: List<String>,
    val history: List<TicketHistory>
)

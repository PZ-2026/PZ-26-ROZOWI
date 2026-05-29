package pl.edu.ur.blokur.dtos

enum class TicketStatus {
    NOWE, ZAPLANOWANO, W_REALIZACJI, WSTRZYMANO, ZAKONCZONE_DO_WERYFIKACJI, ZAMKNIETE, ODRZUCONE
}

data class CategoryDto(
    val id: String,
    val name: String,
    val slaHours: Int? = null
)

data class CreateTicketRequest(
    val title: String,
    val description: String,
    val categoryId: String
)

data class ConservatorDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val activeTicketsCount: Long
) {
    val fullName: String get() = "$firstName $lastName"
}

data class TicketAssignRequest(
    val assignedTo: String,
    val plannedVisitAt: String,
    val internalNote: String? = null
)

data class TicketSummaryDto(
    val id: String,
    val ticketNumber: String,
    val title: String,
    val status: TicketStatus,
    val categoryName: String,
    val authorName: String,
    val assignedToName: String?,
    val locationLabel: String?,
    val createdAt: String,
    val closedAt: String?,
    val slaBreached: Boolean
)

data class TicketDetailDto(
    val id: String,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val categoryName: String,
    val categoryId: String,
    val authorName: String,
    val authorId: String,
    val assignedToName: String?,
    val assignedToId: String?,
    val locationLabel: String?,
    val apartmentId: String?,
    val plannedVisitAt: String?,
    val internalNote: String?,
    val createdAt: String,
    val updatedAt: String?,
    val closedAt: String?
)

data class AppUserDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
) {
    val fullName: String get() = "$firstName $lastName"
}

// ── Request DTOs dla akcji na zgłoszeniu ──────────────────────────────────────

/** PATCH /api/tickets/{id}/reject — odrzucenie zgłoszenia przez zarządcę. */
data class TicketRejectRequest(
    val reason: String
)

/** PATCH /api/tickets/{id}/suspend — wstrzymanie prac przez konserwatora. */
data class TicketSuspendRequest(
    val reason: String
)

/** POST /api/tickets/{id}/completion — zakończenie prac przez konserwatora. */
data class TicketCompletionRequest(
    val workDescription: String
)

/**
 * PATCH /api/tickets/{id}/status — ogólna zmiana statusu przez maszynę stanów.
 * Używana przez ZARZADCA i KONSERWATOR do dowolnych dozwolonych przejść.
 * Dozwolone statusy docelowe są walidowane po stronie backendu przez TicketStateMachine.
 */
data class TicketStatusChangeRequest(
    val status: String,
    val comment: String? = null
)

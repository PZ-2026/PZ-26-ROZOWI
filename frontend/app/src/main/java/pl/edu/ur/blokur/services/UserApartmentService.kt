package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.UserRole
import javax.inject.Inject
import javax.inject.Singleton

/** Informacja o lokalu przypisanym mieszkańcowi (rozwiązywana z istniejącego API). */
data class ResidentApartmentInfo(
    val apartmentId: String,
    val label: String
)

/** Brak możliwości ustalenia lokalu mieszkańca bez nowego endpointu backendowego. */
class UserApartmentException(message: String) : Exception(message)

/**
 * Ustalanie lokalu mieszkańca wyłącznie przez istniejące API:
 * GET /api/tickets → GET /api/tickets/{id} → apartmentId.
 *
 * Backend nie filtruje GET /api/buildings/tree wg roli — nie używamy „pierwszego lokalu z drzewa”.
 */
@Singleton
class UserApartmentService @Inject constructor(
    private val ticketService: TicketService,
    private val authService: AuthService
) {

    @Volatile
    private var cached: ResidentApartmentInfo? = null

    suspend fun resolveForResident(): ResidentApartmentInfo {
        cached?.let { return it }

        val role = authService.getCurrentUserRole()
        if (role != UserRole.MIESZKANIEC) {
            throw UserApartmentException("Usługa dostępna tylko dla roli MIESZKANIEC.")
        }

        val tickets = ticketService.getTickets()
        if (tickets.isEmpty()) {
            throw UserApartmentException(
                "Brak przypisanego lokalu. Utwórz zgłoszenie serwisowe, aby powiązać konto z lokalem."
            )
        }

        val detail = ticketService.getTicketById(tickets.first().id)
            ?: throw UserApartmentException("Nie znaleziono szczegółów zgłoszenia.")

        val apartmentId = detail.apartmentId
            ?: throw UserApartmentException("Brak przypisanego lokalu w profilu użytkownika.")

        val label = detail.locationLabel?.let { "Lokal $it" } ?: "Twój lokal"
        return ResidentApartmentInfo(apartmentId, label).also { cached = it }
    }

    fun clearCache() {
        cached = null
    }
}

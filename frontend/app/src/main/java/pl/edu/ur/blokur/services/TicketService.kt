package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketService @Inject constructor(
    private val api: TicketApiService,
    private val tokenStorage: TokenStorage
) {
    suspend fun getTickets(): List<TicketSummaryDto> {
        return runCatching {
            val response = api.getTickets()
            handleResponse(response, "Błąd podczas pobierania zgłoszeń")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun getTicketById(id: String): TicketDetailDto? {
        return runCatching {
            val response = api.getTicketById(id)
            if (response.code() == 404) return@runCatching null
            handleResponse(response, "Błąd podczas pobierania detali zgłoszenia")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun getCategories(): List<CategoryDto> {
        return runCatching {
            val response = api.getCategories()
            handleResponse(response, "Błąd podczas pobierania kategorii")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun createTicket(request: CreateTicketRequest): TicketDetailDto {
        return runCatching {
            val response = api.createTicket(request)
            if (!response.isSuccessful) {
                val message = when (response.code()) {
                    400 -> "Błąd walidacji danych."
                    403 -> "Brak uprawnień. Upewnij się, że masz przypisany lokal."
                    422 -> "Niezgodność danych z regułami biznesowymi."
                    else -> "Błąd podczas tworzenia zgłoszenia (Kod: ${response.code()})"
                }
                throw Exception(message)
            }
            response.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun getCurrentUserRole(): String {
        return tokenStorage.getUserRole() ?: "GOSC"
    }

    suspend fun getAvailableConservators(): List<ConservatorDto> {
        return runCatching {
            val response = api.getConservators()
            handleResponse(response, "Błąd podczas pobierania konserwatorów")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun assignTicket(ticketId: String, conservatorId: String, plannedVisitAt: String): TicketDetailDto {
        return runCatching {
            val response = api.assignTicket(
                ticketId,
                TicketAssignRequest(assignedTo = conservatorId, plannedVisitAt = plannedVisitAt)
            )
            handleResponse(response, "Błąd podczas przypisywania konserwatora")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** PATCH /api/tickets/{id}/close — zamknięcie zgłoszenia (ZARZADCA). */
    suspend fun closeTicket(ticketId: String): TicketDetailDto {
        return runCatching {
            val response = api.closeTicket(ticketId)
            handleResponse(response, "Błąd podczas zamykania zgłoszenia")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** PATCH /api/tickets/{id}/reject — odrzucenie zgłoszenia z powodem (ZARZADCA). */
    suspend fun rejectTicket(ticketId: String, reason: String): TicketDetailDto {
        return runCatching {
            val response = api.rejectTicket(ticketId, TicketRejectRequest(reason = reason))
            handleResponse(response, "Błąd podczas odrzucania zgłoszenia")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** PATCH /api/tickets/{id}/start — rozpoczęcie prac (KONSERWATOR). */
    suspend fun startWork(ticketId: String): TicketDetailDto {
        return runCatching {
            val response = api.startWork(ticketId)
            handleResponse(response, "Błąd podczas rozpoczynania prac")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** PATCH /api/tickets/{id}/suspend — wstrzymanie prac z powodem (KONSERWATOR). */
    suspend fun suspendWork(ticketId: String, reason: String): TicketDetailDto {
        return runCatching {
            val response = api.suspendWork(ticketId, TicketSuspendRequest(reason = reason))
            handleResponse(response, "Błąd podczas wstrzymywania prac")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** POST /api/tickets/{id}/completion — zakończenie prac z opisem (KONSERWATOR). */
    suspend fun completeWork(ticketId: String, workDescription: String): TicketDetailDto {
        return runCatching {
            val response = api.completeWork(ticketId, TicketCompletionRequest(workDescription = workDescription))
            handleResponse(response, "Błąd podczas zgłaszania zakończenia prac")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    private fun <T> handleResponse(response: retrofit2.Response<T>, defaultErrorMessage: String): T {
        if (!response.isSuccessful) {
            val message = when (response.code()) {
                400 -> "Błąd walidacji danych."
                403 -> "Brak uprawnień do wykonania tej operacji."
                404 -> "Nie znaleziono wybranego zgłoszenia."
                409 -> "Operacja niedozwolona w aktualnym stanie zgłoszenia."
                422 -> "Niezgodność danych z regułami biznesowymi."
                else -> "$defaultErrorMessage (Kod: ${response.code()})"
            }
            throw Exception(message)
        }
        return response.body() ?: throw Exception("Pusta odpowiedź z serwera")
    }
}



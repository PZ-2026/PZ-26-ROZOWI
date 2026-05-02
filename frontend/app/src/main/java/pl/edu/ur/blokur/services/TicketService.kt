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

    private fun <T> handleResponse(response: retrofit2.Response<T>, defaultErrorMessage: String): T {
        if (!response.isSuccessful) {
            val message = when (response.code()) {
                400 -> "Błąd walidacji danych."
                403 -> "Brak uprawnień do wykonania tej operacji."
                404 -> "Nie znaleziono wybranego zgłoszenia."
                422 -> "Niezgodność danych z regułami biznesowymi."
                else -> "$defaultErrorMessage (Kod: ${response.code()})"
            }
            throw Exception(message)
        }
        return response.body() ?: throw Exception("Pusta odpowiedź z serwera")
    }
}


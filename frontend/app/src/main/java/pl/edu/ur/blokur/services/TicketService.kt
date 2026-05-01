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

    suspend fun getCurrentUserRole(): String {
        return tokenStorage.getUserRole() ?: "GOSC"
    }

    // Pozostawione mockowe funkcje w celach kompatybilności z formularzami, które jeszcze nie mają API
    suspend fun getAvailableConservators(): List<AppUserDto> =
        emptyList()

    suspend fun getCategories(): List<String> =
        listOf("Hydraulika", "Elektryka", "Domofony", "Części wspólne", "Winda", "Inne")

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

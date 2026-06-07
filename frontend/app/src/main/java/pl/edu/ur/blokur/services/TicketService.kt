package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketService @Inject constructor(
    private val api: TicketApiService,
    private val tokenStorage: TokenStorage
) {
    /**
     * Pobiera listę zgłoszeń z opcjonalnymi filtrami serwera.
     * Wszystkie parametry są opcjonalne — null = brak filtra.
     */
    suspend fun getTickets(
        status: String? = null,
        categoryId: String? = null,
        buildingId: String? = null,
        staircaseId: String? = null,
        assignedTo: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        search: String? = null,
        page: Int = 0,
        size: Int = 20
    ): List<TicketSummaryDto> {
        return runCatching {
            val response = api.getTickets(
                status = status,
                categoryId = categoryId,
                buildingId = buildingId,
                staircaseId = staircaseId,
                assignedTo = assignedTo,
                dateFrom = dateFrom,
                dateTo = dateTo,
                search = search,
                page = page,
                size = size
            )
            ApiResponseHandler.requireSuccess(response, "Błąd podczas pobierania zgłoszeń")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun getTicketById(id: String): TicketDetailDto? {
        return runCatching {
            val response = api.getTicketById(id)
            if (response.code() == 404) return@runCatching null
            ApiResponseHandler.requireSuccess(response, "Błąd podczas pobierania detali zgłoszenia")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun getCategories(): List<CategoryDto> {
        return runCatching {
            val response = api.getCategories()
            ApiResponseHandler.requireSuccess(response, "Błąd podczas pobierania kategorii")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun createTicket(request: CreateTicketRequest): TicketDetailDto {
        return runCatching {
            val response = api.createTicket(request)
            if (!response.isSuccessful) {
                val message = when (response.code()) {
                    403 -> "Brak uprawnień. Upewnij się, że masz przypisany lokal."
                    else -> ApiResponseHandler.mapHttpError(response, "Błąd podczas tworzenia zgłoszenia")
                }
                throw ApiException(message, response.code())
            }
            response.body() ?: throw ApiException("Pusta odpowiedź z serwera", response.code())
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun getCurrentUserRole(): String {
        return tokenStorage.getUserRole() ?: "GOSC"
    }

    suspend fun getAvailableConservators(): List<ConservatorDto> {
        return runCatching {
            val response = api.getConservators()
            ApiResponseHandler.requireSuccess(response, "Błąd podczas pobierania konserwatorów")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun assignTicket(ticketId: String, conservatorId: String, plannedVisitAt: String): TicketDetailDto {
        return runCatching {
            val response = api.assignTicket(
                ticketId,
                TicketAssignRequest(assignedTo = conservatorId, plannedVisitAt = plannedVisitAt)
            )
            ApiResponseHandler.requireSuccess(response, "Błąd podczas przypisywania konserwatora")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun closeTicket(ticketId: String): TicketDetailDto {
        return runCatching {
            val response = api.closeTicket(ticketId)
            ApiResponseHandler.requireSuccess(response, "Błąd podczas zamykania zgłoszenia")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun rejectTicket(ticketId: String, reason: String): TicketDetailDto {
        return runCatching {
            val response = api.rejectTicket(ticketId, TicketRejectRequest(reason = reason))
            ApiResponseHandler.requireSuccess(response, "Błąd podczas odrzucania zgłoszenia")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun startWork(ticketId: String): TicketDetailDto {
        return runCatching {
            val response = api.startWork(ticketId)
            ApiResponseHandler.requireSuccess(response, "Błąd podczas rozpoczynania prac")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun suspendWork(ticketId: String, reason: String): TicketDetailDto {
        return runCatching {
            val response = api.suspendWork(ticketId, TicketSuspendRequest(reason = reason))
            ApiResponseHandler.requireSuccess(response, "Błąd podczas wstrzymywania prac")
        }.getOrElse { throw wrapException(it) }
    }

    suspend fun completeWork(ticketId: String, workDescription: String): TicketDetailDto {
        return runCatching {
            val response = api.completeWork(ticketId, TicketCompletionRequest(workDescription = workDescription))
            ApiResponseHandler.requireSuccess(response, "Błąd podczas zgłaszania zakończenia prac")
        }.getOrElse { throw wrapException(it) }
    }

    /** PATCH /api/tickets/{id}/status — np. WSTRZYMANO → W_REALIZACJI (ZARZĄDCA). */
    suspend fun changeStatus(
        ticketId: String,
        status: String,
        comment: String? = null
    ): TicketDetailDto {
        return runCatching {
            val response = api.changeStatus(
                ticketId,
                TicketStatusChangeRequest(status = status, comment = comment)
            )
            ApiResponseHandler.requireSuccess(response, "Błąd zmiany statusu zgłoszenia")
        }.getOrElse { throw wrapException(it) }
    }

    private fun wrapException(cause: Throwable): Exception =
        if (cause is ApiException) cause else Exception(cause.message ?: "Błąd połączenia", cause)
}

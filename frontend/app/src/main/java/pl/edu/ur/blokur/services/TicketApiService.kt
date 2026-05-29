package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.CategoryDto
import pl.edu.ur.blokur.dtos.ConservatorDto
import pl.edu.ur.blokur.dtos.CreateTicketRequest
import pl.edu.ur.blokur.dtos.TicketAssignRequest
import pl.edu.ur.blokur.dtos.TicketCompletionRequest
import pl.edu.ur.blokur.dtos.TicketDetailDto
import pl.edu.ur.blokur.dtos.TicketRejectRequest
import pl.edu.ur.blokur.dtos.TicketSummaryDto
import pl.edu.ur.blokur.dtos.TicketSuspendRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TicketApiService {

    /**
     * GET /api/tickets — lista zgłoszeń z opcjonalnymi filtrami.
     *
     * Parametry (wszystkie opcjonalne):
     * @param status      nazwa statusu (np. "NOWE", "W_REALIZACJI")
     * @param categoryId  UUID kategorii
     * @param buildingId  UUID budynku
     * @param staircaseId UUID klatki schodowej
     * @param assignedTo  UUID konserwatora
     * @param dateFrom    data od (ISO 8601, np. "2026-01-01T00:00:00")
     * @param dateTo      data do (ISO 8601)
     * @param search      fraza do wyszukiwania w tytule / numerze zgłoszenia
     */
    @GET("api/tickets")
    suspend fun getTickets(
        @Query("status") status: String? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("buildingId") buildingId: String? = null,
        @Query("staircaseId") staircaseId: String? = null,
        @Query("assignedTo") assignedTo: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("search") search: String? = null
    ): Response<List<TicketSummaryDto>>

    @GET("api/tickets/{id}")
    suspend fun getTicketById(@Path("id") id: String): Response<TicketDetailDto>

    @GET("api/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("api/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): Response<TicketDetailDto>

    @GET("api/users")
    suspend fun getConservators(@Query("role") role: String = "KONSERWATOR"): Response<List<ConservatorDto>>

    /** PATCH /api/tickets/{id}/assign — przypisanie konserwatora (ZARZADCA). */
    @PATCH("api/tickets/{id}/assign")
    suspend fun assignTicket(
        @Path("id") ticketId: String,
        @Body request: TicketAssignRequest
    ): Response<TicketDetailDto>

    /** PATCH /api/tickets/{id}/close — zamknięcie zgłoszenia (ZARZADCA). */
    @PATCH("api/tickets/{id}/close")
    suspend fun closeTicket(@Path("id") ticketId: String): Response<TicketDetailDto>

    /** PATCH /api/tickets/{id}/reject — odrzucenie zgłoszenia z powodem (ZARZADCA). */
    @PATCH("api/tickets/{id}/reject")
    suspend fun rejectTicket(
        @Path("id") ticketId: String,
        @Body request: TicketRejectRequest
    ): Response<TicketDetailDto>

    /** PATCH /api/tickets/{id}/start — rozpoczęcie prac (KONSERWATOR). */
    @PATCH("api/tickets/{id}/start")
    suspend fun startWork(@Path("id") ticketId: String): Response<TicketDetailDto>

    /** PATCH /api/tickets/{id}/suspend — wstrzymanie prac z powodem (KONSERWATOR). */
    @PATCH("api/tickets/{id}/suspend")
    suspend fun suspendWork(
        @Path("id") ticketId: String,
        @Body request: TicketSuspendRequest
    ): Response<TicketDetailDto>

    /** POST /api/tickets/{id}/completion — zakończenie prac z opisem (KONSERWATOR). */
    @POST("api/tickets/{id}/completion")
    suspend fun completeWork(
        @Path("id") ticketId: String,
        @Body request: TicketCompletionRequest
    ): Response<TicketDetailDto>
}

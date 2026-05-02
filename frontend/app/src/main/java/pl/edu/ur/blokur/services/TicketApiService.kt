package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.CategoryDto
import pl.edu.ur.blokur.dtos.ConservatorDto
import pl.edu.ur.blokur.dtos.CreateTicketRequest
import pl.edu.ur.blokur.dtos.TicketAssignRequest
import pl.edu.ur.blokur.dtos.TicketDetailDto
import pl.edu.ur.blokur.dtos.TicketSummaryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TicketApiService {

    @GET("api/tickets")
    suspend fun getTickets(): Response<List<TicketSummaryDto>>

    @GET("api/tickets/{id}")
    suspend fun getTicketById(@Path("id") id: String): Response<TicketDetailDto>

    @GET("api/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("api/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): Response<TicketDetailDto>

    @GET("api/users")
    suspend fun getConservators(@Query("role") role: String = "KONSERWATOR"): Response<List<ConservatorDto>>

    @PATCH("api/tickets/{id}/assign")
    suspend fun assignTicket(
        @Path("id") ticketId: String,
        @Body request: TicketAssignRequest
    ): Response<TicketDetailDto>
}

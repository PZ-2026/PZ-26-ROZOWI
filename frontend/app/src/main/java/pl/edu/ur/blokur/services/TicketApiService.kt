package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.TicketDetailDto
import pl.edu.ur.blokur.dtos.TicketSummaryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TicketApiService {

    @GET("api/tickets")
    suspend fun getTickets(): Response<List<TicketSummaryDto>>

    @GET("api/tickets/{id}")
    suspend fun getTicketById(@Path("id") id: String): Response<TicketDetailDto>
}

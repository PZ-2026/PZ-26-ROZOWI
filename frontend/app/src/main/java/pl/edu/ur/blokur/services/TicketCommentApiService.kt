package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.TicketCommentDto
import pl.edu.ur.blokur.dtos.TicketCommentRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface dla endpointów komentarzy do zgłoszeń.
 *
 * - GET /api/tickets/{id}/comments — lista komentarzy (wszyscy zalogowani)
 * - POST /api/tickets/{id}/comments — dodaj komentarz (wszyscy zalogowani)
 *
 * Typy komentarzy: PUBLICZNY (widoczny dla wszystkich) / WEWNETRZNY (tylko personel).
 */
interface TicketCommentApiService {

    /** GET /api/tickets/{id}/comments — lista komentarzy do zgłoszenia. */
    @GET("/api/tickets/{id}/comments")
    suspend fun getComments(@Path("id") ticketId: String): Response<List<TicketCommentDto>>

    /**
     * POST /api/tickets/{id}/comments — dodaj komentarz do zgłoszenia.
     * Pole commentType: "PUBLICZNY" lub "WEWNETRZNY".
     */
    @POST("/api/tickets/{id}/comments")
    suspend fun addComment(
        @Path("id") ticketId: String,
        @Body request: TicketCommentRequestDto
    ): Response<TicketCommentDto>
}

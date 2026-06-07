package pl.edu.ur.blokur.services

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import pl.edu.ur.blokur.dtos.AnnouncementDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Retrofit interface dla endpointów ogłoszeń (/api/announcements).
 *
 * CRUD dostępny tylko dla ZARZADCA; GET dla wszystkich uwierzytelnionych.
 * Tworzenie i aktualizacja ogłoszeń używa multipart/form-data:
 *  - part "data"       — JSON z AnnouncementRequest (title, content, targetType, targetId, plannedDate)
 *  - part "attachment" — opcjonalny plik PDF (max ~10 MB)
 */
interface AnnouncementApiService {

    /** GET /api/announcements — lista ogłoszeń dostępnych dla zalogowanego użytkownika. */
    @GET("api/announcements")
    suspend fun getAnnouncements(): Response<List<AnnouncementDto>>

    /**
     * POST /api/announcements — utwórz ogłoszenie (ZARZADCA).
     * Part "data" to JSON body, part "attachment" — opcjonalny PDF.
     */
    @Multipart
    @POST("api/announcements")
    suspend fun createAnnouncement(
        @Part("data") data: RequestBody,
        @Part attachment: MultipartBody.Part?
    ): Response<AnnouncementDto>

    /**
     * PUT /api/announcements/{id} — zaktualizuj ogłoszenie (ZARZADCA).
     * Part "data" to JSON body, part "attachment" — opcjonalny nowy PDF.
     */
    @Multipart
    @PUT("api/announcements/{id}")
    suspend fun updateAnnouncement(
        @Path("id") id: String,
        @Part("data") data: RequestBody,
        @Part attachment: MultipartBody.Part?
    ): Response<AnnouncementDto>

    /** DELETE /api/announcements/{id} — usuń ogłoszenie (ZARZADCA). */
    @DELETE("api/announcements/{id}")
    suspend fun deleteAnnouncement(@Path("id") id: String): Response<Unit>

    /**
     * GET /api/announcements/{id}/attachment — pobierz załącznik PDF.
     * Zwraca ResponseBody do ręcznego odczytu bajtów PDF.
     */
    @Headers("Accept: application/pdf")
    @GET("api/announcements/{id}/attachment")
    suspend fun getAttachment(@Path("id") id: String): Response<ResponseBody>
}

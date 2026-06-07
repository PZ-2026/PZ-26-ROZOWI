package pl.edu.ur.blokur.services

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import pl.edu.ur.blokur.dtos.TicketImageDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Retrofit interface dla endpointów zdjęć w zgłoszeniach.
 *
 * - POST /api/tickets/{id}/images  — wgraj zdjęcie (multipart, param image_type: BEFORE|AFTER)
 * - GET  /api/tickets/{id}/images  — lista metadanych zdjęć
 * - GET  /api/images/{id}          — serwuj plik obrazu (JPEG/PNG jako ResponseBody)
 */
interface TicketImageApiService {

    @Multipart
    @POST("api/tickets/{id}/images")
    suspend fun uploadImage(
        @Path("id") ticketId: String,
        @Part("image_type") imageType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<TicketImageDto>

    @GET("api/tickets/{id}/images")
    suspend fun getImagesForTicket(@Path("id") ticketId: String): Response<List<TicketImageDto>>

    @Headers("Accept: image/jpeg, image/png, application/octet-stream")
    @GET("api/images/{id}")
    suspend fun serveImage(@Path("id") imageId: String): Response<ResponseBody>
}

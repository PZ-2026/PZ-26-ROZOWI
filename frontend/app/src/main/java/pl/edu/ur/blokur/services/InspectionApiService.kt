package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.InspectionRequestDto
import pl.edu.ur.blokur.dtos.InspectionResponseDto
import retrofit2.Response
import retrofit2.http.*

interface InspectionApiService {

    /** GET /api/inspections — lista przeglądów (filtrowana wg roli przez backend). */
    @GET("api/inspections")
    suspend fun getAll(): Response<List<InspectionResponseDto>>

    /** POST /api/inspections — utwórz nowy przegląd (ZARZADCA). Zwraca 201 + body. */
    @POST("api/inspections")
    suspend fun create(@Body request: InspectionRequestDto): Response<InspectionResponseDto>

    /** PUT /api/inspections/{id} — aktualizuj przegląd (ZARZADCA). */
    @PUT("api/inspections/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: InspectionRequestDto
    ): Response<InspectionResponseDto>

    /** DELETE /api/inspections/{id} — usuń przegląd (ZARZADCA). Zwraca 204. */
    @DELETE("api/inspections/{id}")
    suspend fun delete(@Path("id") id: String): Response<Void>
}

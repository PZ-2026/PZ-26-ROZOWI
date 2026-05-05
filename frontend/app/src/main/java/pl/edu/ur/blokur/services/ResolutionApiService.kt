package pl.edu.ur.blokur.services

import okhttp3.ResponseBody
import pl.edu.ur.blokur.dtos.CastVoteRequest
import pl.edu.ur.blokur.dtos.CreateResolutionRequest
import pl.edu.ur.blokur.dtos.ResolutionDetailDto
import pl.edu.ur.blokur.dtos.ResolutionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ResolutionApiService {

    /** GET /api/resolutions — lista uchwał (filtrowana wg roli przez backend). */
    @GET("api/resolutions")
    suspend fun getResolutions(): Response<List<ResolutionDto>>

    /** GET /api/resolutions/{id} — szczegóły uchwały z opcjami i wynikami. */
    @GET("api/resolutions/{id}")
    suspend fun getResolutionDetails(
        @Path("id") id: String
    ): Response<ResolutionDetailDto>

    /** POST /api/resolutions — utwórz nową uchwałę (ZARZADCA). Zwraca 201 bez body. */
    @POST("api/resolutions")
    suspend fun createResolution(
        @Body request: CreateResolutionRequest
    ): Response<Void>

    /** POST /api/resolutions/{id}/vote — oddaj głos. Zwraca 204 bez body. */
    @POST("api/resolutions/{id}/vote")
    suspend fun castVote(
        @Path("id") resolutionId: String,
        @Body request: CastVoteRequest
    ): Response<Void>

    /** GET /api/resolutions/{id}/report — pobierz PDF z wynikami (ZARZADCA). */
    @Headers("Accept: application/pdf")
    @GET("api/resolutions/{id}/report")
    suspend fun getResolutionReport(
        @Path("id") resolutionId: String
    ): Response<ResponseBody>
}

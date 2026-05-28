package pl.edu.ur.blokur.services

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// ─── Request DTOs ─────────────────────────────────────────────────────────────

data class RateChangeDistributionRequestDto(
    val subject: String,
    val body: String,
    val effectiveDate: String,
    val scope: String,
    val targetId: String?
)

data class AnnualSettlementDistributionRequestDto(
    val year: Int,
    val note: String?,
    val scope: String,
    val targetId: String?
)

// ─── Response DTOs ────────────────────────────────────────────────────────────

data class DocumentDistributionResultDto(
    val documentsGenerated: Int,
    val recipientsNotified: Int,
    val message: String
)

// ─── Retrofit interface ───────────────────────────────────────────────────────

/** Retrofit interface dla endpointów dokumentów administracyjnych. */
interface DocumentApiService {

    /**
     * Dystrybuuje zawiadomienie o zmianie stawek do wybranych mieszkańców.
     * POST /api/admin/documents/rate-change
     */
    @POST("/api/admin/documents/rate-change")
    suspend fun distributeRateChange(
        @Body request: RateChangeDistributionRequestDto
    ): Response<DocumentDistributionResultDto>

    /**
     * Generuje i dystrybuuje roczne rozliczenia do wybranych mieszkańców.
     * POST /api/admin/documents/annual-settlement
     */
    @POST("/api/admin/documents/annual-settlement")
    suspend fun distributeAnnualSettlement(
        @Body request: AnnualSettlementDistributionRequestDto
    ): Response<DocumentDistributionResultDto>

    /**
     * Przesyła logo nieruchomości.
     * PATCH /api/properties/{id}/logo
     */
    @Multipart
    @PATCH("/api/properties/{id}/logo")
    suspend fun uploadPropertyLogo(
        @Path("id") propertyId: String,
        @Part file: MultipartBody.Part
    ): Response<Unit>
}

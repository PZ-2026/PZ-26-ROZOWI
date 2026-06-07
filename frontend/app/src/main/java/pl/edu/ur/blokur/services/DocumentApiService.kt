package pl.edu.ur.blokur.services

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import pl.edu.ur.blokur.dtos.PropertyResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

// ─── Retrofit interface — dokumenty administracyjne ───────────────────────────

/** Retrofit interface dla endpointów dokumentów administracyjnych. */
interface DocumentApiService {

    /**
     * Dystrybuuje zawiadomienie o zmianie stawek do wybranych mieszkańców.
     * POST /api/admin/documents/rate-change
     */
    @POST("api/admin/documents/rate-change")
    suspend fun distributeRateChange(
        @Body request: RateChangeDistributionRequestDto
    ): Response<DocumentDistributionResultDto>

    /**
     * Generuje i dystrybuuje roczne rozliczenia do wybranych mieszkańców.
     * POST /api/admin/documents/annual-settlement
     */
    @POST("api/admin/documents/annual-settlement")
    suspend fun distributeAnnualSettlement(
        @Body request: AnnualSettlementDistributionRequestDto
    ): Response<DocumentDistributionResultDto>

    /**
     * Przesyła logo nieruchomości.
     * PATCH /api/properties/{id}/logo
     *
     * POPRAWKA: backend zwraca zaktualizowany PropertyResponse (nie Unit) —
     * używamy go do odświeżenia stanu UI bez konieczności dodatkowego GET.
     */
    @Multipart
    @PATCH("api/properties/{id}/logo")
    suspend fun uploadPropertyLogo(
        @Path("id") propertyId: String,
        @Part file: MultipartBody.Part
    ): Response<PropertyResponseDto>
}

// ─── Retrofit interface — generowanie PDF ─────────────────────────────────────

/**
 * Retrofit interface dla endpointów generowania PDF (/api/pdf).
 *
 * Zastępuje dotychczasowe ręczne budowanie URL w ApartmentBalancesViewModel.
 * Dzięki temu token JWT jest automatycznie dołączany przez AuthInterceptor,
 * a błędy HTTP są spójnie obsługiwane przez Retrofit.
 */
interface PdfApiService {

    /**
     * GET /api/pdf/balances — generuje zestawienie sald i zaległości lokali jako PDF.
     * Dostęp: ZARZADCA.
     *
     * @param propertyId     UUID nieruchomości (opcjonalny filtr)
     * @param minDebt        minimalna kwota zaległości w PLN (opcjonalny)
     * @param minDaysOverdue minimalna liczba dni zalegania (opcjonalny)
     * @param sort           "debt_desc" (domyślnie) lub "debt_asc"
     * @param save           true — PDF zostaje zarchiwizowany jako dokument w systemie
     */
    @GET("api/pdf/balances")
    suspend fun getBalancesPdf(
        @Query("propertyId") propertyId: String? = null,
        @Query("minDebt") minDebt: String? = null,
        @Query("minDaysOverdue") minDaysOverdue: Long? = null,
        @Query("sort") sort: String? = null,
        @Query("save") save: Boolean = false
    ): Response<ResponseBody>

    /**
     * POST /api/pdf/work-acceptance-protocol — generuje protokół odbioru prac jako PDF.
     * Dostęp: ZARZADCA i KONSERWATOR.
     * Backend generuje PDF na podstawie przekazanego opisu prac i danych zgłoszenia.
     */
    @POST("api/pdf/work-acceptance-protocol")
    suspend fun getWorkAcceptanceProtocol(
        @Body request: WorkAcceptanceProtocolRequestDto
    ): Response<ResponseBody>
}

/** Request DTO dla POST /api/pdf/work-acceptance-protocol. */
data class WorkAcceptanceProtocolRequestDto(
    val ticketNumber: String,
    val workDescription: String,
    /** Imię i nazwisko konserwatora — pole musi się zgadzać z backendem: maintenanceWorkerName */
    val maintenanceWorkerName: String,
    val beforeImagesPaths: List<String> = emptyList(),
    val afterImagesPaths: List<String> = emptyList()
)

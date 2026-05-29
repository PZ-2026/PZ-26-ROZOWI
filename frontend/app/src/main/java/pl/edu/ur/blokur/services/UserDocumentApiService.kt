package pl.edu.ur.blokur.services

import okhttp3.ResponseBody
import pl.edu.ur.blokur.dtos.UserDocumentDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface dla endpointów listy i pobierania dokumentów użytkownika.
 *
 * Uwaga: ten serwis obsługuje /api/documents (dokumenty *użytkownika*).
 * Dokumenty administracyjne (dystrybucja stawek/rozliczeń) obsługuje DocumentApiService
 * pod /api/admin/documents.
 *
 * Reguły widoczności (backend):
 *  - ZARZADCA widzi wszystkie dokumenty nieruchomości.
 *  - MIESZKANIEC widzi tylko swoje (powiązane z jego lokalem).
 */
interface UserDocumentApiService {

    /**
     * GET /api/documents — lista dokumentów.
     * Parametry opcjonalne: apartmentId, startDate (YYYY-MM-DD), endDate, type.
     */
    @GET("/api/documents")
    suspend fun getDocuments(
        @Query("apartmentId") apartmentId: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("type") type: String? = null
    ): Response<List<UserDocumentDto>>

    /**
     * GET /api/documents/{id}/download — pobierz plik PDF dokumentu.
     * Zwraca ResponseBody do ręcznego zapisu na dysk lub otwarcia przez FileProvider.
     */
    @Headers("Accept: application/pdf")
    @GET("/api/documents/{id}/download")
    suspend fun downloadDocument(@Path("id") documentId: String): Response<ResponseBody>
}

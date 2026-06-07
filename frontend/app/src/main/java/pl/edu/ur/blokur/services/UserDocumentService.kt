package pl.edu.ur.blokur.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import pl.edu.ur.blokur.dtos.UserDocumentDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serwis dokumentów użytkownika — lista i pobieranie PDF.
 *
 * Uwaga: dotyczy endpointu /api/documents (dokumenty MIESZKAŃCA lub ZARZĄDCY).
 * Dystrybucja dokumentów (admin) jest w DocumentApiService → DocumentDistributionViewModel.
 */
@Singleton
class UserDocumentService @Inject constructor(
    private val api: UserDocumentApiService
) {
    /**
     * GET /api/documents — pobiera listę dokumentów.
     * Opcjonalne filtry: apartmentId, startDate/endDate ("YYYY-MM-DD"), type.
     */
    suspend fun getDocuments(
        apartmentId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        type: String? = null
    ): List<UserDocumentDto> = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.getDocuments(apartmentId, startDate, endDate, type), "Błąd pobierania dokumentów")
    }

    /**
     * GET /api/documents/{id}/download — pobiera surowe bajty pliku PDF.
     * Wynik (ResponseBody) należy zapisać do pliku lub otworzyć przez FileProvider/Intent.
     */
    suspend fun downloadDocument(documentId: String): ResponseBody = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.downloadDocument(documentId), "Błąd pobierania pliku dokumentu")
    }
}

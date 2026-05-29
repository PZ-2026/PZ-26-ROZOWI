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
        val response = api.getDocuments(apartmentId, startDate, endDate, type)
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception(handleError(response.code(), "pobierania dokumentów"))
        }
    }

    /**
     * GET /api/documents/{id}/download — pobiera surowe bajty pliku PDF.
     * Wynik (ResponseBody) należy zapisać do pliku lub otworzyć przez FileProvider/Intent.
     */
    suspend fun downloadDocument(documentId: String): ResponseBody = withContext(Dispatchers.IO) {
        val response = api.downloadDocument(documentId)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Brak treści odpowiedzi")
        } else {
            throw Exception(handleError(response.code(), "pobierania pliku dokumentu"))
        }
    }

    private fun handleError(code: Int, action: String): String = when (code) {
        401 -> "Brak autoryzacji. Zaloguj się ponownie."
        403 -> "Brak uprawnień do $action."
        404 -> "Nie znaleziono dokumentu."
        else -> "Błąd serwera ($code) podczas $action."
    }
}

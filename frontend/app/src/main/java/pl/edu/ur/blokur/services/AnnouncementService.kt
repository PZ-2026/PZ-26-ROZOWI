package pl.edu.ur.blokur.services

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import pl.edu.ur.blokur.dtos.AnnouncementDto
import pl.edu.ur.blokur.dtos.AnnouncementRequestDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serwis ogłoszeń — pośredniczy między ViewModelami a AnnouncementApiService.
 *
 * Multipart: AnnouncementRequest serializowany jest do JSON i wysyłany
 * jako part "data" (application/json), opcjonalny załącznik PDF jako part "attachment".
 */
@Singleton
class AnnouncementService @Inject constructor(
    private val api: AnnouncementApiService
) {
    private val gson = Gson()

    /** Pobiera listę ogłoszeń dostępnych dla zalogowanego użytkownika. */
    suspend fun getAnnouncements(): List<AnnouncementDto> = withContext(Dispatchers.IO) {
        val response = api.getAnnouncements()
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception(handleError(response.code(), "pobierania ogłoszeń"))
        }
    }

    /**
     * Tworzy nowe ogłoszenie.
     * @param request   dane ogłoszenia
     * @param pdfBytes  opcjonalny załącznik PDF (bajty)
     * @param pdfName   nazwa pliku załącznika
     */
    suspend fun createAnnouncement(
        request: AnnouncementRequestDto,
        pdfBytes: ByteArray? = null,
        pdfName: String = "attachment.pdf"
    ): AnnouncementDto = withContext(Dispatchers.IO) {
        val dataPart = gson.toJson(request)
            .toRequestBody("application/json".toMediaTypeOrNull())
        val attachmentPart = pdfBytes?.let {
            MultipartBody.Part.createFormData(
                "attachment", pdfName,
                it.toRequestBody("application/pdf".toMediaTypeOrNull())
            )
        }
        val response = api.createAnnouncement(dataPart, attachmentPart)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Pusta odpowiedź z serwera")
        } else {
            throw Exception(handleError(response.code(), "tworzenia ogłoszenia"))
        }
    }

    /**
     * Aktualizuje istniejące ogłoszenie.
     * @param id        UUID ogłoszenia
     * @param request   nowe dane
     * @param pdfBytes  opcjonalny nowy załącznik PDF
     * @param pdfName   nazwa pliku nowego załącznika
     */
    suspend fun updateAnnouncement(
        id: String,
        request: AnnouncementRequestDto,
        pdfBytes: ByteArray? = null,
        pdfName: String = "attachment.pdf"
    ): AnnouncementDto = withContext(Dispatchers.IO) {
        val dataPart = gson.toJson(request)
            .toRequestBody("application/json".toMediaTypeOrNull())
        val attachmentPart = pdfBytes?.let {
            MultipartBody.Part.createFormData(
                "attachment", pdfName,
                it.toRequestBody("application/pdf".toMediaTypeOrNull())
            )
        }
        val response = api.updateAnnouncement(id, dataPart, attachmentPart)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Pusta odpowiedź z serwera")
        } else {
            throw Exception(handleError(response.code(), "aktualizacji ogłoszenia"))
        }
    }

    /** Usuwa ogłoszenie (ZARZADCA). */
    suspend fun deleteAnnouncement(id: String) = withContext(Dispatchers.IO) {
        val response = api.deleteAnnouncement(id)
        if (!response.isSuccessful) {
            throw Exception(handleError(response.code(), "usuwania ogłoszenia"))
        }
    }

    /** Pobiera załącznik PDF ogłoszenia jako ResponseBody. */
    suspend fun getAttachment(id: String): ResponseBody = withContext(Dispatchers.IO) {
        val response = api.getAttachment(id)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Brak treści odpowiedzi")
        } else {
            throw Exception(handleError(response.code(), "pobierania załącznika"))
        }
    }

    private fun handleError(code: Int, action: String): String = when (code) {
        401 -> "Brak autoryzacji. Zaloguj się ponownie."
        403 -> "Brak uprawnień do $action."
        404 -> "Nie znaleziono zasobu."
        else -> "Błąd serwera ($code) podczas $action."
    }
}
